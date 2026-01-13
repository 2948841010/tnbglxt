package org.example.tlbglxt.service;

import org.example.tlbglxt.common.PageResult;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.entity.DoctorInfo;
import org.example.tlbglxt.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 医生服务接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
public interface DoctorService {

    /**
     * 根据用户ID获取医生完整信息（包含用户基础信息）
     *
     * @param userId 用户ID
     * @return 医生完整信息
     */
    Result<DoctorInfo> getDoctorInfoByUserId(Long userId);

    /**
     * 根据医生编号获取医生信息
     *
     * @param doctorNo 医生编号
     * @return 医生信息
     */
    Result<DoctorInfo> getDoctorInfoByDoctorNo(String doctorNo);

    /**
     * 更新医生个人信息
     *
     * @param userId 用户ID
     * @param user 用户基础信息
     * @param doctorInfo 医生专业信息
     * @return 更新结果
     */
    Result<Void> updateDoctorProfile(Long userId, User user, DoctorInfo doctorInfo);

    /**
     * 创建医生信息档案
     *
     * @param doctorInfo 医生信息
     * @return 创建结果
     */
    Result<DoctorInfo> createDoctorInfo(DoctorInfo doctorInfo);

    /**
     * 更新医生在线状态
     *
     * @param userId 用户ID
     * @param onlineStatus 在线状态（0-离线，1-在线，2-忙碌）
     * @return 更新结果
     */
    Result<Void> updateOnlineStatus(Long userId, Integer onlineStatus);

    /**
     * 验证医生是否存在
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    boolean isDoctorExists(Long userId);

    // ======================= 统计相关方法 =======================

    /**
     * 获取总医生数
     *
     * @return 总医生数
     */
    long getTotalDoctorCount();

    /**
     * 获取活跃医生数
     *
     * @param since 指定时间之后的医生
     * @return 活跃医生数
     */
    long getActiveDoctorCount(LocalDateTime since);

    /**
     * 获取在线医生数
     *
     * @return 在线医生数
     */
    long getOnlineDoctorCount();

    // ======================= 管理员相关方法 =======================

    /**
     * 分页查询医生列表（管理员用）
     *
     * @param page 页码
     * @param size 页面大小
     * @param keyword 关键词搜索（姓名、用户名）
     * @param department 科室筛选
     * @param title 职称筛选
     * @param onlineStatus 在线状态筛选
     * @param available 可接诊状态筛选
     * @param status 用户状态筛选
     * @param startTime 注册开始时间
     * @param endTime 注册结束时间
     * @return 分页医生列表
     */
    PageResult<Map<String, Object>> getDoctorListForAdmin(int page, int size, String keyword, 
                                                         String department, String title, Integer onlineStatus, 
                                                         Integer available, Integer status, 
                                                         LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取医生统计数据（管理员用）
     *
     * @return 医生统计信息
     */
    Map<String, Object> getDoctorStatistics();

    /**
     * 更新医生可接诊状态（管理员）
     *
     * @param doctorId 医生ID
     * @param available 可接诊状态（0-不可接诊，1-可接诊）
     * @return 更新结果
     */
    Result<Void> updateDoctorAvailable(Long doctorId, Integer available);

    /**
     * 更新医生用户状态（管理员）
     *
     * @param doctorId 医生ID
     * @param status 用户状态（0-禁用，1-启用）
     * @return 更新结果
     */
    Result<Void> updateDoctorUserStatus(Long doctorId, Integer status);

    /**
     * 更新医生信息（管理员）
     *
     * @param doctorId 医生ID
     * @param doctorData 医生数据
     * @return 更新结果
     */
    Result<Void> updateDoctorInfo(Long doctorId, Map<String, Object> doctorData);

    /**
     * 删除医生（管理员）
     *
     * @param doctorId 医生ID
     * @return 删除结果
     */
    Result<Void> deleteDoctor(Long doctorId);

    /**
     * 批量删除医生（管理员）
     *
     * @param doctorIds 医生ID列表
     * @return 删除结果
     */
    Result<Void> batchDeleteDoctors(List<Long> doctorIds);

    /**
     * 获取医生详情（管理员用）
     *
     * @param doctorId 医生ID
     * @return 医生详细信息
     */
    Result<Map<String, Object>> getDoctorDetailForAdmin(Long doctorId);
} 