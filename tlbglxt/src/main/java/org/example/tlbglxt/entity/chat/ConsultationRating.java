package org.example.tlbglxt.entity.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 问诊评价实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRating {
    
    /**
     * 评分（1-5星）
     */
    private Integer score;
    
    /**
     * 评价内容
     */
    private String comment;
    
    /**
     * 评价时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime ratingTime;
} 