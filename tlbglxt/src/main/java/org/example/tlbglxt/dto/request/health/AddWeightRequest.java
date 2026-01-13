package org.example.tlbglxt.dto.request.health;

import lombok.Data;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 添加体重记录请求DTO
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class AddWeightRequest {

    /**
     * 体重值 (kg)
     */
    @NotNull(message = "体重值不能为空")
    @DecimalMin(value = "1.0", message = "体重值必须大于1.0kg")
    @DecimalMax(value = "500.0", message = "体重值不能超过500.0kg")
    private BigDecimal weight;

    /**
     * 身高 (cm) - 可选，用于计算BMI
     */
    @DecimalMin(value = "50.0", message = "身高必须大于50.0cm")
    @DecimalMax(value = "250.0", message = "身高不能超过250.0cm")
    private BigDecimal height;

    /**
     * 体脂率 (%) - 可选
     */
    @DecimalMin(value = "0.0", message = "体脂率不能为负数")
    @DecimalMax(value = "100.0", message = "体脂率不能超过100%")
    private BigDecimal bodyFatRate;

    /**
     * 肌肉量 (kg) - 可选
     */
    @DecimalMin(value = "0.0", message = "肌肉量不能为负数")
    @DecimalMax(value = "200.0", message = "肌肉量不能超过200.0kg")
    private BigDecimal muscleMass;

    /**
     * 基础代谢率 (kcal) - 可选
     */
    @Min(value = 500, message = "基础代谢率不能低于500kcal")
    @Max(value = 5000, message = "基础代谢率不能超过5000kcal")
    private Integer bmr;

    /**
     * 测量时间
     */
    @NotNull(message = "测量时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime measureTime;

    /**
     * 测量状态: morning(晨起), evening(晚间), after_meal(餐后), before_meal(餐前)
     */
    @Pattern(regexp = "^(morning|evening|after_meal|before_meal)?$", 
             message = "测量状态只能是 morning、evening、after_meal、before_meal 或为空")
    private String measureState;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注不能超过200个字符")
    private String note;

    /**
     * 是否手动输入
     */
    private Boolean isManualInput = true;

    /**
     * 设备类型
     */
    @Size(max = 50, message = "设备类型不能超过50个字符")
    private String deviceType;
} 