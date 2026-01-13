package org.example.tlbglxt.service;

import org.example.tlbglxt.entity.chat.ConsultationChat;
import org.example.tlbglxt.entity.chat.ConsultationMessage;

/**
 * WebSocket服务接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
public interface WebSocketService {

    /**
     * 广播医生状态变化
     *
     * @param doctorId 医生用户ID
     * @param doctorName 医生姓名
     * @param department 科室
     * @param onlineStatus 在线状态
     */
    void broadcastDoctorStatusChange(Long doctorId, String doctorName, String department, Integer onlineStatus);
    
    /**
     * 向指定问诊聊天室发送消息
     *
     * @param consultationNo 问诊编号
     * @param message 消息内容
     */
    void sendMessageToConsultation(String consultationNo, ConsultationMessage message);
    
    /**
     * 向指定用户发送私有消息
     *
     * @param userId 用户ID
     * @param message 消息内容
     */
    void sendMessageToUser(Long userId, Object message);
    
    /**
     * 通知咨询状态变更
     *
     * @param consultationNo 问诊编号
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param consultation 咨询详情
     */
    void notifyConsultationStatusChange(String consultationNo, Integer oldStatus, Integer newStatus, ConsultationChat consultation);
    
    /**
     * 通知医生咨询列表更新
     *
     * @param doctorId 医生ID
     * @param updateType 更新类型
     * @param consultation 咨询信息
     */
    void notifyDoctorConsultationListUpdate(Long doctorId, String updateType, ConsultationChat consultation);
    
    /**
     * 通知新咨询创建
     *
     * @param consultation 新创建的咨询
     */
    void notifyNewConsultation(ConsultationChat consultation);
} 