import sys
from pathlib import Path
import argparse
import json
import os
import uuid
import mysql.connector
from pymongo import MongoClient
from typing import List, Dict, Any
from datetime import datetime, timedelta, timezone
from bson import ObjectId
import requests
import time

from mcp.server.fastmcp import FastMCP

# 数据库配置
MYSQL_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': '123456',
    'database': 'tlbglxt',
    'charset': 'utf8mb4',
    'autocommit': True
}

MONGODB_CONFIG = {
    'host': 'localhost',
    'port': 27017,
    'database': 'tlbglxt_health'
}

# RAG API配置
RAG_API_CONFIG = {
    'base_url': 'http://localhost:8001',
    'timeout': 30,
    'default_top_k': 5,
    'default_similarity_threshold': 0.0
}

def parse_args():
    """Parse command line arguments for MCP server."""
    parser = argparse.ArgumentParser(description="Medical System MCP Server")
    parser.add_argument('--port', type=int, default=50001, help='Server port (default: 50001)')
    parser.add_argument('--host', default='0.0.0.0', help='Server host (default: 0.0.0.0)')
    parser.add_argument('--log-level', default='INFO', 
                       choices=['DEBUG', 'INFO', 'WARNING', 'ERROR'],
                       help='Logging level (default: INFO)')
    try:
        args = parser.parse_args()
    except SystemExit:
        class Args:
            port = 50001
            host = '0.0.0.0'
            log_level = 'INFO'
        args = Args()
    return args

args = parse_args()
mcp = FastMCP("medical_system_assistant", port=args.port, host=args.host)

def normalize_datetime_to_utc(dt_input):
    """
    标准化时间为UTC时区
    
    Args:
        dt_input: datetime对象或字符串
        
    Returns:
        带UTC时区的datetime对象
    """
    if isinstance(dt_input, str):
        # 处理字符串时间
        if dt_input.endswith('Z'):
            return datetime.fromisoformat(dt_input.replace('Z', '+00:00'))
        elif '+' not in dt_input and 'T' in dt_input:
            # 处理无时区的ISO格式，假设为UTC时间
            return datetime.fromisoformat(dt_input).replace(tzinfo=timezone.utc)
        else:
            return datetime.fromisoformat(dt_input)
    elif isinstance(dt_input, datetime):
        # 处理datetime对象
        if dt_input.tzinfo is None:
            # 无时区信息，假设为UTC
            return dt_input.replace(tzinfo=timezone.utc)
        else:
            return dt_input
    else:
        # 兜底：当前UTC时间
        return datetime.now(timezone.utc)

def get_mysql_connection():
    """获取MySQL数据库连接"""
    try:
        connection = mysql.connector.connect(**MYSQL_CONFIG)
        return connection
    except mysql.connector.Error as err:
        print(f"MySQL连接失败: {err}")
        return None

def get_mongodb_connection():
    """获取MongoDB数据库连接"""
    try:
        client = MongoClient(MONGODB_CONFIG['host'], MONGODB_CONFIG['port'])
        db = client[MONGODB_CONFIG['database']]
        return client, db
    except Exception as err:
        print(f"MongoDB连接失败: {err}")
        return None, None

@mcp.tool()
def query_user_health_records(user_id: int, record_type: str = "all", days: int = 30, limit: int = 50) -> str:
    """
    查询用户健康记录数据(MySQL+MongoDB联合查询)

    Args:
        user_id: 用户ID
        record_type: 记录类型 (all, glucose, pressure, weight)
        days: 查询最近多少天的数据 (默认30天)
        limit: 限制返回记录数量 (默认50条)

    Returns:
        JSON格式的健康记录数据
    """
    
    mysql_conn = get_mysql_connection()
    if mysql_conn is None:
        return json.dumps({"error": "MySQL数据库连接失败"})
    
    mongo_client, mongo_db = get_mongodb_connection()
    if mongo_client is None or mongo_db is None:
        return json.dumps({"error": "MongoDB数据库连接失败"})
    
    try:
        cursor = mysql_conn.cursor(dictionary=True)
        
        # 查询用户基本信息 (MySQL)
        cursor.execute("""
            SELECT id, real_name, gender, birthday, phone, email
            FROM sys_user 
            WHERE id = %s AND is_deleted = 0
        """, (user_id,))
        
        user_info = cursor.fetchone()
        if not user_info:
            return json.dumps({"error": "用户不存在"})
        
        result = {
            "user_info": user_info,
            "health_records": {},
            "query_params": {
                "days": days,
                "limit": limit,
                "record_type": record_type
            }
        }
        
        # 计算时间范围（使用UTC时区以匹配MongoDB记录）
        end_date = datetime.now(timezone.utc)
        start_date = end_date - timedelta(days=days)
        
        # 查询MongoDB中的健康记录
        if record_type in ["all", "glucose"]:
            glucose_collection = mongo_db.blood_glucose_records
            # 查询用户的血糖记录文档
            glucose_doc = glucose_collection.find_one({"userId": user_id})
            
            if glucose_doc and "records" in glucose_doc:
                # 从records数组中筛选最近的记录
                records = glucose_doc["records"]
                filtered_records = []
                
                for record in records:
                    if "measureTime" in record:
                        measure_time = normalize_datetime_to_utc(record["measureTime"])
                        
                        if start_date <= measure_time <= end_date:
                            record_copy = record.copy()
                            record_copy["measureTime"] = measure_time.isoformat()
                            filtered_records.append(record_copy)
                
                # 按时间倒序排序并限制数量
                filtered_records.sort(key=lambda x: x["measureTime"], reverse=True)
                result["health_records"]["glucose"] = filtered_records[:limit]
            else:
                result["health_records"]["glucose"] = []
            
        if record_type in ["all", "pressure"]:
            pressure_collection = mongo_db.blood_pressure_records
            pressure_doc = pressure_collection.find_one({"userId": user_id})
            
            if pressure_doc and "records" in pressure_doc:
                records = pressure_doc["records"]
                filtered_records = []
                
                for record in records:
                    if "measureTime" in record:
                        measure_time = normalize_datetime_to_utc(record["measureTime"])
                        
                        if start_date <= measure_time <= end_date:
                            record_copy = record.copy()
                            record_copy["measureTime"] = measure_time.isoformat()
                            filtered_records.append(record_copy)

                filtered_records.sort(key=lambda x: x["measureTime"], reverse=True)
                result["health_records"]["pressure"] = filtered_records[:limit]
            else:
                result["health_records"]["pressure"] = []
            
        if record_type in ["all", "weight"]:
            weight_collection = mongo_db.weight_records
            weight_doc = weight_collection.find_one({"userId": user_id})
            
            if weight_doc and "records" in weight_doc:
                records = weight_doc["records"]
                filtered_records = []
                
                for record in records:
                    if "measureTime" in record:
                        measure_time = normalize_datetime_to_utc(record["measureTime"])
                        
                        if start_date <= measure_time <= end_date:
                            record_copy = record.copy()
                            record_copy["measureTime"] = measure_time.isoformat()
                            filtered_records.append(record_copy)

                filtered_records.sort(key=lambda x: x["measureTime"], reverse=True)
                result["health_records"]["weight"] = filtered_records[:limit]
            else:
                result["health_records"]["weight"] = []
        
        return json.dumps(result, ensure_ascii=False, default=str)
        
    except Exception as err:
        return json.dumps({"error": f"查询失败: {err}"})
    finally:
        if mysql_conn and mysql_conn.is_connected():
            cursor.close()
            mysql_conn.close()
        if mongo_client:
            mongo_client.close()

