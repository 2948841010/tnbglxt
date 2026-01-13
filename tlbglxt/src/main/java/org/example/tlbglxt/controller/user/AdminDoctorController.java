package org.example.tlbglxt.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.PageResult;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.entity.DoctorInfo;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.service.DoctorService;
import org.example.tlbglxt.service.UserService;
import org.example.tlbglxt.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员医生管理控制器
 * 提供医生管理相关接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "管理员医生管理", description = "管理员医生管理相关接口")
@RestController
@RequestMapping("/api/v1/admin/doctors")
@Validated
public class AdminDoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 分页查询医生列表
     */
    @Operation(summary = "分页查询医生列表", description = "分页查询医生列表，支持搜索和筛选")
    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> getDoctorList(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer onlineStatus,
            @RequestParam(required = false) Integer available,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员查询医生列表，页码：{}，页面大小：{}，关键词：{}", page, size, keyword);
            
            PageResult<Map<String, Object>> result = doctorService.getDoctorListForAdmin(
                page, size, keyword, department, title, onlineStatus, available, status, startTime, endTime);
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("查询医生列表失败", e);
            return Result.error("查询医生列表失败");
        }
    }

    /**
     * 获取医生统计数据
     */
    @Operation(summary = "获取医生统计数据", description = "获取医生相关统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getDoctorStatistics(@RequestHeader("Authorization") String token) {
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员查询医生统计数据");
            
            Map<String, Object> stats = doctorService.getDoctorStatistics();
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("查询医生统计数据失败", e);
            return Result.error("查询医生统计数据失败");
        }
    }

    /**
     * 更新医生可接诊状态
     */
    @Operation(summary = "更新医生可接诊状态", description = "开启或关闭医生可接诊状态")
    @PutMapping("/{doctorId}/available")
    public Result<Void> updateDoctorAvailable(
            @PathVariable @NotNull @Min(1) Long doctorId,
            @RequestParam @NotNull Integer available,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员更新医生可接诊状态，医生ID：{}，状态：{}", doctorId, available);
            
            return doctorService.updateDoctorAvailable(doctorId, available);
            
        } catch (Exception e) {
            log.error("更新医生可接诊状态失败，医生ID：{}，状态：{}", doctorId, available, e);
            return Result.error("更新医生可接诊状态失败");
        }
    }

    /**
     * 更新医生用户状态
     */
    @Operation(summary = "更新医生用户状态", description = "启用或禁用医生用户")
    @PutMapping("/{doctorId}/status")
    public Result<Void> updateDoctorStatus(
            @PathVariable @NotNull @Min(1) Long doctorId,
            @RequestParam @NotNull Integer status,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员更新医生用户状态，医生ID：{}，状态：{}", doctorId, status);
            
            return doctorService.updateDoctorUserStatus(doctorId, status);
            
        } catch (Exception e) {
            log.error("更新医生用户状态失败，医生ID：{}，状态：{}", doctorId, status, e);
            return Result.error("更新医生用户状态失败");
        }
    }

    /**
     * 更新医生信息
     */
    @Operation(summary = "更新医生信息", description = "更新医生基本信息和详细信息")
    @PutMapping("/{doctorId}")
    public Result<Void> updateDoctor(
            @PathVariable @NotNull @Min(1) Long doctorId,
            @RequestBody @Validated Map<String, Object> doctorData,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员更新医生信息，医生ID：{}", doctorId);
            
            return doctorService.updateDoctorInfo(doctorId, doctorData);
            
        } catch (Exception e) {
            log.error("更新医生信息失败，医生ID：{}", doctorId, e);
            return Result.error("更新医生信息失败");
        }
    }

    /**
     * 删除医生
     */
    @Operation(summary = "删除医生", description = "逻辑删除指定医生")
    @DeleteMapping("/{doctorId}")
    public Result<Void> deleteDoctor(
            @PathVariable @NotNull @Min(1) Long doctorId,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员删除医生，医生ID：{}", doctorId);
            
            return doctorService.deleteDoctor(doctorId);
            
        } catch (Exception e) {
            log.error("删除医生失败，医生ID：{}", doctorId, e);
            return Result.error("删除医生失败");
        }
    }

    /**
     * 批量删除医生
     */
    @Operation(summary = "批量删除医生", description = "批量逻辑删除医生")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteDoctors(
            @RequestBody List<Long> doctorIds,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员批量删除医生，医生ID列表：{}", doctorIds);
            
            return doctorService.batchDeleteDoctors(doctorIds);
            
        } catch (Exception e) {
            log.error("批量删除医生失败，医生ID列表：{}", doctorIds, e);
            return Result.error("批量删除医生失败");
        }
    }

    /**
     * 获取医生详情
     */
    @Operation(summary = "获取医生详情", description = "获取指定医生的详细信息")
    @GetMapping("/{doctorId}")
    public Result<Map<String, Object>> getDoctorDetail(
            @PathVariable @NotNull @Min(1) Long doctorId,
            @RequestHeader("Authorization") String token) {
        
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            log.info("管理员查询医生详情，医生ID：{}", doctorId);
            
            Result<Map<String, Object>> result = doctorService.getDoctorDetailForAdmin(doctorId);
            
            return result;
            
        } catch (Exception e) {
            log.error("查询医生详情失败，医生ID：{}", doctorId, e);
            return Result.error("查询医生详情失败");
        }
    }

    /**
     * 验证是否为管理员
     */
    private boolean isAdmin(String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return false;
            }

            String token = authorization.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return false;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                return false;
            }

            Result<User> userResult = userService.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return false;
            }

            User user = userResult.getData();
            return user.getUserType() != null && user.getUserType() == 2;

        } catch (Exception e) {
            log.error("验证管理员权限失败", e);
            return false;
        }
    }
} 