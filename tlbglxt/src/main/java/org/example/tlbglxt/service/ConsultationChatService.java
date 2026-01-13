package org.example.tlbglxt.service;

import org.example.tlbglxt.common.PageResult;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.dto.request.chat.CreateConsultationRequest;
import org.example.tlbglxt.dto.request.chat.SendMessageRequest;
import org.example.tlbglxt.entity.chat.ConsultationChat;
import org.example.tlbglxt.entity.chat.ConsultationMessage;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 问诊聊天服务接口
 */
public interface ConsultationChatService {
    
    /**
     * 创建问诊
     */
    Result<ConsultationChat> createConsultation(Long patientId, CreateConsultationRequest request);
    
    /**
     * 发送消息
     */
    Result<ConsultationMessage> sendMessage(Long senderId, String senderType, SendMessageRequest request);
    
    /**
     * 获取问诊详情
     */
    Result<ConsultationChat> getConsultationDetail(String consultationNo);
    
    /**
     * 获取患者的问诊列表
     */
    Result<List<ConsultationChat>> getPatientConsultations(Long patientId);
    
    /**
     * 获取医生的问诊列表
     */
    Result<List<ConsultationChat>> getDoctorConsultations(Long doctorId);
    
    /**
     * 分页获取患者问诊列表
     */
    Result<PageResult<ConsultationChat>> getPatientConsultationsPage(Long patientId, int page, int size);
    
    /**
     * 分页获取医生问诊列表
     */
    Result<PageResult<ConsultationChat>> getDoctorConsultationsPage(Long doctorId, int page, int size);
    
    /**
     * 分页获取医生指定状态的问诊列表
     */
    Result<PageResult<ConsultationChat>> getDoctorConsultationsPageByStatus(Long doctorId, int page, int size, String statusFilter);
    
    /**
     * 医生接诊
     */
    Result<Void> acceptConsultation(Long doctorId, String consultationNo);
    
    /**
     * 完成问诊
     */
    Result<Void> completeConsultation(String consultationNo);
    
    /**
     * 取消问诊
     */
    Result<Void> cancelConsultation(String consultationNo, Long userId);
    
    /**
     * 标记消息为已读
     */
    Result<Void> markMessageAsRead(String consultationNo, String messageId, Long userId);
    
    /**
     * 获取用户正在进行的问诊
     */
    Result<List<ConsultationChat>> getActiveConsultations(Long userId);
    
    /**
     * 统计医生今日问诊数量
     */
    Result<Long> getTodayConsultationCount(Long doctorId);
    
    /**
     * 获取医生待回复咨询数量
     */
    Result<Long> getPendingConsultationsCount(Long doctorId);
    
    /**
     * 获取医生待接诊咨询数量（状态1）
     */
    Result<Long> getWaitingConsultationsCount(Long doctorId);
    
    /**
     * 获取医生正在接诊咨询数量（状态2）
     */
    Result<Long> getOngoingConsultationsCount(Long doctorId);
    
    /**
     * 获取医生已完成咨询数量（状态3）
     */
    Result<Long> getCompletedConsultationsCount(Long doctorId);
    
    /**
     * 获取医生最近咨询列表
     */
    Result<List<ConsultationChat>> getRecentConsultations(Long doctorId, int limit);

    // ======================= 管理员统计相关方法 =======================

    /**
     * 获取总咨询数量
     *
     * @return 总咨询数量
     */
    long getTotalConsultationCount();

    /**
     * 获取指定时间范围内的咨询数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 咨询数量
     */
    long getConsultationCountByDateRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取已完成咨询总数
     *
     * @return 已完成咨询总数
     */
    long getCompletedConsultationCount();

    /**
     * 获取指定时间范围内已完成的咨询数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 已完成咨询数量
     */
    long getCompletedConsultationCountByDate(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取正在进行的咨询数量（所有医生）
     *
     * @return 正在进行的咨询数量
     */
    long getOngoingConsultationCount();
} 