@mcp.tool()
def query_doctor_list(department: str = "", available_only: bool = True, limit: int = 20) -> str:
    """
    查询医生列表信息(MySQL+MongoDB联合查询)

    Args:
        department: 科室筛选 (空字符串表示全部)
        available_only: 是否只查询可接诊的医生
        limit: 限制返回医生数量 (默认20个)

    Returns:
        JSON格式的医生列表数据(包含实时咨询统计)
    """
    
    mysql_conn = get_mysql_connection()
    if mysql_conn is None:
        return json.dumps({"error": "MySQL数据库连接失败"})
    
    mongo_client, mongo_db = get_mongodb_connection()
    if mongo_client is None or mongo_db is None:
        return json.dumps({"error": "MongoDB数据库连接失败"})
    
    try:
        cursor = mysql_conn.cursor(dictionary=True)
        
        # 构建查询条件
        where_conditions = ["di.is_deleted = 0", "su.is_deleted = 0"]
        params = []
        
        if department:
            where_conditions.append("di.department = %s")
            params.append(department)
            
        if available_only:
            where_conditions.append("COALESCE(di.status, 1) = 1")
            where_conditions.append("su.status = 1")
        
        where_clause = " AND ".join(where_conditions)
        
        query = f"""
            SELECT 
                di.id, di.doctor_no, di.department, di.title, 
                di.work_years as experience, di.introduction, di.speciality,
                COALESCE(di.status, 1) as available, di.online_status,
                di.consultation_count, di.rating,
                su.id as user_id, su.real_name, su.username, su.email, su.phone, su.gender
            FROM doctor_info di 
            INNER JOIN sys_user su ON di.user_id = su.id 
            WHERE {where_clause}
            ORDER BY di.online_status DESC, di.rating DESC
            LIMIT {limit}
        """
        
        cursor.execute(query, params)
        doctors = cursor.fetchall()
        
        # 为每个医生查询MongoDB中的实时咨询统计
        consultation_collection = mongo_db.consultation_chats
        for doctor in doctors:
            user_id = doctor['user_id']
            
            # 统计总咨询数
            total_consultations = consultation_collection.count_documents({"doctorId": user_id})
            
            # 统计进行中的咨询
            ongoing_consultations = consultation_collection.count_documents({
                "doctorId": user_id, 
                "status": {"$in": [1, 2]}
            })
            
            # 统计今日咨询数 - 使用字符串时间比较
            today_start_str = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0).isoformat()
            today_consultations = consultation_collection.count_documents({
                "doctorId": user_id,
                "createTime": {"$gte": today_start_str}
            })
            
            doctor["real_consultation_count"] = total_consultations
            doctor["ongoing_consultations"] = ongoing_consultations  
            doctor["today_consultations"] = today_consultations
        
        result = {
            "total_count": len(doctors),
            "doctors": doctors,
            "query_params": {
                "department": department,
                "available_only": available_only,
                "limit": limit
            }
        }
        
        return json.dumps(result, ensure_ascii=False, default=str)
        
    except Exception as err:
        return json.dumps({"error": f"查询失败: {err}"})
    finally:
        if mysql_conn and mysql_conn.is_connected():
            cursor.close()
            mysql_conn.close()
        if mongo_client:
            mongo_client.close()

@mcp.tool()
def query_user_consultations(user_id: int, status: str = "all", days: int = 30, limit: int = 20) -> str:
    """
    查询用户咨询记录(MySQL+MongoDB联合查询)

    Args:
        user_id: 用户ID
        status: 咨询状态 (all, pending, ongoing, completed, cancelled)
        days: 查询最近多少天的数据 (默认30天)
        limit: 限制返回咨询数量 (默认20条)

    Returns:
        JSON格式的用户咨询数据
    """
    
    mysql_conn = get_mysql_connection()
    if mysql_conn is None:
        return json.dumps({"error": "MySQL数据库连接失败"})
    
    mongo_client, mongo_db = get_mongodb_connection()
    if mongo_client is None or mongo_db is None:
        return json.dumps({"error": "MongoDB数据库连接失败"})
    
    try:
        cursor = mysql_conn.cursor(dictionary=True)
        
        # 查询用户基本信息 (MySQL)
        cursor.execute("""
            SELECT id, real_name, phone, email, user_type
            FROM sys_user 
            WHERE id = %s AND is_deleted = 0
        """, (user_id,))
        
        user_info = cursor.fetchone()
        if not user_info:
            return json.dumps({"error": "用户不存在"})
        
        # 构建MongoDB查询条件
        mongo_query = {"patientId": user_id}
        
        # 状态筛选
        mongo_query = {"$or": [{"patientId": user_id}, {"doctorId": user_id}]}
        
        if status != "all":
            status_map = {
                "pending": 1,
                "ongoing": 2, 
                "completed": 3,
                "cancelled": 4
            }
            if status in status_map:
                mongo_query["status"] = status_map[status]
        
        # 查询咨询记录 (MongoDB) - 先不做时间过滤
        consultation_collection = mongo_db.consultation_chats
        consultations = list(consultation_collection.find(mongo_query)
                           .sort("createTime", -1)
                           .limit(limit * 2))  # 获取更多数据用于时间过滤
        
        # 时间范围筛选 - 在Python中处理字符串时间
        start_date = datetime.now() - timedelta(days=days) if days > 0 else None
        filtered_consultations = []
        
        for consultation in consultations:
            # 处理时间字段
            create_time_str = consultation.get("createTime")
            if create_time_str:
                try:
                    if isinstance(create_time_str, str):
                                                    # 尝试解析字符串时间 - 支持两种格式
                            try:
                                create_time = datetime.fromisoformat(create_time_str)
                            except:
                                create_time = datetime.strptime(create_time_str, "%Y-%m-%d %H:%M:%S")
                    else:
                        create_time = create_time_str
                    
                                            # 时间范围检查
                        if start_date is None or create_time >= start_date:
                            consultation["createTime"] = create_time.isoformat()
                        filtered_consultations.append(consultation)
                        
                        if len(filtered_consultations) >= limit:
                            break
                            
                except Exception as e:
                    print(f"时间解析错误: {e}, 原始值: {create_time_str}")
                    # 如果时间解析失败，仍然包含该记录
                    consultation["createTime"] = str(create_time_str)
                    filtered_consultations.append(consultation)
        
        # 获取相关医生信息
        doctor_ids = [c.get("doctorId") for c in filtered_consultations if c.get("doctorId")]
        doctor_info_map = {}
        
        if doctor_ids:
            # 查询医生信息 (MySQL)
            placeholders = ','.join(['%s'] * len(doctor_ids))
            cursor.execute(f"""
                SELECT 
                    su.id as user_id, su.real_name,
                    di.department, di.title, di.doctor_no
                FROM sys_user su
                INNER JOIN doctor_info di ON su.id = di.user_id
                WHERE su.id IN ({placeholders}) AND su.is_deleted = 0
            """, doctor_ids)
            
            doctors = cursor.fetchall()
            for doctor in doctors:
                doctor_info_map[doctor['user_id']] = doctor
        
        # 处理咨询记录数据
        for consultation in filtered_consultations:
            consultation["_id"] = str(consultation["_id"])
            
            # 处理其他时间字段
            for time_field in ["startTime", "endTime"]:
                if time_field in consultation and consultation[time_field]:
                    time_value = consultation[time_field]
                    if isinstance(time_value, str):
                        consultation[time_field] = time_value
                    else:
                        consultation[time_field] = time_value.isoformat() if time_value else None
            
            # 添加医生信息
            doctor_id = consultation.get("doctorId")
            if doctor_id and doctor_id in doctor_info_map:
                consultation["doctor_info"] = doctor_info_map[doctor_id]
        
        consultations = filtered_consultations
        
        result = {
            "user_info": user_info,
            "consultations": consultations,
            "total_count": len(consultations),
            "query_params": {
                "status": status,
                "days": days,
                "limit": limit
            }
        }
        
        return json.dumps(result, ensure_ascii=False, default=str)
        
    except Exception as err:
        return json.dumps({"error": f"查询失败: {err}"})
    finally:
        if mysql_conn and mysql_conn.is_connected():
            cursor.close()
            mysql_conn.close()
        if mongo_client:
            mongo_client.close()

