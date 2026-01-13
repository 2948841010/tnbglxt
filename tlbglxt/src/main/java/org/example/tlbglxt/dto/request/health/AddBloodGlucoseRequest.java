package org.example.tlbglxt.dto.request.health;

import lombok.Data;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 添加血糖记录请求DTO
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class AddBloodGlucoseRequest {

    /**
     * 血糖值 (mmol/L)
     */
    @NotNull(message = "血糖值不能为空")
    @DecimalMin(value = "0.1", message = "血糖值必须大于0.1")
    @DecimalMax(value = "50.0", message = "血糖值不能超过50.0")
    private BigDecimal value;

    /**
     * 测量类型: fasting(空腹), after_meal(餐后), random(随机)
     */
    @NotBlank(message = "测量类型不能为空")
    @Pattern(regexp = "^(fasting|after_meal|random)$", message = "测量类型只能是 fasting、after_meal、random")
    private String measureType;

    /**
     * 测量时间
     */
    @NotNull(message = "测量时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime measureTime;

    /**
     * 餐次: breakfast(早餐), lunch(午餐), dinner(晚餐)
     */
    @Pattern(regexp = "^(breakfast|lunch|dinner)?$", message = "餐次只能是 breakfast、lunch、dinner 或为空")
    private String mealType;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注不能超过200个字符")
    private String note;
} 