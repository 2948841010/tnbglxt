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
 * 体重记录实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "weight_records")
public class WeightRecord extends BaseHealthRecord {

    /**
     * 体重记录列表
     */
    @Field("records")
    private List<WeightEntry> records;

    /**
     * 统计数据
     */
    @Field("statistics")
    private WeightStatistics statistics;

    /**
     * 体重记录条目
     */
    @Data
    public static class WeightEntry {
        /**
         * 记录唯一标识
         */
        @Field("id")
        private String id;

        /**
         * 体重值 (kg)
         */
        @Field("weight")
        private BigDecimal weight;

        /**
         * 身高 (cm) - 可选，用于计算BMI
         */
        @Field("height")
        private BigDecimal height;

        /**
         * BMI值
         */
        @Field("bmi")
        private BigDecimal bmi;

        /**
         * 体脂率 (%) - 可选
         */
        @Field("bodyFatRate")
        private BigDecimal bodyFatRate;

        /**
         * 肌肉量 (kg) - 可选
         */
        @Field("muscleMass")
        private BigDecimal muscleMass;

        /**
         * 基础代谢率 (kcal) - 可选
         */
        @Field("bmr")
        private Integer bmr;

        /**
         * 测量时间
         */
        @Field("measureTime")
        @Indexed
        private LocalDateTime measureTime;

        /**
         * 测量状态: morning(晨起), evening(晚间), after_meal(餐后), before_meal(餐前)
         */
        @Field("measureState")
        private String measureState;

        /**
         * 备注
         */
        @Field("note")
        private String note;

        /**
         * 是否手动输入
         */
        @Field("isManualInput")
        private Boolean isManualInput;

        /**
         * 设备类型
         */
        @Field("deviceType")
        private String deviceType;

        /**
         * 构造函数，自动生成ID
         */
        public WeightEntry() {
            this.id = UUID.randomUUID().toString();
        }
    }

    /**
     * 体重统计数据
     */
    @Data
    public static class WeightStatistics {
        /**
         * 平均体重
         */
        @Field("avgWeight")
        private BigDecimal avgWeight;

        /**
         * 最高体重
         */
        @Field("maxWeight")
        private BigDecimal maxWeight;

        /**
         * 最低体重
         */
        @Field("minWeight")
        private BigDecimal minWeight;

        /**
         * 当前体重
         */
        @Field("currentWeight")
        private BigDecimal currentWeight;

        /**
         * 当前BMI
         */
        @Field("currentBmi")
        private BigDecimal currentBmi;

        /**
         * 目标体重
         */
        @Field("targetWeight")
        private BigDecimal targetWeight;

        /**
         * 体重变化趋势: up(上升), down(下降), stable(稳定)
         */
        @Field("weightTrend")
        private String weightTrend;

        /**
         * 7天体重变化
         */
        @Field("weightChange7Days")
        private BigDecimal weightChange7Days;

        /**
         * 30天体重变化
         */
        @Field("weightChange30Days")
        private BigDecimal weightChange30Days;

        /**
         * 记录总数
         */
        @Field("totalCount")
        private Integer totalCount;

        /**
         * 最后更新时间
         */
        @Field("lastUpdateTime")
        private LocalDateTime lastUpdateTime;
    }
} 