@mcp.tool()
def query_department_info(department: str = "", limit: int = 20) -> str:
    """
    查询科室信息和医生分布(MySQL+MongoDB联合查询)

    Args:
        department: 科室名称 (空字符串查询所有科室)
        limit: 限制返回医生数量 (默认20个)

    Returns:
        JSON格式的科室信息(包含实时咨询统计)
    """
    
    mysql_conn = get_mysql_connection()
    if mysql_conn is None:
        return json.dumps({"error": "MySQL数据库连接失败"})
    
    mongo_client, mongo_db = get_mongodb_connection()
    if mongo_client is None or mongo_db is None:
        return json.dumps({"error": "MongoDB数据库连接失败"})
    
    try:
        cursor = mysql_conn.cursor(dictionary=True)
        
        if department:
            # 查询特定科室的医生信息
            cursor.execute("""
                SELECT 
                    di.id, di.department, di.title, di.online_status,
                    COALESCE(di.status, 1) as available,
                    su.id as user_id, su.real_name, di.rating, di.consultation_count
                FROM doctor_info di 
                INNER JOIN sys_user su ON di.user_id = su.id 
                WHERE di.department = %s AND di.is_deleted = 0 AND su.is_deleted = 0
                ORDER BY di.online_status DESC, di.rating DESC
                LIMIT %s
            """, (department, limit))
            
            doctors = cursor.fetchall()
            
            # 为每个医生添加MongoDB中的实时咨询统计
            consultation_collection = mongo_db.consultation_chats
            today_start = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
            
            for doctor in doctors:
                user_id = doctor['user_id']
                
                # 实时咨询统计
                total_consultations = consultation_collection.count_documents({"doctorId": user_id})
                ongoing_consultations = consultation_collection.count_documents({
                    "doctorId": user_id, 
                    "status": {"$in": [1, 2]}
                })
                today_consultations = consultation_collection.count_documents({
                    "doctorId": user_id,
                    "createTime": {"$gte": today_start}
                })
                
                doctor["real_consultation_count"] = total_consultations
                doctor["ongoing_consultations"] = ongoing_consultations
                doctor["today_consultations"] = today_consultations
            
            result = {
                "department": department,
                "doctor_count": len(doctors),
                "doctors": doctors,
                "query_params": {"limit": limit}
            }
        else:
            # 查询所有科室统计
            cursor.execute("""
                SELECT 
                    di.department,
                    COUNT(*) as doctor_count,
                    SUM(CASE WHEN di.online_status = 1 THEN 1 ELSE 0 END) as online_count,
                    SUM(CASE WHEN COALESCE(di.status, 1) = 1 THEN 1 ELSE 0 END) as available_count,
                    AVG(di.rating) as avg_rating
                FROM doctor_info di 
                INNER JOIN sys_user su ON di.user_id = su.id 
                WHERE di.is_deleted = 0 AND su.is_deleted = 0
                GROUP BY di.department
                ORDER BY doctor_count DESC
            """)
            
            departments = cursor.fetchall()
            
            # 为每个科室添加MongoDB咨询统计
            consultation_collection = mongo_db.consultation_chats
            for dept in departments:
                dept_name = dept['department']
                
                # 查询该科室所有医生的用户ID
                cursor.execute("""
                    SELECT su.id as user_id
                    FROM doctor_info di 
                    INNER JOIN sys_user su ON di.user_id = su.id 
                    WHERE di.department = %s AND di.is_deleted = 0
                """, (dept_name,))
                
                doctor_user_ids = [row['user_id'] for row in cursor.fetchall()]
                
                if doctor_user_ids:
                    # 统计该科室的总咨询数
                    dept_consultations = consultation_collection.count_documents({
                        "doctorId": {"$in": doctor_user_ids}
                    })
                    dept["total_consultations"] = dept_consultations
                else:
                    dept["total_consultations"] = 0
            
            result = {
                "departments": departments,
                "total_departments": len(departments)
            }
        
        return json.dumps(result, ensure_ascii=False, default=str)
        
    except Exception as err:
        return json.dumps({"error": f"查询失败: {err}"})
    finally:
        if mysql_conn and mysql_conn.is_connected():
            cursor.close()
            mysql_conn.close()
        if mongo_client:
            mongo_client.close()

