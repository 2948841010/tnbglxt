package org.example.tlbglxt.service;

import org.example.tlbglxt.entity.DoctorInfo;

import java.util.List;

/**
 * 咨询服务接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
public interface ConsultationService {

    /**
     * 获取可咨询的医生列表
     *
     * @param department 科室（可选）
     * @param keyword    关键词搜索（可选）
     * @param page       页码
     * @param size       每页大小
     * @return 医生列表
     */
    List<DoctorInfo> getAvailableDoctors(String department, String keyword, Integer page, Integer size);

    /**
     * 获取医生详细信息
     *
     * @param doctorId 医生用户ID
     * @return 医生详细信息
     */
    DoctorInfo getDoctorDetail(Long doctorId);

    /**
     * 获取所有科室列表
     *
     * @return 科室列表
     */
    List<String> getAllDepartments();
} 