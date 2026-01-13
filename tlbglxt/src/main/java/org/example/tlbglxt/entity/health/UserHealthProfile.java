package org.example.tlbglxt.entity.health;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户健康档案实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "user_health_profile")
public class UserHealthProfile extends BaseHealthRecord {

    /**
     * 个人信息
     */
    @Field("profileInfo")
    private ProfileInfo profileInfo;

    /**
     * 糖尿病信息
     */
    @Field("diabetesInfo")
    private DiabetesInfo diabetesInfo;

    /**
     * 个人基础信息
     */
    @Data
    public static class ProfileInfo {
        /**
         * 身高 (cm)
         */
        @Field("height")
        private Integer height;

        /**
         * 当前体重 (kg)
         */
        @Field("weight")
        private Double weight;

        /**
         * BMI指数
         */
        @Field("bmi")
        private Double bmi;

        /**
         * 血型
         */
        @Field("bloodType")
        private String bloodType;

        /**
         * 过敏史
         */
        @Field("allergies")
        private String allergies;

        /**
         * 家族病史
         */
        @Field("familyHistory")
        private String familyHistory;

        /**
         * 紧急联系人
         */
        @Field("emergencyContact")
        private String emergencyContact;

        /**
         * 紧急联系人电话
         */
        @Field("emergencyPhone")
        private String emergencyPhone;
    }

    /**
     * 糖尿病相关信息
     */
    @Data
    public static class DiabetesInfo {
        /**
         * 糖尿病类型: type1(1型), type2(2型), gestational(妊娠期), other(其他)
         */
        @Field("diabetesType")
        private String diabetesType;

        /**
         * 确诊日期
         */
        @Field("diagnosisDate")
        private LocalDate diagnosisDate;

        /**
         * 目标血糖范围 - 最低值 (mmol/L)
         */
        @Field("targetGlucoseMin")
        private Double targetGlucoseMin;

        /**
         * 目标血糖范围 - 最高值 (mmol/L)
         */
        @Field("targetGlucoseMax")
        private Double targetGlucoseMax;

        /**
         * 糖化血红蛋白目标值 (%)
         */
        @Field("targetHbA1c")
        private Double targetHbA1c;

        /**
         * 当前用药情况
         */
        @Field("currentMedication")
        private String currentMedication;

        /**
         * 胰岛素使用情况
         */
        @Field("insulinUsage")
        private String insulinUsage;

        /**
         * 并发症情况
         */
        @Field("complications")
        private String complications;

        /**
         * 主治医生
         */
        @Field("primaryDoctor")
        private String primaryDoctor;

        /**
         * 最近复查时间
         */
        @Field("lastCheckupDate")
        private LocalDate lastCheckupDate;

        /**
         * 下次复查时间
         */
        @Field("nextCheckupDate")
        private LocalDate nextCheckupDate;
    }
} 