@mcp.tool()
def query_system_overview(days: int = 7) -> str:
    """
    查询系统总体概览数据(MySQL+MongoDB联合查询)

    Args:
        days: 统计最近多少天的活跃数据 (默认7天)

    Returns:
        JSON格式的系统概览数据
    """
    
    mysql_conn = get_mysql_connection()
    if mysql_conn is None:
        return json.dumps({"error": "MySQL数据库连接失败"})
    
    mongo_client, mongo_db = get_mongodb_connection()
    if mongo_client is None or mongo_db is None:
        return json.dumps({"error": "MongoDB数据库连接失败"})
    
    try:
        cursor = mysql_conn.cursor(dictionary=True)
        
        result = {"overview_period_days": days}
        
        # 用户统计 (MySQL)
        cursor.execute("""
            SELECT 
                COUNT(*) as total_users,
                SUM(CASE WHEN user_type = 0 THEN 1 ELSE 0 END) as patients,
                SUM(CASE WHEN user_type = 1 THEN 1 ELSE 0 END) as doctors,
                SUM(CASE WHEN user_type = 2 THEN 1 ELSE 0 END) as admins,
                SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as active_users
            FROM sys_user 
            WHERE is_deleted = 0
        """)
        result["users"] = cursor.fetchone()
        
        # 医生统计 (MySQL)
        cursor.execute("""
            SELECT 
                COUNT(*) as total_doctors,
                SUM(CASE WHEN di.online_status = 1 THEN 1 ELSE 0 END) as online_doctors,
                SUM(CASE WHEN COALESCE(di.status, 1) = 1 THEN 1 ELSE 0 END) as available_doctors,
                AVG(di.rating) as avg_rating,
                COUNT(DISTINCT di.department) as departments
            FROM doctor_info di 
            INNER JOIN sys_user su ON di.user_id = su.id 
            WHERE di.is_deleted = 0 AND su.is_deleted = 0
        """)
        result["doctors"] = cursor.fetchone()
        
        # 科室分布 (MySQL)
        cursor.execute("""
            SELECT department, COUNT(*) as count
            FROM doctor_info 
            WHERE is_deleted = 0 
            GROUP BY department 
            ORDER BY count DESC
        """)
        result["departments"] = cursor.fetchall()
        
        # MongoDB咨询统计
        consultation_collection = mongo_db.consultation_chats
        start_date_str = (datetime.now() - timedelta(days=days)).isoformat()
        
        # 总咨询数
        total_consultations = consultation_collection.count_documents({})
        
        # 最近N天的咨询数 - 使用字符串时间比较
        recent_consultations = consultation_collection.count_documents({
            "createTime": {"$gte": start_date_str}
        })
        
        # 按状态统计咨询
        consultation_by_status = {}
        for status, name in [(1, "pending"), (2, "ongoing"), (3, "completed"), (4, "cancelled")]:
            count = consultation_collection.count_documents({"status": status})
            consultation_by_status[name] = count
        
        result["consultations"] = {
            "total": total_consultations,
            "recent": recent_consultations,
            "by_status": consultation_by_status
        }
        
        # MongoDB健康记录统计
        glucose_collection = mongo_db.blood_glucose_records
        pressure_collection = mongo_db.blood_pressure_records
        weight_collection = mongo_db.weight_records
        
        result["health_records"] = {
            "glucose_records": glucose_collection.count_documents({}),
            "pressure_records": pressure_collection.count_documents({}),
            "weight_records": weight_collection.count_documents({})
        }
        
        return json.dumps(result, ensure_ascii=False, default=str)
        
    except Exception as err:
        return json.dumps({"error": f"查询失败: {err}"})
    finally:
        if mysql_conn and mysql_conn.is_connected():
            cursor.close()
            mysql_conn.close()
        if mongo_client:
            mongo_client.close()

@mcp.tool()
def search_doctors_by_condition(symptoms: str, department: str = "", limit: int = 10) -> str:
    """
    根据症状或条件推荐合适的医生(MySQL+MongoDB联合查询)

    Args:
        symptoms: 症状描述
        department: 偏好科室 (可选)
        limit: 限制返回医生数量 (默认10个)

    Returns:
        JSON格式的推荐医生信息(包含实时咨询统计)
    """
    
    mysql_conn = get_mysql_connection()
    if mysql_conn is None:
        return json.dumps({"error": "MySQL数据库连接失败"})
    
    mongo_client, mongo_db = get_mongodb_connection()
    if mongo_client is None or mongo_db is None:
        return json.dumps({"error": "MongoDB数据库连接失败"})
    
    try:
        cursor = mysql_conn.cursor(dictionary=True)
        
        # 基于症状关键词推荐科室
        symptoms_lower = symptoms.lower()
        recommended_dept = ""
        confidence_score = 0.0
        
        # 更详细的症状匹配
        if any(keyword in symptoms_lower for keyword in ['血糖', '糖尿病', '血压', '高血压', '内分泌', '甲状腺']):
            recommended_dept = "内分泌科"
            confidence_score = 0.9
        elif any(keyword in symptoms_lower for keyword in ['心脏', '胸痛', '心律', '心悸', '心慌']):
            recommended_dept = "心血管科" 
            confidence_score = 0.85
        elif any(keyword in symptoms_lower for keyword in ['头痛', '神经', '记忆', '失眠', '焦虑']):
            recommended_dept = "神经科"
            confidence_score = 0.8
        elif any(keyword in symptoms_lower for keyword in ['皮肤', '过敏', '湿疹', '瘙痒']):
            recommended_dept = "皮肤科"
            confidence_score = 0.85
        elif any(keyword in symptoms_lower for keyword in ['咳嗽', '胸闷', '呼吸', '肺']):
            recommended_dept = "呼吸科"
            confidence_score = 0.8
        elif any(keyword in symptoms_lower for keyword in ['胃', '肠', '腹痛', '消化']):
            recommended_dept = "消化科"
            confidence_score = 0.8
        
        # 如果用户指定了科室，优先使用用户指定的
        if department:
            recommended_dept = department
            confidence_score = 1.0
        
        # 查询推荐科室的医生
        where_conditions = ["di.is_deleted = 0", "su.is_deleted = 0", "su.status = 1"]
        params = []
        
        if recommended_dept:
            where_conditions.append("di.department = %s")
            params.append(recommended_dept)
        
        where_clause = " AND ".join(where_conditions)
        
        query = f"""
            SELECT 
                di.id, di.doctor_no, di.department, di.title, 
                di.work_years as experience, di.introduction, di.speciality,
                di.online_status, di.rating, di.consultation_count,
                su.id as user_id, su.real_name, su.username
            FROM doctor_info di 
            INNER JOIN sys_user su ON di.user_id = su.id 
            WHERE {where_clause}
            ORDER BY di.online_status DESC, di.rating DESC, di.consultation_count DESC
            LIMIT {limit}
        """
        
        cursor.execute(query, params)
        doctors = cursor.fetchall()
        
        # 为每个医生添加MongoDB中的实时咨询统计
        consultation_collection = mongo_db.consultation_chats
        today_start = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
        
        for doctor in doctors:
            user_id = doctor['user_id']
            
            # 实时咨询统计
            total_consultations = consultation_collection.count_documents({"doctorId": user_id})
            ongoing_consultations = consultation_collection.count_documents({
                "doctorId": user_id, 
                "status": {"$in": [1, 2]}
            })
            
            doctor["real_consultation_count"] = total_consultations
            doctor["ongoing_consultations"] = ongoing_consultations
            doctor["availability_score"] = 1.0 - (ongoing_consultations / 10.0)  # 可用性评分
        
        result = {
            "symptoms": symptoms,
            "recommended_department": recommended_dept,
            "confidence_score": confidence_score,
            "recommended_doctors": doctors,
            "total_found": len(doctors),
            "query_params": {"limit": limit}
        }
        
        return json.dumps(result, ensure_ascii=False, default=str)
        
    except Exception as err:
        return json.dumps({"error": f"查询失败: {err}"})
    finally:
        if mysql_conn and mysql_conn.is_connected():
            cursor.close()
            mysql_conn.close()
        if mongo_client:
            mongo_client.close()

