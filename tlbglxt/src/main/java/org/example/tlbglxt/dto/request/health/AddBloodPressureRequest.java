package org.example.tlbglxt.dto.request.health;

import lombok.Data;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 添加血压记录请求DTO
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class AddBloodPressureRequest {

    /**
     * 收缩压 (mmHg)
     */
    @NotNull(message = "收缩压不能为空")
    @Min(value = 50, message = "收缩压不能低于50")
    @Max(value = 300, message = "收缩压不能高于300")
    private Integer systolic;

    /**
     * 舒张压 (mmHg)
     */
    @NotNull(message = "舒张压不能为空")
    @Min(value = 30, message = "舒张压不能低于30")
    @Max(value = 200, message = "舒张压不能高于200")
    private Integer diastolic;

    /**
     * 心率 (bpm)
     */
    @Min(value = 30, message = "心率不能低于30")
    @Max(value = 250, message = "心率不能高于250")
    private Integer heartRate;

    /**
     * 测量时间
     */
    @NotNull(message = "测量时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime measureTime;

    /**
     * 测量状态: rest(休息), activity(活动后), morning(晨起), evening(晚间)
     */
    @Pattern(regexp = "^(rest|activity|morning|evening)?$", message = "测量状态只能是 rest、activity、morning、evening 或为空")
    private String measureState;

    /**
     * 备注
     */
    @Size(max = 200, message = "备注不能超过200个字符")
    private String note;
} 