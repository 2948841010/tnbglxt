package org.example.tlbglxt.entity.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 诊断信息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diagnosis {
    
    /**
     * 主要诊断
     */
    private String primaryDiagnosis;
    
    /**
     * 次要诊断列表
     */
    private List<String> secondaryDiagnosis;
    
    /**
     * 临床建议
     */
    private String clinicalAdvice;
} 