@mcp.tool()
def query_consultation_detail(consultation_id: str = "", consultation_no: str = "", limit: int = 50) -> str:
    """
    查询具体咨询的详细信息(MongoDB+MySQL联合查询)

    Args:
        consultation_id: MongoDB中的咨询ID (可选)
        consultation_no: 咨询编号 (可选)
        limit: 限制返回消息数量 (默认50条)

    Returns:
        JSON格式的咨询详细信息
    """
    
    mysql_conn = get_mysql_connection()
    if mysql_conn is None:
        return json.dumps({"error": "MySQL数据库连接失败"})
    
    mongo_client, mongo_db = get_mongodb_connection()
    if mongo_client is None or mongo_db is None:
        return json.dumps({"error": "MongoDB数据库连接失败"})
    
    try:
        cursor = mysql_conn.cursor(dictionary=True)
        
        # 构建MongoDB查询条件
        mongo_query = {}
        if consultation_id:
            try:
                mongo_query["_id"] = ObjectId(consultation_id)
            except:
                return json.dumps({"error": "无效的咨询ID格式"})
        elif consultation_no:
            mongo_query["consultationNo"] = consultation_no
        else:
            return json.dumps({"error": "必须提供咨询ID或咨询编号"})
        
        # 查询咨询基本信息 (MongoDB)
        consultation_collection = mongo_db.consultation_chats
        consultation = consultation_collection.find_one(mongo_query)
        
        if not consultation:
            return json.dumps({"error": "咨询记录不存在"})
        
        # 转换数据格式
        consultation["_id"] = str(consultation["_id"])
        if "createTime" in consultation:
            consultation["createTime"] = consultation["createTime"].isoformat()
        if "endTime" in consultation:
            consultation["endTime"] = consultation["endTime"].isoformat()
        
        # 查询患者信息 (MySQL)
        patient_id = consultation.get("patientId")
        doctor_id = consultation.get("doctorId")
        
        patient_info = None
        doctor_info = None
        
        if patient_id:
            cursor.execute("""
                SELECT id, real_name, phone, email, gender
                FROM sys_user 
                WHERE id = %s AND is_deleted = 0
            """, (patient_id,))
            patient_info = cursor.fetchone()
        
        if doctor_id:
            cursor.execute("""
                SELECT 
                    su.id as user_id, su.real_name, su.phone, su.email,
                    di.department, di.title, di.doctor_no, di.rating
                FROM sys_user su
                INNER JOIN doctor_info di ON su.id = di.user_id
                WHERE su.id = %s AND su.is_deleted = 0
            """, (doctor_id,))
            doctor_info = cursor.fetchone()
        
        # 查询消息记录 (MongoDB)
        messages = list(consultation.get("messages", []))
        if len(messages) > limit:
            messages = messages[-limit:]  # 获取最新的limit条消息
        
        result = {
            "consultation": consultation,
            "patient_info": patient_info,
            "doctor_info": doctor_info,
            "messages": messages,
            "message_count": len(consultation.get("messages", [])),
            "shown_messages": len(messages),
            "query_params": {"limit": limit}
        }
        
        return json.dumps(result, ensure_ascii=False, default=str)
        
    except Exception as err:
        return json.dumps({"error": f"查询失败: {err}"})
    finally:
        if mysql_conn and mysql_conn.is_connected():
            cursor.close()
            mysql_conn.close()
        if mongo_client:
            mongo_client.close()

