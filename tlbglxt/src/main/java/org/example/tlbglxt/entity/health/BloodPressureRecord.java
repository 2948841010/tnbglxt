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
 * 血压记录实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "blood_pressure_records")
public class BloodPressureRecord extends BaseHealthRecord {

    /**
     * 血压记录列表
     */
    @Field("records")
    private List<PressureEntry> records;

    /**
     * 统计数据
     */
    @Field("statistics")
    private PressureStatistics statistics;

    /**
     * 血压记录条目
     */
    @Data
    public static class PressureEntry {
        /**
         * 记录唯一标识
         */
        @Field("id")
        private String id;

        /**
         * 收缩压 (mmHg)
         */
        @Field("systolic")
        private Integer systolic;

        /**
         * 舒张压 (mmHg)
         */
        @Field("diastolic")
        private Integer diastolic;

        /**
         * 心率 (bpm)
         */
        @Field("heartRate")
        private Integer heartRate;

        /**
         * 测量时间
         */
        @Field("measureTime")
        @Indexed
        private LocalDateTime measureTime;

        /**
         * 测量状态: rest(休息), activity(活动后), morning(晨起), evening(晚间)
         */
        @Field("measureState")
        private String measureState;

        /**
         * 备注
         */
        @Field("note")
        private String note;

        /**
         * 构造函数，自动生成ID
         */
        public PressureEntry() {
            this.id = UUID.randomUUID().toString();
        }
    }

    /**
     * 血压统计数据
     */
    @Data
    public static class PressureStatistics {
        /**
         * 平均收缩压
         */
        @Field("avgSystolic")
        private BigDecimal avgSystolic;

        /**
         * 平均舒张压
         */
        @Field("avgDiastolic")
        private BigDecimal avgDiastolic;

        /**
         * 平均心率
         */
        @Field("avgHeartRate")
        private BigDecimal avgHeartRate;

        /**
         * 最高收缩压
         */
        @Field("maxSystolic")
        private Integer maxSystolic;

        /**
         * 最高舒张压
         */
        @Field("maxDiastolic")
        private Integer maxDiastolic;

        /**
         * 最低收缩压
         */
        @Field("minSystolic")
        private Integer minSystolic;

        /**
         * 最低舒张压
         */
        @Field("minDiastolic")
        private Integer minDiastolic;

        /**
         * 记录总数
         */
        @Field("totalCount")
        private Integer totalCount;

        /**
         * 正常血压记录数
         */
        @Field("normalCount")
        private Integer normalCount;

        /**
         * 高血压记录数
         */
        @Field("highCount")
        private Integer highCount;

        /**
         * 低血压记录数
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
 