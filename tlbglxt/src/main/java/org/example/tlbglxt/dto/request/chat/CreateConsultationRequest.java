package org.example.tlbglxt.dto.request.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建问诊请求DTO
 */
@Data
public class CreateConsultationRequest {
    
    /**
     * 医生ID
     */
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;
    
    /**
     * 问诊类型：1-图文问诊，2-语音问诊，3-视频问诊
     */
    @NotNull(message = "问诊类型不能为空")
    private Integer consultationType;
    
    /**
     * 主诉
     */
    @NotNull(message = "主诉不能为空")
    private String chiefComplaint;
    
    /**
     * 问诊费用
     */
    private BigDecimal fee = BigDecimal.ZERO;
} 