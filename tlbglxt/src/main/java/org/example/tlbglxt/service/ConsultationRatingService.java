package org.example.tlbglxt.service;

import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.dto.request.consultation.SubmitRatingRequest;

/**
 * 问诊评价服务接口
 */
public interface ConsultationRatingService {
    
    /**
     * 提交问诊评价
     *
     * @param userId 用户ID
     * @param request 评价请求
     * @return 结果
     */
    Result<Object> submitRating(Long userId, SubmitRatingRequest request);
    
    /**
     * 获取问诊评价
     *
     * @param consultationNo 咨询编号
     * @param userId 用户ID
     * @return 评价信息
     */
    Result<Object> getRating(String consultationNo, Long userId);
    
    /**
     * 获取医生评价统计
     *
     * @param doctorId 医生ID
     * @return 评价统计
     */
    Result<Object> getDoctorRatingStats(Long doctorId);
} 