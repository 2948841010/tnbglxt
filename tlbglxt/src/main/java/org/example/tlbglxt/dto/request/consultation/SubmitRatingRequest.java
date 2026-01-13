package org.example.tlbglxt.dto.request.consultation;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 提交问诊评价请求DTO
 */
@Data
public class SubmitRatingRequest {
    
    /**
     * 咨询编号
     */
    @NotBlank(message = "咨询编号不能为空")
    private String consultationNo;
    
    /**
     * 评分：1-5分
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分不能低于1分")
    @Max(value = 5, message = "评分不能超过5分")
    private Integer score;
    
    /**
     * 评价内容（可选）
     */
    @Size(max = 500, message = "评价内容不能超过500字符")
    private String comment;
} 