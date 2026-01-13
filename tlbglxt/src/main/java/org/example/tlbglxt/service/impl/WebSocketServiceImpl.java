package org.example.tlbglxt.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.dto.websocket.DoctorStatusMessage;
import org.example.tlbglxt.entity.chat.ConsultationChat;
import org.example.tlbglxt.entity.chat.ConsultationMessage;
import org.example.tlbglxt.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务实现类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class WebSocketServiceImpl implements WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void broadcastDoctorStatusChange(Long doctorId, String doctorName, String department, Integer onlineStatus) {
        try {
            // 创建状态变化消息
            DoctorStatusMessage message = new DoctorStatusMessage(doctorId, doctorName, department, onlineStatus);
            
            // 向所有订阅了医生状态主题的客户端广播消息
            messagingTemplate.convertAndSend("/topic/doctor-status", message);
            
            log.info("广播医生状态变化成功 - 医生ID：{}，姓名：{}，状态：{}", 
                    doctorId, doctorName, message.getStatusText());
            
        } catch (Exception e) {
            log.error("广播医生状态变化失败 - 医生ID：{}，姓名：{}", doctorId, doctorName, e);
        }
    }
    
    @Override
    public void sendMessageToConsultation(String consultationNo, ConsultationMessage message) {
        try {
            // 向指定问诊聊天室发送消息
            String destination = "/topic/consultation/" + consultationNo;
            messagingTemplate.convertAndSend(destination, message);
            
            log.info("发送聊天消息成功 - 问诊编号：{}，消息ID：{}", 
                    consultationNo, message.getMessageId());
            
        } catch (Exception e) {
            log.error("发送聊天消息失败 - 问诊编号：{}，消息ID：{}", 
                    consultationNo, message.getMessageId(), e);
        }
    }
    
    @Override
    public void sendMessageToUser(Long userId, Object message) {
        try {
            // 向指定用户发送私有消息
            String destination = "/user/" + userId + "/queue/messages";
            messagingTemplate.convertAndSend(destination, message);
            
            log.info("发送私有消息成功 - 用户ID：{}", userId);
            
        } catch (Exception e) {
            log.error("发送私有消息失败 - 用户ID：{}", userId, e);
        }
    }
    
    @Override
    public void notifyConsultationStatusChange(String consultationNo, Integer oldStatus, Integer newStatus, ConsultationChat consultation) {
        try {
            // 构建状态变更消息
            Map<String, Object> statusMessage = new HashMap<>();
            statusMessage.put("type", "consultation_status_changed");
            statusMessage.put("consultationNo", consultationNo);
            statusMessage.put("oldStatus", oldStatus);
            statusMessage.put("newStatus", newStatus);
            statusMessage.put("consultation", consultation);
            statusMessage.put("timestamp", System.currentTimeMillis());
            
            // 向该咨询的状态变更主题发送消息
            String destination = "/topic/consultation/status/" + consultationNo;
            messagingTemplate.convertAndSend(destination, statusMessage);
            
            log.info("通知咨询状态变更成功 - 问诊编号：{}，状态：{} -> {}", 
                    consultationNo, oldStatus, newStatus);
            
        } catch (Exception e) {
            log.error("通知咨询状态变更失败 - 问诊编号：{}，状态：{} -> {}", 
                    consultationNo, oldStatus, newStatus, e);
        }
    }
    
    @Override
    public void notifyDoctorConsultationListUpdate(Long doctorId, String updateType, ConsultationChat consultation) {
        try {
            // 构建列表更新消息
            Map<String, Object> updateMessage = new HashMap<>();
            updateMessage.put("type", updateType);
            updateMessage.put("consultation", consultation);
            updateMessage.put("consultationNo", consultation.getConsultationNo());
            updateMessage.put("timestamp", System.currentTimeMillis());
            
            if ("consultation_status_changed".equals(updateType)) {
                updateMessage.put("newStatus", consultation.getStatus());
            }
            
            // 向医生咨询列表主题发送消息
            String destination = "/topic/doctor/" + doctorId + "/consultations";
            messagingTemplate.convertAndSend(destination, updateMessage);
            
            log.info("通知医生咨询列表更新成功 - 医生ID：{}，更新类型：{}", doctorId, updateType);
            
        } catch (Exception e) {
            log.error("通知医生咨询列表更新失败 - 医生ID：{}，更新类型：{}", doctorId, updateType, e);
        }
    }
    
    @Override
    public void notifyNewConsultation(ConsultationChat consultation) {
        try {
            // 通知医生有新咨询
            notifyDoctorConsultationListUpdate(
                consultation.getDoctorId(), 
                "new_consultation", 
                consultation
            );
            
            log.info("通知新咨询创建成功 - 问诊编号：{}", consultation.getConsultationNo());
            
        } catch (Exception e) {
            log.error("通知新咨询创建失败 - 问诊编号：{}", consultation.getConsultationNo(), e);
        }
    }
} 