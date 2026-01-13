package org.example.tlbglxt.entity.health;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 血糖记录实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "blood_glucose_records")
public class BloodGlucoseRecord extends BaseHealthRecord {

    /**
     * 血糖记录列表
     */
    @Field("records")
    private List<GlucoseEntry> records;

    /**
     * 统计数据
     */
    @Field("statistics")
    private GlucoseStatistics statistics;

    /**
     * 血糖记录条目
     */
    @Data
    public static class GlucoseEntry {
        /**
         * 记录唯一标识
         */
        @Field("id")
        private String id;

        /**
         * 血糖值 (mmol/L)
         */
        @Field("value")
        private BigDecimal value;

        /**
         * 测量类型: fasting(空腹), after_meal(餐后), random(随机)
         */
        @Field("measureType")
        private String measureType;

        /**
         * 测量时间
         */
        @Field("measureTime")
        @Indexed
        private LocalDateTime measureTime;

        /**
         * 餐次: breakfast(早餐), lunch(午餐), dinner(晚餐)
         */
        @Field("mealType")
        private String mealType;

        /**
         * 备注
         */
        @Field("note")
        private String note;

        /**
         * 构造函数，自动生成ID
         */
        public GlucoseEntry() {
            this.id = UUID.randomUUID().toString();
        }
    }

    /**
     * 血糖统计数据
     */
    @Data
    public static class GlucoseStatistics {
        /**
         * 平均值
         */
        @Field("avgValue")
        private BigDecimal avgValue;

        /**
         * 最高值
         */
        @Field("maxValue")
        private BigDecimal maxValue;

        /**
         * 最低值
         */
        @Field("minValue")
        private BigDecimal minValue;

        /**
         * 记录总数
         */
        @Field("totalCount")
        private Integer totalCount;

        /**
         * 正常范围内记录数
         */
        @Field("normalCount")
        private Integer normalCount;

        /**
         * 高血糖记录数
         */
        @Field("highCount")
        private Integer highCount;

        /**
         * 低血糖记录数
         */
        @Field("lowCount")
        private Integer lowCount;

        /**
         * 最后更新时间
         */
        @Field("lastUpdateTime")
        private LocalDateTime lastUpdateTime;
    }
} 