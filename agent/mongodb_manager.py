"""
MongoDB管理器 - 管理AI咨询记录的持久化存储
"""

import logging
from typing import Dict, List, Any, Optional
from datetime import datetime
from pymongo import MongoClient, DESCENDING
from pymongo.errors import ConnectionFailure, DuplicateKeyError
from config import MONGODB_CONFIG

logger = logging.getLogger(__name__)

class MongoDBManager:
    """MongoDB管理器"""
    
    def __init__(self):
        """初始化MongoDB连接"""
        try:
            # 构建连接URI
            if MONGODB_CONFIG.get('username') and MONGODB_CONFIG.get('password'):
                uri = f"mongodb://{MONGODB_CONFIG['username']}:{MONGODB_CONFIG['password']}@{MONGODB_CONFIG['host']}:{MONGODB_CONFIG['port']}/{MONGODB_CONFIG['database']}?authSource=admin"
            else:
                uri = f"mongodb://{MONGODB_CONFIG['host']}:{MONGODB_CONFIG['port']}/{MONGODB_CONFIG['database']}"
            
            self.client = MongoClient(uri, serverSelectionTimeoutMS=5000)
            # 测试连接
            self.client.admin.command('ping')
            
            self.db = self.client[MONGODB_CONFIG['database']]
            self.consultation_collection = self.db['ai_consultation_history']
            
            logger.info(f"✅ MongoDB连接成功: {MONGODB_CONFIG['database']}")
            
        except ConnectionFailure as e:
            logger.error(f"❌ MongoDB连接失败: {e}")
            raise
        except Exception as e:
            logger.error(f"❌ MongoDB初始化失败: {e}")
            raise
    
    def _generate_session_id(self, user_id: int) -> str:
        """生成会话ID"""
        timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
        return f"ai_session_{timestamp}_user{user_id}"
    
    async def create_session(self, user_id: int, user_info: Dict[str, Any], session_id: str = None) -> str:
        """
        创建新会话
        
        Args:
            user_id: 用户ID
            user_info: 用户信息
            session_id: 可选的会话ID，如果不提供则自动生成
            
        Returns:
            会话ID
        """
        try:
            if not session_id:
                session_id = self._generate_session_id(user_id)
            now = datetime.now()
            
            session_doc = {
                "sessionId": session_id,
                "userId": user_id,
                "userInfo": {
                    "username": user_info.get("username", ""),
                    "realName": user_info.get("real_name", ""),
                    "gender": user_info.get("gender", "")
                },
                "messages": [],
                "context": {},
                "statistics": {
                    "totalMessages": 0,
                    "userMessages": 0,
                    "assistantMessages": 0,
                    "expertExecutions": 0,
                    "mcpCalls": 0
                },
                "createTime": now,
                "updateTime": now,
                "lastAccessTime": now
            }
            
            result = self.consultation_collection.insert_one(session_doc)
            logger.info(f"📝 创建新会话: {session_id}")
            
            return session_id
            
        except DuplicateKeyError:
            logger.warning(f"会话ID已存在，重新生成: {session_id}")
            # 递归调用，重新生成ID
            return await self.create_session(user_id, user_info)
        except Exception as e:
            logger.error(f"创建会话失败: {e}")
            raise
    
    async def get_session(self, user_id: int, session_id: Optional[str] = None) -> Optional[Dict[str, Any]]:
        """
        获取会话
        
        Args:
            user_id: 用户ID
            session_id: 会话ID（可选，不提供则获取最新会话）
            
        Returns:
            会话数据
        """
        try:
            if session_id:
                # 根据session_id获取
                query = {"sessionId": session_id, "userId": user_id}
            else:
                # 获取用户最新会话
                query = {"userId": user_id}
            
            session = self.consultation_collection.find_one(
                query,
                sort=[("updateTime", DESCENDING)]
            )
            
            if session:
                # 更新最后访问时间
                self.consultation_collection.update_one(
                    {"_id": session["_id"]},
                    {"$set": {"lastAccessTime": datetime.now()}}
                )
                
                # 移除MongoDB的_id字段
                session.pop("_id", None)
                
            return session
            
        except Exception as e:
            logger.error(f"获取会话失败: {e}")
            return None
    
    async def add_message(self, session_id: str, message_data: Dict[str, Any]) -> bool:
        """
        添加消息到会话
        
        Args:
            session_id: 会话ID
            message_data: 消息数据
            
        Returns:
            是否成功
        """
        try:
            # 生成消息ID
            message_id = f"msg_{datetime.now().strftime('%Y%m%d%H%M%S%f')}"
            message_data["messageId"] = message_id
            message_data["timestamp"] = message_data.get("timestamp", datetime.now())
            
            # 更新统计信息
            update_stats = {
                "statistics.totalMessages": 1
            }
            
            if message_data["role"] == "user":
                update_stats["statistics.userMessages"] = 1
            elif message_data["role"] == "assistant":
                update_stats["statistics.assistantMessages"] = 1
                
                # 统计专家执行和MCP调用
                if message_data.get("expertPlan"):
                    experts = message_data["expertPlan"].get("experts", [])
                    update_stats["statistics.expertExecutions"] = len(experts)
                    
                    # 统计MCP调用次数
                    mcp_call_count = 0
                    for expert in experts:
                        result = expert.get("result", {})
                        mcp_calls = result.get("mcpCalls", result.get("mcp_calls", []))
                        mcp_call_count += len(mcp_calls)
                    
                    if mcp_call_count > 0:
                        update_stats["statistics.mcpCalls"] = mcp_call_count
            
            result = self.consultation_collection.update_one(
                {"sessionId": session_id},
                {
                    "$push": {"messages": message_data},
                    "$inc": update_stats,
                    "$set": {"updateTime": datetime.now()}
                }
            )
            
            if result.modified_count > 0:
                logger.debug(f"✅ 消息已添加到会话: {session_id}")
                return True
            else:
                logger.warning(f"⚠️ 会话不存在: {session_id}")
                return False
                
        except Exception as e:
            logger.error(f"添加消息失败: {e}")
            return False
    
    async def update_context(self, session_id: str, context_key: str, context_value: Any) -> bool:
        """
        更新会话上下文
        
        Args:
            session_id: 会话ID
            context_key: 上下文键
            context_value: 上下文值
            
        Returns:
            是否成功
        """
        try:
            result = self.consultation_collection.update_one(
                {"sessionId": session_id},
                {
                    "$set": {
                        f"context.{context_key}": context_value,
                        "updateTime": datetime.now()
                    }
                }
            )
            
            return result.modified_count > 0
            
        except Exception as e:
            logger.error(f"更新上下文失败: {e}")
            return False
    
    async def get_conversation_history(
        self, 
        user_id: int, 
        session_id: Optional[str] = None,
        limit: int = 20
    ) -> List[Dict[str, Any]]:
        """
        获取对话历史
        
        Args:
            user_id: 用户ID
            session_id: 会话ID（可选）
            limit: 返回消息数量限制
            
        Returns:
            消息列表
        """
        try:
            session = await self.get_session(user_id, session_id)
            
            if not session:
                return []
            
            messages = session.get("messages", [])
            
            # 返回最近的消息
            recent_messages = messages[-limit:] if len(messages) > limit else messages
            
            return recent_messages
            
        except Exception as e:
            logger.error(f"获取对话历史失败: {e}")
            return []
    
    async def get_user_sessions(
        self, 
        user_id: int, 
        limit: int = 10
    ) -> List[Dict[str, Any]]:
        """
        获取用户的所有会话列表
        
        Args:
            user_id: 用户ID
            limit: 返回数量限制
            
        Returns:
            会话列表（简化信息）
        """
        try:
            sessions = self.consultation_collection.find(
                {"userId": user_id},
                {
                    "sessionId": 1,
                    "createTime": 1,
                    "updateTime": 1,
                    "statistics": 1,
                    "messages": {"$slice": -1}  # 只获取最后一条消息
                }
            ).sort("updateTime", DESCENDING).limit(limit)
            
            result = []
            for session in sessions:
                session.pop("_id", None)
                result.append(session)
            
            return result
            
        except Exception as e:
            logger.error(f"获取用户会话列表失败: {e}")
            return []
    
    async def delete_session(self, session_id: str, user_id: int) -> bool:
        """
        删除会话
        
        Args:
            session_id: 会话ID
            user_id: 用户ID（安全验证）
            
        Returns:
            是否成功
        """
        try:
            result = self.consultation_collection.delete_one({
                "sessionId": session_id,
                "userId": user_id
            })
            
            if result.deleted_count > 0:
                logger.info(f"🗑️ 会话已删除: {session_id}")
                return True
            else:
                logger.warning(f"⚠️ 会话不存在或无权限: {session_id}")
                return False
                
        except Exception as e:
            logger.error(f"删除会话失败: {e}")
            return False
    
    def close(self):
        """关闭MongoDB连接"""
        try:
            if self.client:
                self.client.close()
                logger.info("MongoDB连接已关闭")
        except Exception as e:
            logger.error(f"关闭MongoDB连接失败: {e}")

