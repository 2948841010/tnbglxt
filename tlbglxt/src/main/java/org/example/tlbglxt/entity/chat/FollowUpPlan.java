package org.example.tlbglxt.entity.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 随访计划实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpPlan {
    
    /**
     * 下次复诊时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime nextVisitDate;
    
    /**
     * 随访项目列表
     */
    private List<String> followUpItems;
    
    /**
     * 注意事项列表
     */
    private List<String> precautions;
} 