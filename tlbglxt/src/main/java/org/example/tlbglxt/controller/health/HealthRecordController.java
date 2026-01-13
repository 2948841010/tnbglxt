package org.example.tlbglxt.controller.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.dto.request.health.AddBloodGlucoseRequest;
import org.example.tlbglxt.dto.request.health.AddBloodPressureRequest;
import org.example.tlbglxt.dto.request.health.AddWeightRequest;
import org.example.tlbglxt.dto.request.health.QueryHealthRecordRequest;
import org.example.tlbglxt.dto.response.health.BloodGlucoseRecordResponse;
import org.example.tlbglxt.dto.response.health.BloodPressureRecordResponse;
import org.example.tlbglxt.service.HealthRecordService;
import org.example.tlbglxt.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康记录控制器
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "健康数据管理", description = "用户健康数据相关接口")
@RestController
@RequestMapping("/api/v1/health")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 🔧 修复血糖统计数据 - 重新计算当前用户的血糖统计
     */
    @Operation(summary = "修复血糖统计数据", description = "重新计算血糖统计信息（修复normalCount和highCount反转问题）")
    @PostMapping("/glucose/fix-statistics")
    public Result<String> fixBloodGlucoseStatistics(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            log.info("🔧 开始修复用户 {} 的血糖统计数据", userId);
            boolean success = healthRecordService.recalculateBloodGlucoseStatistics(userId);
            
            if (success) {
                log.info("✅ 用户 {} 的血糖统计数据修复成功", userId);
                return Result.success("血糖统计数据已重新计算");
            } else {
                return Result.error("未找到血糖记录或重新计算失败");
            }
        } catch (Exception e) {
            log.error("修复血糖统计数据失败", e);
            return Result.error("修复失败: " + e.getMessage());
        }
    }

    /**
     * 添加血糖记录
     */
    @Operation(summary = "添加血糖记录", description = "用户添加血糖测量记录")
    @PostMapping("/glucose")
    public Result<Boolean> addBloodGlucoseRecord(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody AddBloodGlucoseRequest request) {
        
        log.info("添加血糖记录请求，测量值：{}mmol/L，测量时间：{}", 
                request.getValue(), request.getMeasureTime());
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.addBloodGlucoseRecord(userId, request);
        
        return Result.success("血糖记录添加成功", result);
    }

    /**
     * 更新血糖记录
     */
    @Operation(summary = "更新血糖记录", description = "更新指定ID的血糖记录")
    @PutMapping("/glucose")
    public Result<Boolean> updateBloodGlucoseRecord(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "记录ID") @RequestParam String recordId,
            @Valid @RequestBody AddBloodGlucoseRequest request) {
        
        log.info("更新血糖记录请求，记录ID：{}，新测量值：{}mmol/L", 
                recordId, request.getValue());
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.updateBloodGlucoseRecord(userId, recordId, request);
        
        return Result.success("血糖记录更新成功", result);
    }

    /**
     * 添加血压记录
     */
    @Operation(summary = "添加血压记录", description = "用户添加血压测量记录")
    @PostMapping("/pressure")
    public Result<Boolean> addBloodPressureRecord(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody AddBloodPressureRequest request) {
        
        log.info("添加血压记录请求，收缩压：{}mmHg，舒张压：{}mmHg，测量时间：{}", 
                request.getSystolic(), request.getDiastolic(), request.getMeasureTime());
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.addBloodPressureRecord(userId, request);
        
        return Result.success("血压记录添加成功", result);
    }

    /**
     * 更新血压记录
     */
    @Operation(summary = "更新血压记录", description = "更新指定ID的血压记录")
    @PutMapping("/pressure")
    public Result<Boolean> updateBloodPressureRecord(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "记录ID") @RequestParam String recordId,
            @Valid @RequestBody AddBloodPressureRequest request) {
        
        log.info("更新血压记录请求，记录ID：{}，新收缩压：{}mmHg，新舒张压：{}mmHg", 
                recordId, request.getSystolic(), request.getDiastolic());
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.updateBloodPressureRecord(userId, recordId, request);
        
        return Result.success("血压记录更新成功", result);
    }

    /**
     * 获取血糖记录
     */
    @Operation(summary = "获取血糖记录", description = "查询用户血糖记录")
    @GetMapping("/glucose")
    public Result<BloodGlucoseRecordResponse> getBloodGlucoseRecords(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "measureTime") String sortField,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "desc") String sortOrder) {
        
        log.info("获取血糖记录请求，时间范围：{} - {}", startTime, endTime);
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 构建查询请求
        QueryHealthRecordRequest queryRequest = new QueryHealthRecordRequest();
        queryRequest.setRecordType("glucose");
        queryRequest.setStartTime(startTime);
        queryRequest.setEndTime(endTime);
        queryRequest.setCurrent(current);
        queryRequest.setSize(size);
        queryRequest.setSortField(sortField);
        queryRequest.setSortOrder(sortOrder);
        
        BloodGlucoseRecordResponse response = healthRecordService.getBloodGlucoseRecords(userId, queryRequest);
        
        return Result.success("血糖记录查询成功", response);
    }

    /**
     * 获取血压记录
     */
    @Operation(summary = "获取血压记录", description = "查询用户血压记录")
    @GetMapping("/pressure")
    public Result<BloodPressureRecordResponse> getBloodPressureRecords(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "measureTime") String sortField,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "desc") String sortOrder) {
        
        log.info("获取血压记录请求，时间范围：{} - {}", startTime, endTime);
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 构建查询请求
        QueryHealthRecordRequest queryRequest = new QueryHealthRecordRequest();
        queryRequest.setRecordType("pressure");
        queryRequest.setStartTime(startTime);
        queryRequest.setEndTime(endTime);
        queryRequest.setCurrent(current);
        queryRequest.setSize(size);
        queryRequest.setSortField(sortField);
        queryRequest.setSortOrder(sortOrder);
        
        BloodPressureRecordResponse response = healthRecordService.getBloodPressureRecords(userId, queryRequest);
        
        return Result.success("血压记录查询成功", response);
    }

    /**
     * 删除血糖记录
     */
    @Operation(summary = "删除血糖记录", description = "删除指定ID的血糖记录")
    @DeleteMapping("/glucose")
    public Result<Boolean> deleteBloodGlucoseRecord(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "记录ID") @RequestParam String recordId) {
        
        log.info("删除血糖记录请求，记录ID：{}", recordId);
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.deleteBloodGlucoseRecord(userId, recordId);
        
        return Result.success("血糖记录删除成功", result);
    }

    /**
     * 批量删除血糖记录
     */
    @Operation(summary = "批量删除血糖记录", description = "批量删除多个血糖记录")
    @DeleteMapping("/glucose/batch")
    public Result<Boolean> batchDeleteBloodGlucoseRecords(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "记录ID列表") 
            @RequestBody List<String> recordIds) {
        
        log.info("批量删除血糖记录请求，记录数量：{}", recordIds.size());
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.batchDeleteBloodGlucoseRecords(userId, recordIds);
        
        return Result.success("血糖记录批量删除成功", result);
    }

    /**
     * 删除血压记录
     */
    @Operation(summary = "删除血压记录", description = "删除指定ID的血压记录")
    @DeleteMapping("/pressure")
    public Result<Boolean> deleteBloodPressureRecord(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "记录ID") @RequestParam String recordId) {
        
        log.info("删除血压记录请求，记录ID：{}", recordId);
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.deleteBloodPressureRecord(userId, recordId);
        
        return Result.success("血压记录删除成功", result);
    }

    /**
     * 批量删除血压记录
     */
    @Operation(summary = "批量删除血压记录", description = "批量删除多个血压记录")
    @DeleteMapping("/pressure/batch")
    public Result<Boolean> batchDeleteBloodPressureRecords(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "记录ID列表") 
            @RequestBody List<String> recordIds) {
        
        log.info("批量删除血压记录请求，记录数量：{}", recordIds.size());
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.batchDeleteBloodPressureRecords(userId, recordIds);
        
        return Result.success("血压记录批量删除成功", result);
    }

    /**
     * 获取健康统计信息
     */
    @Operation(summary = "获取健康统计", description = "获取用户健康数据统计信息")
    @GetMapping("/statistics")
    public Result<Object> getHealthStatistics(@RequestHeader("Authorization") String authorization) {
        
        log.info("获取健康统计信息请求");
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Object statistics = healthRecordService.getHealthStatistics(userId);
        
        return Result.success("健康统计查询成功", statistics);
    }

    /**
     * 获取健康数据趋势
     */
    @Operation(summary = "获取健康数据趋势", description = "获取指定天数的健康数据趋势")
    @GetMapping("/trend")
    public Result<Object> getHealthDataTrend(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "数据类型") @RequestParam String dataType,
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        
        log.info("获取健康数据趋势请求，数据类型：{}，天数：{}", dataType, days);
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Object trend = healthRecordService.getHealthDataTrend(userId, dataType, days);
        
        return Result.success("健康数据趋势查询成功", trend);
    }

    /**
     * 初始化用户健康档案
     */
    @Operation(summary = "初始化健康档案", description = "为新用户初始化健康档案")
    @PostMapping("/profile/init")
    public Result<Boolean> initUserHealthProfile(@RequestHeader("Authorization") String authorization) {
        
        log.info("初始化用户健康档案请求");
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.initUserHealthProfile(userId);
        
        return Result.success("健康档案初始化成功", result);
    }

    // ===== 体重记录相关接口 =====

    /**
     * 添加体重记录
     */
    @Operation(summary = "添加体重记录", description = "用户添加体重测量记录")
    @PostMapping("/weight")
    public Result<Boolean> addWeightRecord(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody AddWeightRequest request) {
        
        log.info("添加体重记录请求: {}", request);
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.addWeightRecord(userId, request);
        
        return Result.success("体重记录添加成功", result);
    }

    /**
     * 更新体重记录
     */
    @Operation(summary = "更新体重记录", description = "更新指定ID的体重记录")
    @PutMapping("/weight")
    public Result<Boolean> updateWeightRecord(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "记录ID") @RequestParam String recordId,
            @Valid @RequestBody AddWeightRequest request) {
        
        log.info("更新体重记录请求: recordId={}, request={}", recordId, request);
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.updateWeightRecord(userId, recordId, request);
        
        return Result.success("体重记录更新成功", result);
    }

    /**
     * 获取体重记录
     */
    @Operation(summary = "获取体重记录", description = "获取用户的体重记录列表")
    @GetMapping("/weight")
    public Result<Object> getWeightRecords(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "开始时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {
        
        log.info("获取体重记录请求: startTime={}, endTime={}, current={}, size={}", 
                startTime, endTime, current, size);
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 构建查询请求
        QueryHealthRecordRequest request = new QueryHealthRecordRequest();
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setCurrent(current);
        request.setSize(size);
        
        Object result = healthRecordService.getWeightRecords(userId, request);
        
        return Result.success("获取体重记录成功", result);
    }

    /**
     * 删除体重记录
     */
    @Operation(summary = "删除体重记录", description = "删除指定ID的体重记录")
    @DeleteMapping("/weight")
    public Result<Boolean> deleteWeightRecord(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "记录ID") @RequestParam String recordId) {
        
        log.info("删除体重记录请求: recordId={}", recordId);
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.deleteWeightRecord(userId, recordId);
        
        return Result.success("体重记录删除成功", result);
    }

    /**
     * 批量删除体重记录
     */
    @Operation(summary = "批量删除体重记录", description = "批量删除多条体重记录")
    @DeleteMapping("/weight/batch")
    public Result<Boolean> batchDeleteWeightRecords(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "记录ID列表") 
            @RequestBody List<String> recordIds) {
        
        log.info("批量删除体重记录请求: recordIds={}", recordIds);
        
        // 从JWT令牌中获取用户ID
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Boolean result = healthRecordService.batchDeleteWeightRecords(userId, recordIds);
        
        return Result.success("批量删除体重记录成功", result);
    }

    // ===== 医生端专用接口 =====
    
    /**
     * 医生查询患者血糖趋势
     */
    @Operation(summary = "医生查询患者血糖趋势", description = "医生端查询指定患者的血糖趋势数据")
    @GetMapping("/doctor/patient/{patientId}/glucose/trend")
    public Result<Object> getPatientBloodGlucoseTrend(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "患者用户ID") @PathVariable Long patientId,
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        
        log.info("医生查询患者血糖趋势请求，患者ID：{}，天数：{}", patientId, days);
        
        // 从JWT令牌中获取医生ID并验证权限
        String token = authorization.replace("Bearer ", "");
        Long doctorId = jwtUtil.getUserIdFromToken(token);
        
        log.info("请求的医生ID：{}，查询患者ID：{}", doctorId, patientId);
        
        // TODO: 这里应该添加权限验证，确保医生有权查看该患者的数据
        // 可以通过检查是否存在活跃的咨询记录来验证
        // 临时允许所有医生查看所有患者数据（仅用于开发测试）
        
        Object trend = healthRecordService.getHealthDataTrend(patientId, "glucose", days);
        
        log.info("血糖趋势数据查询完成，患者ID：{}，返回数据：{}", patientId, trend);
        
        return Result.success("患者血糖趋势查询成功", trend);
    }
    
    /**
     * 医生查询患者血压趋势
     */
    @Operation(summary = "医生查询患者血压趋势", description = "医生端查询指定患者的血压趋势数据")
    @GetMapping("/doctor/patient/{patientId}/pressure/trend")
    public Result<Object> getPatientBloodPressureTrend(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "患者用户ID") @PathVariable Long patientId,
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        
        log.info("医生查询患者血压趋势请求，患者ID：{}，天数：{}", patientId, days);
        
        // 从JWT令牌中获取医生ID并验证权限
        String token = authorization.replace("Bearer ", "");
        Long doctorId = jwtUtil.getUserIdFromToken(token);
        
        log.info("请求的医生ID：{}，查询患者ID：{}", doctorId, patientId);
        
        // TODO: 这里应该添加权限验证，确保医生有权查看该患者的数据
        // 可以通过检查是否存在活跃的咨询记录来验证
        // 临时允许所有医生查看所有患者数据（仅用于开发测试）
        
        Object trend = healthRecordService.getHealthDataTrend(patientId, "pressure", days);
        
        log.info("血压趋势数据查询完成，患者ID：{}，返回数据：{}", patientId, trend);
        
        return Result.success("患者血压趋势查询成功", trend);
    }
    
    /**
     * 医生查询患者健康数据概览
     */
    @Operation(summary = "医生查询患者健康数据概览", description = "医生端查询指定患者的健康数据概览")
    @GetMapping("/doctor/patient/{patientId}/overview")
    public Result<Object> getPatientHealthOverview(
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "患者用户ID") @PathVariable Long patientId) {
        
        log.info("医生查询患者健康数据概览请求，患者ID：{}", patientId);
        
        // 从JWT令牌中获取医生ID并验证权限
        String token = authorization.replace("Bearer ", "");
        Long doctorId = jwtUtil.getUserIdFromToken(token);
        
        log.info("请求的医生ID：{}，查询患者ID：{}", doctorId, patientId);
        
        // TODO: 这里应该添加权限验证，确保医生有权查看该患者的数据
        // 临时允许所有医生查看所有患者数据（仅用于开发测试）
        
        Object statistics = healthRecordService.getHealthStatistics(patientId);
        
        log.info("健康概览数据查询完成，患者ID：{}，返回数据：{}", patientId, statistics);
        
        return Result.success("患者健康数据概览查询成功", statistics);
    }


} 