def update_statistics(collection, user_id: int, record_type: str):
    """
    重新计算并更新用户健康记录的统计信息（内部辅助函数）
    
    Args:
        collection: MongoDB集合对象
        user_id: 用户ID
        record_type: 记录类型 (glucose, pressure, weight)
    """
    # 获取用户的所有记录
    user_doc = collection.find_one({"userId": user_id})
    if not user_doc or "records" not in user_doc:
        return
    
    records = user_doc.get("records", [])
    if not records:
        return
    
    statistics = {}
    now = datetime.now().isoformat()
    
    if record_type == "glucose":
        # 血糖统计
        values = [float(r.get("value", 0)) for r in records if r.get("value")]
        if values:
            avg_value = sum(values) / len(values)
            max_value = max(values)
            min_value = min(values)
            
            # 统计正常、偏高、偏低的记录数
            normal_count = 0
            high_count = 0
            low_count = 0
            
            for value in values:
                if value < 3.9:
                    low_count += 1
                elif value <= 6.1:  # 使用空腹血糖正常范围
                    normal_count += 1
                else:
                    high_count += 1
            
            statistics = {
                "avgValue": round(avg_value, 2),
                "maxValue": round(max_value, 2),
                "minValue": round(min_value, 2),
                "totalCount": len(records),
                "normalCount": normal_count,
                "highCount": high_count,
                "lowCount": low_count,
                "lastUpdateTime": now
            }
    
    elif record_type == "pressure":
        # 血压统计
        systolic_values = [float(r.get("systolic", 0)) for r in records if r.get("systolic")]
        diastolic_values = [float(r.get("diastolic", 0)) for r in records if r.get("diastolic")]
        heart_rates = [float(r.get("heartRate", 0)) for r in records if r.get("heartRate")]
        
        if systolic_values and diastolic_values:
            avg_systolic = sum(systolic_values) / len(systolic_values)
            avg_diastolic = sum(diastolic_values) / len(diastolic_values)
            avg_heart_rate = sum(heart_rates) / len(heart_rates) if heart_rates else 0
            
            # 统计正常、偏高、偏低的记录数
            normal_count = 0
            high_count = 0
            low_count = 0
            
            for sys, dia in zip(systolic_values, diastolic_values):
                if sys < 90 or dia < 60:
                    low_count += 1
                elif sys <= 120 and dia <= 80:
                    normal_count += 1
                else:
                    high_count += 1
            
            statistics = {
                "avgSystolic": round(avg_systolic, 2),
                "avgDiastolic": round(avg_diastolic, 2),
                "avgHeartRate": round(avg_heart_rate, 2) if heart_rates else 0,
                "totalCount": len(records),
                "normalCount": normal_count,
                "highCount": high_count,
                "lowCount": low_count,
                "lastUpdateTime": now
            }
    
    elif record_type == "weight":
        # 体重统计
        weights = [float(r.get("weight", 0)) for r in records if r.get("weight")]
        
        if weights:
            avg_weight = sum(weights) / len(weights)
            current_weight = weights[-1] if weights else 0  # 最后一条记录的体重
            
            # 计算BMI（如果有身高信息）
            current_bmi = 0
            last_record = records[-1] if records else {}
            if last_record.get("height"):
                try:
                    height_m = float(last_record["height"]) / 100
                    current_bmi = current_weight / (height_m ** 2)
                except:
                    pass
            
            # 计算7天和30天的体重变化
            weight_change_7days = 0
            weight_change_30days = 0
            
            now_dt = datetime.now()
            seven_days_ago = now_dt - timedelta(days=7)
            thirty_days_ago = now_dt - timedelta(days=30)
            
            for r in records:
                measure_time_str = r.get("measureTime", "")
                try:
                    measure_time = datetime.fromisoformat(measure_time_str.replace('Z', '+00:00'))
                    if measure_time >= seven_days_ago and weight_change_7days == 0:
                        weight_change_7days = current_weight - float(r.get("weight", current_weight))
                    if measure_time >= thirty_days_ago and weight_change_30days == 0:
                        weight_change_30days = current_weight - float(r.get("weight", current_weight))
                except:
                    pass
            
            statistics = {
                "avgWeight": round(avg_weight, 2),
                "currentWeight": round(current_weight, 2),
                "currentBmi": round(current_bmi, 2) if current_bmi > 0 else 0,
                "weightChange7Days": round(weight_change_7days, 2),
                "weightChange30Days": round(weight_change_30days, 2),
                "totalCount": len(records),
                "lastUpdateTime": now
            }
    
    # 更新统计信息到MongoDB
    if statistics:
        collection.update_one(
            {"userId": user_id},
            {
                "$set": {
                    "statistics": statistics,
                    "updatedAt": now
                }
            }
        )


