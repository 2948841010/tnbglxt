package org.example.tlbglxt.entity.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 处方信息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {
    
    /**
     * 药品列表
     */
    private List<Medication> medications;
    
    /**
     * 处方总费用
     */
    private BigDecimal totalFee;
    
    /**
     * 药品信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Medication {
        /**
         * 药品名称
         */
        private String drugName;
        
        /**
         * 用药剂量
         */
        private String dosage;
        
        /**
         * 用药频次
         */
        private String frequency;
        
        /**
         * 用药时长
         */
        private String duration;
        
        /**
         * 用药说明
         */
        private String notes;
    }
} 