package org.example.tlbglxt.entity.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问诊聊天记录实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "consultation_chats")
public class ConsultationChat {
    
    /**
     * MongoDB文档ID
     */
    @Id
    private String id;
    
    /**
     * 问诊编号
     */
    private String consultationNo;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 问诊类型：1-图文问诊，2-语音问诊，3-视频问诊
     */
    private Integer consultationType;
    
    /**
     * 问诊状态：1-待接诊，2-进行中，3-已完成，4-已取消
     */
    private Integer status;
    
    /**
     * 主诉
     */
    private String chiefComplaint;
    
    /**
     * 患者信息
     */
    private PatientInfo patientInfo;
    
    /**
     * 医生信息
     */
    private DoctorInfo doctorInfo;
    
    /**
     * 问诊费用
     */
    private BigDecimal fee;
    
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    
    /**
     * 消息列表
     */
    private List<ConsultationMessage> messages;
    
    /**
     * 诊断信息
     */
    private Diagnosis diagnosis;
    
    /**
     * 处方信息
     */
    private Prescription prescription;
    
    /**
     * 随访计划
     */
    private FollowUpPlan followUpPlan;
    
    /**
     * 评价信息
     */
    private ConsultationRating rating;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    /**
     * 患者信息嵌套类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientInfo {
        private String name;
        private Integer age;
        private String gender;
        private String phone;
        private String avatar;  // 患者头像
    }
    
    /**
     * 医生信息嵌套类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorInfo {
        private String name;
        private String department;
        private String title;
        private String avatar;  // 医生头像
    }
} 