@mcp.tool()
def add_health_record(user_id: int, record_type: str, record_data: str, measure_time: str = "") -> str:
    """
    向MongoDB中添加用户健康记录
    
    Args:
        user_id: 用户ID
        record_type: 记录类型 (glucose, pressure, weight)
        record_data: 记录数据JSON字符串，例如: {"value": 6.8, "measureType": "餐后血糖"}
        measure_time: 测量时间 (ISO格式，为空则使用当前时间)
    
    Returns:
        JSON格式的操作结果
    """
    
    # 验证记录类型
    valid_types = ["glucose", "pressure", "weight"]
    if record_type not in valid_types:
        return json.dumps({"error": f"无效的记录类型，支持的类型: {valid_types}"})
    
    # 验证用户ID
    if not isinstance(user_id, int) or user_id <= 0:
        return json.dumps({"error": "无效的用户ID"})
    
    # 验证和解析记录数据
    try:
        if isinstance(record_data, str):
            record_data = json.loads(record_data)
        elif not isinstance(record_data, dict):
            return json.dumps({"error": "记录数据必须是JSON字符串或字典格式"})
    except json.JSONDecodeError:
        return json.dumps({"error": "记录数据JSON格式无效"})
    
    mysql_conn = get_mysql_connection()
    if mysql_conn is None:
        return json.dumps({"error": "MySQL数据库连接失败"})
    
    mongo_client, mongo_db = get_mongodb_connection()
    if mongo_client is None or mongo_db is None:
        return json.dumps({"error": "MongoDB数据库连接失败"})
    
    try:
        cursor = mysql_conn.cursor(dictionary=True)
        
        # 验证用户是否存在 (MySQL)
        cursor.execute("""
            SELECT id, real_name, user_type
            FROM sys_user 
            WHERE id = %s AND is_deleted = 0
        """, (user_id,))
        
        user_info = cursor.fetchone()
        if not user_info:
            return json.dumps({"error": "用户不存在"})
        
        # 处理测量时间
        if measure_time:
            try:
                # 验证时间格式
                parsed_time = datetime.fromisoformat(measure_time.replace('Z', '+00:00'))
                measure_time_str = parsed_time.isoformat()
            except Exception:
                return json.dumps({"error": "无效的时间格式，请使用ISO格式"})
        else:
            measure_time_str = datetime.now().isoformat()
        
        # 根据记录类型验证和处理数据
        if record_type == "glucose":
            # 验证血糖记录必需字段
            required_fields = ["value"]
            for field in required_fields:
                if field not in record_data:
                    return json.dumps({"error": f"血糖记录缺少必需字段: {field}"})
            
            # 验证血糖值范围 (通常2.0-30.0 mmol/L)
            glucose_value = record_data.get("value")
            try:
                glucose_value = float(glucose_value)
                if glucose_value < 1.0 or glucose_value > 50.0:
                    return json.dumps({"error": "血糖值超出合理范围 (1.0-50.0 mmol/L)"})
            except (TypeError, ValueError):
                return json.dumps({"error": "血糖值必须是数字"})
            
            # 构建血糖记录
            new_record = {
                "id": str(uuid.uuid4()),  # 生成UUID作为记录ID
                "value": glucose_value,
                "measureTime": measure_time_str,
                "unit": record_data.get("unit", "mmol/L"),
                "note": record_data.get("notes", ""),  # 使用note字段名（与后端一致）
                "measureType": record_data.get("measureType", "随机血糖"),
                "mealType": record_data.get("mealType", ""),  # 添加餐次字段
                "createdAt": datetime.now().isoformat()
            }
            
            collection_name = "blood_glucose_records"
            
        elif record_type == "pressure":
            # 验证血压记录必需字段
            required_fields = ["systolic", "diastolic"]
            for field in required_fields:
                if field not in record_data:
                    return json.dumps({"error": f"血压记录缺少必需字段: {field}"})
            
            # 验证血压值范围
            try:
                systolic = float(record_data["systolic"])
                diastolic = float(record_data["diastolic"])
                
                if systolic < 50 or systolic > 300:
                    return json.dumps({"error": "收缩压超出合理范围 (50-300 mmHg)"})
                if diastolic < 30 or diastolic > 200:
                    return json.dumps({"error": "舒张压超出合理范围 (30-200 mmHg)"})
                if systolic <= diastolic:
                    return json.dumps({"error": "收缩压应该大于舒张压"})
                    
            except (TypeError, ValueError):
                return json.dumps({"error": "血压值必须是数字"})
            
            # 构建血压记录
            new_record = {
                "id": str(uuid.uuid4()),  # 生成UUID作为记录ID
                "systolic": systolic,
                "diastolic": diastolic,
                "measureTime": measure_time_str,
                "unit": record_data.get("unit", "mmHg"),
                "heartRate": record_data.get("heartRate"),
                "note": record_data.get("notes", ""),  # 使用note字段名（与后端一致）
                "measureState": record_data.get("measureState", ""),  # 添加测量状态字段
                "createdAt": datetime.now().isoformat()
            }
            
            collection_name = "blood_pressure_records"
            
        elif record_type == "weight":
            # 验证体重记录必需字段（支持value或weight字段名）
            weight_value = record_data.get("value") or record_data.get("weight")
            if weight_value is None:
                return json.dumps({"error": "体重记录缺少必需字段: value 或 weight"})
            
            # 验证体重值范围 (通常20-500 kg)
            try:
                weight_value = float(weight_value)
                if weight_value < 10.0 or weight_value > 1000.0:
                    return json.dumps({"error": "体重值超出合理范围 (10.0-1000.0 kg)"})
            except (TypeError, ValueError):
                return json.dumps({"error": "体重值必须是数字"})
            
            # 构建体重记录
            new_record = {
                "id": str(uuid.uuid4()),  # 生成UUID作为记录ID
                "weight": weight_value,  # 使用weight字段名（与后端一致）
                "measureTime": measure_time_str,
                "unit": record_data.get("unit", "kg"),
                "note": record_data.get("notes", ""),  # 使用note字段名（与后端一致）
                "height": record_data.get("height"),  # 身高（用于计算BMI）
                "measureState": record_data.get("measureState", ""),  # 添加测量状态字段
                "isManualInput": record_data.get("isManualInput", True),  # 默认为手动输入
                "deviceType": record_data.get("deviceType", "manual"),  # 设备类型
                "createdAt": datetime.now().isoformat()
            }
            
            # 如果提供了身高，计算BMI
            if new_record["height"]:
                try:
                    height_m = float(new_record["height"]) / 100  # 转换为米
                    bmi = weight_value / (height_m ** 2)
                    new_record["bmi"] = round(bmi, 2)
                except:
                    pass  # BMI计算失败不影响记录添加
            
            collection_name = "weight_records"
        
        # 插入MongoDB记录
        collection = mongo_db[collection_name]
        
        # 查找用户的健康记录文档
        user_doc = collection.find_one({"userId": user_id})
        
        if user_doc:
            # 用户文档已存在，添加新记录到records数组
            result_update = collection.update_one(
                {"userId": user_id},
                {"$push": {"records": new_record}}
            )
            
            if result_update.modified_count > 0:
                operation_result = "记录添加成功"
            else:
                return json.dumps({"error": "记录添加失败"})
                
        else:
            # 用户文档不存在，创建新文档
            new_doc = {
                "userId": user_id,
                "records": [new_record],
                "createdAt": datetime.now().isoformat(),
                "updatedAt": datetime.now().isoformat()
            }
            
            result_insert = collection.insert_one(new_doc)
            
            if result_insert.inserted_id:
                operation_result = "用户健康记录文档创建成功，记录已添加"
            else:
                return json.dumps({"error": "记录添加失败"})
        
        # 关键修复：重新计算并更新统计信息
        update_statistics(collection, user_id, record_type)
        
        # 查询用户当前记录统计
        updated_doc = collection.find_one({"userId": user_id})
        total_records = len(updated_doc.get("records", []))
        
        result = {
            "success": True,
            "message": operation_result,
            "user_info": {
                "user_id": user_id,
                "real_name": user_info["real_name"]
            },
            "record_details": {
                "type": record_type,
                "data": new_record,
                "total_records_count": total_records
            },
            "operation_time": datetime.now().isoformat()
        }
        
        return json.dumps(result, ensure_ascii=False, default=str)
        
    except Exception as err:
        return json.dumps({"error": f"添加记录失败: {err}"})
    finally:
        if mysql_conn and mysql_conn.is_connected():
            cursor.close()
            mysql_conn.close()
        if mongo_client:
            mongo_client.close()


# ==================== RAG 检索工具 ====================

def check_rag_service_health():
    """检查RAG服务健康状态"""
    try:
        response = requests.get(
            f"{RAG_API_CONFIG['base_url']}/health", 
            timeout=5
        )
        response.raise_for_status()
        return response.json()
    except Exception as e:
        return {"error": f"RAG服务连接失败: {str(e)}"}

@mcp.tool()
def rag_health_check() -> str:
    """
    检查RAG检索服务的健康状态
    
    Returns:
        RAG服务的健康状态信息(JSON格式)
    """
    try:
        health_info = check_rag_service_health()
        
        if "error" in health_info:
            return json.dumps({
                "service_status": "unavailable",
                "error": health_info["error"],
                "timestamp": datetime.now().isoformat()
            }, ensure_ascii=False)
        
        result = {
            "service_status": health_info.get("status", "unknown"),
            "model_loaded": health_info.get("model_loaded", False),
            "database_connected": health_info.get("database_connected", False),
            "cache_connected": health_info.get("cache_connected", False),
            "total_documents": health_info.get("total_documents", 0),
            "uptime_seconds": health_info.get("uptime", 0),
            "gpu_available": health_info.get("gpu_available", False),
            "memory_usage": health_info.get("memory_usage", {}),
            "timestamp": datetime.now().isoformat()
        }
        
        return json.dumps(result, ensure_ascii=False)
        
    except Exception as e:
        return json.dumps({
            "service_status": "error",
            "error": f"健康检查失败: {str(e)}",
            "timestamp": datetime.now().isoformat()
        }, ensure_ascii=False)

