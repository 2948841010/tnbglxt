package org.example.tlbglxt.dto.response.health;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 血压记录响应DTO
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class BloodPressureRecordResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 血压记录列表
     */
    private List<PressureEntryVO> records;

    /**
     * 统计数据
     */
    private PressureStatisticsVO statistics;

    /**
     * 血压记录条目VO
     */
    @Data
    public static class PressureEntryVO {
        /**
         * 记录唯一标识
         */
        private String id;

        /**
         * 收缩压 (mmHg)
         */
        private Integer systolic;

        /**
         * 舒张压 (mmHg)
         */
        private Integer diastolic;

        /**
         * 心率 (bpm)
         */
        private Integer heartRate;

        /**
         * 测量时间
         * 注意：移除@JsonFormat注解，使用全局JacksonConfig处理各种时间格式
         */
        private LocalDateTime measureTime;

        /**
         * 测量状态: rest(休息), activity(活动后), morning(晨起), evening(晚间)
         */
        private String measureState;

        /**
         * 备注
         */
        private String note;

        /**
         * 血压水平评估: normal(正常), high(偏高), low(偏低)
         */
        private String level;
    }

    /**
     * 血压统计数据VO
     */
    @Data
    public static class PressureStatisticsVO {
        /**
         * 平均收缩压
         */
        private BigDecimal avgSystolic;

        /**
         * 平均舒张压
         */
        private BigDecimal avgDiastolic;

        /**
         * 平均心率
         */
        private BigDecimal avgHeartRate;

        /**
         * 最高收缩压
         */
        private Integer maxSystolic;

        /**
         * 最低收缩压
         */
        private Integer minSystolic;

        /**
         * 最高舒张压
         */
        private Integer maxDiastolic;

        /**
         * 最低舒张压
         */
        private Integer minDiastolic;

        /**
         * 记录总数
         */
        private Integer totalCount;

        /**
         * 正常范围内记录数
         */
        private Integer normalCount;

        /**
         * 高血压记录数
         */
        private Integer highCount;

        /**
         * 低血压记录数
         */
        private Integer lowCount;

        /**
         * 最后更新时间
         * 注意：移除@JsonFormat注解，使用全局JacksonConfig处理各种时间格式
         */
        private LocalDateTime lastUpdateTime;
    }
} 