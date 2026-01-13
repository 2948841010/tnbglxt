package org.example.tlbglxt.dto.response.health;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 血糖记录响应DTO
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class BloodGlucoseRecordResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 血糖记录列表
     */
    private List<GlucoseEntryVO> records;

    /**
     * 统计数据
     */
    private GlucoseStatisticsVO statistics;

    /**
     * 血糖记录条目VO
     */
    @Data
    public static class GlucoseEntryVO {
        /**
         * 记录唯一标识
         */
        private String id;

        /**
         * 血糖值 (mmol/L)
         */
        private BigDecimal value;

        /**
         * 测量类型
         */
        private String measureType;

        /**
         * 测量时间
         * 注意：移除@JsonFormat注解，使用全局JacksonConfig处理各种时间格式
         */
        private LocalDateTime measureTime;

        /**
         * 餐次
         */
        private String mealType;

        /**
         * 备注
         */
        private String note;

        /**
         * 血糖水平评估: normal(正常), high(偏高), low(偏低)
         */
        private String level;
    }

    /**
     * 血糖统计数据VO
     */
    @Data
    public static class GlucoseStatisticsVO {
        /**
         * 平均值
         */
        private BigDecimal avgValue;

        /**
         * 最高值
         */
        private BigDecimal maxValue;

        /**
         * 最低值
         */
        private BigDecimal minValue;

        /**
         * 记录总数
         */
        private Integer totalCount;

        /**
         * 正常范围内记录数
         */
        private Integer normalCount;

        /**
         * 高血糖记录数
         */
        private Integer highCount;

        /**
         * 低血糖记录数
         */
        private Integer lowCount;

        /**
         * 最后更新时间
         * 注意：移除@JsonFormat注解，使用全局JacksonConfig处理各种时间格式
         */
        private LocalDateTime lastUpdateTime;
    }
} 