@mcp.tool()
def search_diabetes_knowledge(
    query: str, 
    top_k: int = 5, 
    similarity_threshold: float = 0.0,
    use_cache: bool = True,
    include_entities: bool = True,
    category_filter: str = ""
) -> str:
    """
    搜索糖尿病知识库，获取相关医学信息
    
    Args:
        query: 查询问题，如"糖尿病的主要症状有哪些？"
        top_k: 返回结果数量，默认5条 (1-20)
        similarity_threshold: 相似度阈值，默认0.0 (0.0-1.0)
        use_cache: 是否使用缓存，默认True
        include_entities: 是否包含医学实体信息，默认True
        category_filter: 分类过滤器，多个分类用逗号分隔，如"眼部疾病,神经疾病"
        
    Returns:
        JSON格式的搜索结果
    """
    
    if not query or not query.strip():
        return json.dumps({
            "success": False,
            "error": "查询内容不能为空",
            "timestamp": datetime.now().isoformat()
        }, ensure_ascii=False)
    
    try:
        # 准备请求数据
        request_data = {
            "query": query.strip(),
            "top_k": max(1, min(top_k, 20)),  # 限制在1-20之间
            "similarity_threshold": max(0.0, min(similarity_threshold, 1.0)),  # 限制在0-1之间
            "use_cache": use_cache,
            "include_entities": include_entities
        }
        
        # 处理分类过滤器
        if category_filter and category_filter.strip():
            categories = [cat.strip() for cat in category_filter.split(",") if cat.strip()]
            if categories:
                request_data["category_filter"] = categories
        
        # 调用RAG API
        start_time = time.time()
        response = requests.post(
            f"{RAG_API_CONFIG['base_url']}/search",
            json=request_data,
            timeout=RAG_API_CONFIG['timeout']
        )
        response.raise_for_status()
        api_response_time = time.time() - start_time
        
        search_result = response.json()
        
        if not search_result.get("success", False):
            return json.dumps({
                "success": False,
                "error": f"RAG API调用失败: {search_result.get('error', '未知错误')}",
                "timestamp": datetime.now().isoformat()
            }, ensure_ascii=False)
        
        # 格式化返回结果
        formatted_results = []
        for result in search_result.get("results", []):
            formatted_result = {
                "rank": result.get("rank", 0),
                "question": result.get("question", ""),
                "answer": result.get("answer", ""),
                "category": result.get("category", ""),
                "similarity_score": result.get("similarity", 0.0),
                "medical_entities": result.get("entities", []) if include_entities else None,
                "source_info": result.get("source_info", {})
            }
            formatted_results.append(formatted_result)
        
        # 构建完整响应
        final_result = {
            "success": True,
            "query": query.strip(),
            "search_results": formatted_results,
            "search_summary": {
                "total_found": search_result.get("total_found", 0),
                "returned_count": len(formatted_results),
                "cache_hit": search_result.get("cache_hit", False),
                "search_time_seconds": search_result.get("search_time", 0),
                "api_response_time_seconds": round(api_response_time, 3)
            },
            "search_parameters": {
                "top_k": request_data["top_k"],
                "similarity_threshold": request_data["similarity_threshold"],
                "use_cache": request_data["use_cache"],
                "include_entities": request_data["include_entities"],
                "category_filter": request_data.get("category_filter", [])
            },
            "timestamp": datetime.now().isoformat()
        }
        
        return json.dumps(final_result, ensure_ascii=False)
        
    except requests.exceptions.Timeout:
        return json.dumps({
            "success": False,
            "error": f"RAG服务响应超时 (>{RAG_API_CONFIG['timeout']}秒)",
            "timestamp": datetime.now().isoformat()
        }, ensure_ascii=False)
        
    except requests.exceptions.ConnectionError:
        return json.dumps({
            "success": False,
            "error": f"无法连接到RAG服务 ({RAG_API_CONFIG['base_url']})",
            "suggestion": "请确保RAG API服务正在运行",
            "timestamp": datetime.now().isoformat()
        }, ensure_ascii=False)
        
    except requests.exceptions.RequestException as e:
        return json.dumps({
            "success": False,
            "error": f"RAG API请求失败: {str(e)}",
            "timestamp": datetime.now().isoformat()
        }, ensure_ascii=False)
        
    except Exception as e:
        return json.dumps({
            "success": False,
            "error": f"糖尿病知识搜索失败: {str(e)}",
            "timestamp": datetime.now().isoformat()
        }, ensure_ascii=False)

@mcp.tool()
def get_diabetes_knowledge_categories() -> str:
    """
    获取糖尿病知识库的所有可用分类
    
    Returns:
        JSON格式的分类信息
    """
    try:
        # 通过统计接口获取分类信息
        response = requests.get(
            f"{RAG_API_CONFIG['base_url']}/stats",
            timeout=10
        )
        response.raise_for_status()
        stats_data = response.json()
        
        # 从配置或已知信息返回分类
        available_categories = {
            "眼部疾病": "糖尿病视网膜病变等眼部并发症相关信息",
            "神经疾病": "糖尿病神经病变相关信息", 
            "基础知识": "糖尿病基础概念、定义等",
            "治疗方法": "糖尿病治疗方案、药物治疗等",
            "诊断检查": "糖尿病诊断标准、检查方法等",
            "其他": "其他糖尿病相关信息"
        }
        
        result = {
            "success": True,
            "available_categories": available_categories,
            "total_documents": stats_data.get("total_documents", 0),
            "service_status": stats_data.get("service_status", "unknown"),
            "usage_tips": [
                "可以在search_diabetes_knowledge中使用category_filter参数",
                "多个分类用逗号分隔，如: '眼部疾病,神经疾病'",
                "不指定分类则搜索所有分类的内容"
            ],
            "timestamp": datetime.now().isoformat()
        }
        
        return json.dumps(result, ensure_ascii=False)
        
    except Exception as e:
        # 即使API不可用，也返回已知的分类信息
        fallback_result = {
            "success": True,
            "available_categories": {
                "眼部疾病": "糖尿病视网膜病变等眼部并发症相关信息",
                "神经疾病": "糖尿病神经病变相关信息", 
                "基础知识": "糖尿病基础概念、定义等",
                "治疗方法": "糖尿病治疗方案、药物治疗等",
                "诊断检查": "糖尿病诊断标准、检查方法等",
                "其他": "其他糖尿病相关信息"
            },
            "note": f"从RAG服务获取统计信息失败: {str(e)}",
            "timestamp": datetime.now().isoformat()
        }
        
        return json.dumps(fallback_result, ensure_ascii=False)

@mcp.tool()
def clear_rag_cache() -> str:
    """
    清理RAG检索服务的缓存
    
    Returns:
        缓存清理结果
    """
    try:
        response = requests.post(
            f"{RAG_API_CONFIG['base_url']}/cache/clear",
            timeout=10
        )
        response.raise_for_status()
        clear_result = response.json()
        
        result = {
            "success": clear_result.get("success", False),
            "message": clear_result.get("message", "缓存清理完成"),
            "timestamp": datetime.now().isoformat()
        }
        
        return json.dumps(result, ensure_ascii=False)
        
    except Exception as e:
        return json.dumps({
            "success": False,
            "error": f"清理缓存失败: {str(e)}",
            "timestamp": datetime.now().isoformat()
        }, ensure_ascii=False)


if __name__ == "__main__":
    # Get transport type from environment variable, default to SSE
    transport_type = os.getenv('MCP_TRANSPORT', 'sse')
    mcp.run(transport=transport_type)
