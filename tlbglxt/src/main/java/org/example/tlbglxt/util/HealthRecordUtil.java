package org.example.tlbglxt.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 健康记录工具类
 *
 * @author 开发团队
 * @since 1.0.0
 */
public class HealthRecordUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 血糖水平评估
     *
     * @param value 血糖值 (mmol/L)
     * @param measureType 测量类型
     * @return 评估结果: normal, high, low
     */
    public static String evaluateBloodGlucoseLevel(BigDecimal value, String measureType) {
        if (value == null) {
            return "unknown";
        }

        switch (measureType) {
            case "fasting": // 空腹血糖
                if (value.compareTo(new BigDecimal("3.9")) < 0) {
                    return "low";
                } else if (value.compareTo(new BigDecimal("6.1")) > 0) {
                    return "high";
                } else {
                    return "normal";
                }
            case "after_meal": // 餐后血糖
                if (value.compareTo(new BigDecimal("3.9")) < 0) {
                    return "low";
                } else if (value.compareTo(new BigDecimal("11.1")) > 0) {
                    return "high";
                } else {
                    return "normal";
                }
            default: // 随机血糖
                if (value.compareTo(new BigDecimal("3.9")) < 0) {
                    return "low";
                } else if (value.compareTo(new BigDecimal("11.1")) > 0) {
                    return "high";
                } else {
                    return "normal";
                }
        }
    }

    /**
     * 血压水平评估
     *
     * @param systolic 收缩压
     * @param diastolic 舒张压
     * @return 评估结果: normal, high, low
     */
    public static String evaluateBloodPressureLevel(Integer systolic, Integer diastolic) {
        if (systolic == null || diastolic == null) {
            return "unknown";
        }

        // 低血压
        if (systolic < 90 && diastolic < 60) {
            return "low";
        }
        // 高血压
        else if (systolic >= 140 || diastolic >= 90) {
            return "high";
        }
        // 正常血压
        else {
            return "normal";
        }
    }

    /**
     * BMI计算和评估
     *
     * @param weight 体重(kg)
     * @param height 身高(cm)
     * @return BMI值
     */
    public static BigDecimal calculateBMI(BigDecimal weight, BigDecimal height) {
        if (weight == null || height == null || height.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        // BMI = 体重(kg) / 身高(m)²
        BigDecimal heightInMeters = height.divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        return weight.divide(heightInMeters.multiply(heightInMeters), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * BMI水平评估
     *
     * @param bmi BMI值
     * @return 评估结果: underweight, normal, overweight, obese
     */
    public static String evaluateBMILevel(BigDecimal bmi) {
        if (bmi == null) {
            return "unknown";
        }

        if (bmi.compareTo(new BigDecimal("18.5")) < 0) {
            return "underweight"; // 偏瘦
        } else if (bmi.compareTo(new BigDecimal("24.0")) < 0) {
            return "normal"; // 正常
        } else if (bmi.compareTo(new BigDecimal("28.0")) < 0) {
            return "overweight"; // 超重
        } else {
            return "obese"; // 肥胖
        }
    }

    /**
     * 心率水平评估
     *
     * @param heartRate 心率(bpm)
     * @return 评估结果: normal, high, low
     */
    public static String evaluateHeartRateLevel(Integer heartRate) {
        if (heartRate == null) {
            return "unknown";
        }

        if (heartRate < 60) {
            return "low"; // 心动过缓
        } else if (heartRate > 100) {
            return "high"; // 心动过速
        } else {
            return "normal"; // 正常
        }
    }

    /**
     * 时间格式化
     *
     * @param dateTime 时间
     * @return 格式化后的时间字符串
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * 检查时间是否在合理范围内
     *
     * @param measureTime 测量时间
     * @return 是否合理
     */
    public static boolean isValidMeasureTime(LocalDateTime measureTime) {
        if (measureTime == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneYearAgo = now.minusYears(1);

        // 测量时间不能超过当前时间，也不能早于一年前
        return measureTime.isBefore(now.plusMinutes(5)) && measureTime.isAfter(oneYearAgo);
    }

    /**
     * 血糖值合理性检查
     *
     * @param value 血糖值
     * @return 是否合理
     */
    public static boolean isValidBloodGlucoseValue(BigDecimal value) {
        if (value == null) {
            return false;
        }
        // 血糖值应在0.5-50.0 mmol/L之间
        return value.compareTo(new BigDecimal("0.5")) >= 0 
               && value.compareTo(new BigDecimal("50.0")) <= 0;
    }

    /**
     * 血压值合理性检查
     *
     * @param systolic 收缩压
     * @param diastolic 舒张压
     * @return 是否合理
     */
    public static boolean isValidBloodPressureValue(Integer systolic, Integer diastolic) {
        if (systolic == null || diastolic == null) {
            return false;
        }
        // 收缩压应在50-300mmHg之间，舒张压应在30-200mmHg之间
        // 且收缩压应大于舒张压
        return systolic >= 50 && systolic <= 300 
               && diastolic >= 30 && diastolic <= 200
               && systolic > diastolic;
    }

    /**
     * 获取测量类型的中文描述
     *
     * @param measureType 测量类型
     * @return 中文描述
     */
    public static String getMeasureTypeDescription(String measureType) {
        if (measureType == null) {
            return "未知";
        }

        switch (measureType) {
            case "fasting":
                return "空腹";
            case "after_meal":
                return "餐后";
            case "random":
                return "随机";
            case "rest":
                return "休息时";
            case "activity":
                return "活动后";
            case "morning":
                return "晨起";
            case "evening":
                return "晚间";
            default:
                return measureType;
        }
    }

    /**
     * 获取餐次的中文描述
     *
     * @param mealType 餐次类型
     * @return 中文描述
     */
    public static String getMealTypeDescription(String mealType) {
        if (mealType == null) {
            return "";
        }

        switch (mealType) {
            case "breakfast":
                return "早餐";
            case "lunch":
                return "午餐";
            case "dinner":
                return "晚餐";
            default:
                return mealType;
        }
    }
} 