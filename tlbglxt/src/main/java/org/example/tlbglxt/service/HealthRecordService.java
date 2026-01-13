package org.example.tlbglxt.service;

import org.example.tlbglxt.dto.request.health.AddBloodGlucoseRequest;
import org.example.tlbglxt.dto.request.health.AddBloodPressureRequest;
import org.example.tlbglxt.dto.request.health.AddWeightRequest;
import org.example.tlbglxt.dto.request.health.QueryHealthRecordRequest;
import org.example.tlbglxt.dto.response.health.BloodGlucoseRecordResponse;
import org.example.tlbglxt.dto.response.health.BloodPressureRecordResponse;
import org.example.tlbglxt.common.PageResult;


import java.util.List;

/**
 * 健康记录服务接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
public interface HealthRecordService {

    /**
     * 添加血糖记录
     *
     * @param userId 用户ID
     * @param request 血糖记录请求
     * @return 操作结果
     */
    Boolean addBloodGlucoseRecord(Long userId, AddBloodGlucoseRequest request);

    /**
     * 更新血糖记录
     *
     * @param userId 用户ID
     * @param recordId 记录ID（用于定位记录）
     * @param request 更新请求
     * @return 操作结果
     */
    Boolean updateBloodGlucoseRecord(Long userId, String recordId, AddBloodGlucoseRequest request);

    /**
     * 添加血压记录
     *
     * @param userId 用户ID
     * @param request 血压记录请求
     * @return 操作结果
     */
    Boolean addBloodPressureRecord(Long userId, AddBloodPressureRequest request);

    /**
     * 更新血压记录
     *
     * @param userId 用户ID
     * @param recordId 记录ID（用于定位记录）
     * @param request 更新请求
     * @return 操作结果
     */
    Boolean updateBloodPressureRecord(Long userId, String recordId, AddBloodPressureRequest request);

    /**
     * 获取血糖记录
     *
     * @param userId 用户ID
     * @param request 查询请求
     * @return 血糖记录响应
     */
    BloodGlucoseRecordResponse getBloodGlucoseRecords(Long userId, QueryHealthRecordRequest request);

    /**
     * 获取血压记录（分页）
     *
     * @param userId 用户ID
     * @param request 查询请求
     * @return 血压记录响应
     */
    BloodPressureRecordResponse getBloodPressureRecords(Long userId, QueryHealthRecordRequest request);

    /**
     * 批量删除血糖记录
     *
     * @param userId 用户ID
     * @param recordIds 记录ID列表
     * @return 操作结果
     */
    Boolean batchDeleteBloodGlucoseRecords(Long userId, List<String> recordIds);

    /**
     * 批量删除血压记录
     *
     * @param userId 用户ID
     * @param recordIds 记录ID列表
     * @return 操作结果
     */
    Boolean batchDeleteBloodPressureRecords(Long userId, List<String> recordIds);

    /**
     * 删除血糖记录
     *
     * @param userId 用户ID
     * @param recordId 记录ID
     * @return 操作结果
     */
    Boolean deleteBloodGlucoseRecord(Long userId, String recordId);

    /**
     * 删除血压记录
     *
     * @param userId 用户ID
     * @param recordId 记录ID
     * @return 操作结果
     */
    Boolean deleteBloodPressureRecord(Long userId, String recordId);

    /**
     * 获取用户健康统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Object getHealthStatistics(Long userId);

    /**
     * 获取用户健康数据趋势
     *
     * @param userId 用户ID
     * @param dataType 数据类型（glucose/pressure/weight）
     * @param days 天数
     * @return 趋势数据
     */
    Object getHealthDataTrend(Long userId, String dataType, Integer days);

    /**
     * 初始化用户健康档案
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    Boolean initUserHealthProfile(Long userId);
    
    /**
     * 🔧 重新计算血糖统计数据（修复统计错误）
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    Boolean recalculateBloodGlucoseStatistics(Long userId);

    // ===== 体重记录相关方法 =====

    /**
     * 添加体重记录
     *
     * @param userId 用户ID
     * @param request 体重记录请求
     * @return 操作结果
     */
    Boolean addWeightRecord(Long userId, AddWeightRequest request);

    /**
     * 更新体重记录
     *
     * @param userId 用户ID
     * @param recordId 记录ID（用于定位记录）
     * @param request 更新请求
     * @return 操作结果
     */
    Boolean updateWeightRecord(Long userId, String recordId, AddWeightRequest request);

    /**
     * 获取体重记录
     *
     * @param userId 用户ID
     * @param request 查询请求
     * @return 体重记录响应
     */
    Object getWeightRecords(Long userId, QueryHealthRecordRequest request);

    /**
     * 删除体重记录
     *
     * @param userId 用户ID
     * @param recordId 记录ID
     * @return 操作结果
     */
    Boolean deleteWeightRecord(Long userId, String recordId);

    /**
     * 批量删除体重记录
     *
     * @param userId 用户ID
     * @param recordIds 记录ID列表
     * @return 操作结果
     */
    Boolean batchDeleteWeightRecords(Long userId, List<String> recordIds);
} 