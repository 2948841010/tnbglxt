package org.example.tlbglxt.dto.request.health;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 健康记录查询请求DTO
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class QueryHealthRecordRequest {

    /**
     * 记录类型: glucose(血糖), pressure(血压), weight(体重)
     */
    private String recordType;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码不能小于1")
    private Integer current = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能大于100")
    private Integer size = 10;

    /**
     * 排序字段 (measureTime, value)
     */
    private String sortField = "measureTime";

    /**
     * 排序方式 (asc, desc)
     */
    private String sortOrder = "desc";
} 