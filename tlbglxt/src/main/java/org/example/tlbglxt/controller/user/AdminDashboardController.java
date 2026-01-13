package org.example.tlbglxt.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.service.ConsultationChatService;
import org.example.tlbglxt.service.DoctorService;
import org.example.tlbglxt.service.UserService;
import org.example.tlbglxt.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员数据看板控制器
 * 提供各种统计数据接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "管理员数据看板", description = "管理员数据看板统计接口")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ConsultationChatService consultationChatService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取系统统计数据
     */
    @Operation(summary = "获取系统统计数据", description = "获取系统整体统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getSystemStats(@RequestHeader("Authorization") String token) {
        try {
            // 验证管理员权限
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            Map<String, Object> stats = new HashMap<>();
            
            // 获取用户统计
            Map<String, Object> userStats = getUserStatistics();
            stats.put("users", userStats);
            
            // 获取医生统计
            Map<String, Object> doctorStats = getDoctorStatistics();
            stats.put("doctors", doctorStats);
            
            // 获取咨询统计
            Map<String, Object> consultationStats = getConsultationStatistics();
            stats.put("consultations", consultationStats);
            
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取系统统计数据失败", e);
            return Result.error("获取统计数据失败");
        }
    }

    /**
     * 获取用户统计数据
     */
    @Operation(summary = "获取用户统计数据", description = "获取用户相关统计数据")
    @GetMapping("/users")
    public Result<Map<String, Object>> getUserStats(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            Map<String, Object> userStats = getUserStatistics();
            return Result.success(userStats);
            
        } catch (Exception e) {
            log.error("获取用户统计数据失败", e);
            return Result.error("获取用户统计数据失败");
        }
    }

    /**
     * 获取医生统计数据
     */
    @Operation(summary = "获取医生统计数据", description = "获取医生相关统计数据")
    @GetMapping("/doctors")
    public Result<Map<String, Object>> getDoctorStats(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            Map<String, Object> doctorStats = getDoctorStatistics();
            return Result.success(doctorStats);
            
        } catch (Exception e) {
            log.error("获取医生统计数据失败", e);
            return Result.error("获取医生统计数据失败");
        }
    }

    /**
     * 获取咨询统计数据
     */
    @Operation(summary = "获取咨询统计数据", description = "获取咨询相关统计数据")
    @GetMapping("/consultations")
    public Result<Map<String, Object>> getConsultationStats(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            Map<String, Object> consultationStats = getConsultationStatistics();
            return Result.success(consultationStats);
            
        } catch (Exception e) {
            log.error("获取咨询统计数据失败", e);
            return Result.error("获取咨询统计数据失败");
        }
    }

    /**
     * 获取趋势数据
     */
    @Operation(summary = "获取趋势数据", description = "获取各种趋势统计数据")
    @GetMapping("/trends")
    public Result<Map<String, Object>> getTrends(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "7") int days) {
        try {
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            Map<String, Object> trends = new HashMap<>();
            
            // 用户注册趋势
            trends.put("userRegistration", getUserRegistrationTrend(days));
            
            // 咨询量趋势
            trends.put("consultationVolume", getConsultationVolumeTrend(days));
            
            return Result.success(trends);
            
        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            return Result.error("获取趋势数据失败");
        }
    }

    /**
     * 获取实时数据
     */
    @Operation(summary = "获取实时数据", description = "获取实时在线用户和医生数据")
    @GetMapping("/realtime")
    public Result<Map<String, Object>> getRealTimeData(@RequestHeader("Authorization") String token) {
        try {
            if (!isAdmin(token)) {
                return Result.error("无权限访问");
            }

            Map<String, Object> realTimeData = new HashMap<>();
            
            // 在线用户数（基于Redis的真实在线状态）
            realTimeData.put("onlineUsers", userService.getOnlineUserCountFromRedis());
            
            // 在线医生数
            realTimeData.put("onlineDoctors", doctorService.getOnlineDoctorCount());
            
            // 正在进行的咨询数
            realTimeData.put("ongoingConsultations", consultationChatService.getOngoingConsultationCount());
            
            return Result.success(realTimeData);
            
        } catch (Exception e) {
            log.error("获取实时数据失败", e);
            return Result.error("获取实时数据失败");
        }
    }

    // ========================= 私有方法 =========================

    /**
     * 验证是否为管理员用户
     */
    private boolean isAdmin(String token) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return false;
            }
            
            String jwtToken = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(jwtToken);
            
            if (userId == null) {
                return false;
            }
            
            Result<org.example.tlbglxt.entity.User> userResult = userService.getUserById(userId);
            return userResult.isSuccess() && 
                   userResult.getData() != null && 
                   userResult.getData().getUserType() == 2;
                   
        } catch (Exception e) {
            log.warn("验证管理员权限失败", e);
            return false;
        }
    }

    /**
     * 获取用户统计数据
     */
    private Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        stats.put("total", userService.getTotalUserCount());
        
        // 活跃用户数（最近30天登录）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        stats.put("active", userService.getActiveUserCount(thirtyDaysAgo));
        
        // 今日新增用户数
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        stats.put("newToday", userService.getNewUserCount(todayStart, todayEnd));
        
        // 在线用户数（基于Redis的真实在线状态）
        long onlinePatients = userService.getOnlineUserCountFromRedisByType(0); // 普通用户
        long onlineDoctors = userService.getOnlineUserCountFromRedisByType(1); // 医生用户
        long onlineAdmins = userService.getOnlineUserCountFromRedisByType(2); // 管理员用户
        stats.put("online", onlinePatients + onlineDoctors + onlineAdmins);
        
        return stats;
    }

    /**
     * 获取医生统计数据
     */
    private Map<String, Object> getDoctorStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总医生数
        stats.put("totalDoctors", doctorService.getTotalDoctorCount());
        
        // 活跃医生数（最近30天登录）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        stats.put("activeDoctors", doctorService.getActiveDoctorCount(thirtyDaysAgo));
        
        // 在线医生数
        stats.put("onlineDoctors", doctorService.getOnlineDoctorCount());
        
        return stats;
    }

    /**
     * 获取咨询统计数据
     */
    private Map<String, Object> getConsultationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总咨询数
        stats.put("totalConsultations", consultationChatService.getTotalConsultationCount());
        
        // 今日咨询数
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        stats.put("todayConsultations", consultationChatService.getConsultationCountByDateRange(todayStart, todayEnd));
        
        // 总已完成咨询数
        stats.put("totalCompletedConsultations", consultationChatService.getCompletedConsultationCount());
        
        // 今日已完成咨询数
        stats.put("todayCompletedConsultations", consultationChatService.getCompletedConsultationCountByDate(todayStart, todayEnd));
        
        // 正在进行的咨询数
        stats.put("ongoingConsultations", consultationChatService.getOngoingConsultationCount());
        
        return stats;
    }

    /**
     * 获取用户注册趋势
     */
    private Map<String, Object> getUserRegistrationTrend(int days) {
        // TODO: 实现用户注册趋势统计
        Map<String, Object> trend = new HashMap<>();
        trend.put("message", "用户注册趋势功能待实现");
        return trend;
    }

    /**
     * 获取咨询量趋势
     */
    private Map<String, Object> getConsultationVolumeTrend(int days) {
        // TODO: 实现咨询量趋势统计
        Map<String, Object> trend = new HashMap<>();
        trend.put("message", "咨询量趋势功能待实现");
        return trend;
    }
} 