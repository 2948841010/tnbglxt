package org.example.tlbglxt.controller.doctor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.common.ResultCode;
import org.example.tlbglxt.entity.DoctorInfo;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.exception.BusinessException;
import org.example.tlbglxt.service.DoctorService;
import org.example.tlbglxt.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 医生管理控制器
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "医生管理", description = "医生信息管理相关接口")
@RestController
@RequestMapping("/api/v1/doctor")
@Validated
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private JwtUtil jwtUtil;

    // ============================== 医生信息查询 ==============================

    /**
     * 获取当前医生信息
     */
    @Operation(summary = "获取当前医生信息", description = "获取当前登录医生的完整信息，包括基础信息和专业信息")
    @GetMapping("/profile")
    public Result<DoctorInfo> getCurrentDoctorProfile(@RequestHeader("Authorization") String authorization) {
        log.info("获取当前医生信息请求");
        
        try {
            // 1. 验证Authorization头格式
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未提供有效的授权令牌");
            }

            // 2. 提取JWT令牌（去掉"Bearer "前缀）
            String token = authorization.substring(7);
            
            // 3. 验证令牌有效性
            if (!jwtUtil.validateToken(token)) {
                throw new BusinessException(ResultCode.TOKEN_EXPIRED.getCode(), "令牌已过期或无效");
            }

            // 4. 从令牌中获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "无法从令牌中获取用户信息");
            }

            // 5. 获取医生完整信息
            Result<DoctorInfo> result = doctorService.getDoctorInfoByUserId(userId);
            
            log.info("获取当前医生信息成功，用户ID：{}", userId);
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取当前医生信息失败", e);
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "获取医生信息失败");
        }
    }

    /**
     * 根据医生编号获取医生信息
     */
    @Operation(summary = "根据医生编号获取医生信息", description = "根据医生编号获取医生的公开信息")
    @GetMapping("/info/{doctorNo}")
    public Result<DoctorInfo> getDoctorInfoByDoctorNo(@PathVariable String doctorNo) {
        log.info("根据医生编号获取医生信息请求，医生编号：{}", doctorNo);
        return doctorService.getDoctorInfoByDoctorNo(doctorNo);
    }

    // ============================== 医生信息更新 ==============================

    /**
     * 更新医生个人信息
     */
    @Operation(summary = "更新医生个人信息", description = "更新医生的基础信息和专业信息")
    @PutMapping("/profile")
    public Result<Void> updateDoctorProfile(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid DoctorProfileUpdateRequest request) {
        log.info("更新医生个人信息请求");
        
        try {
            // 1. 验证Authorization头格式
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未提供有效的授权令牌");
            }

            // 2. 提取JWT令牌（去掉"Bearer "前缀）
            String token = authorization.substring(7);
            
            // 3. 验证令牌有效性
            if (!jwtUtil.validateToken(token)) {
                throw new BusinessException(ResultCode.TOKEN_EXPIRED.getCode(), "令牌已过期或无效");
            }

            // 4. 从令牌中获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "无法从令牌中获取用户信息");
            }

            // 5. 更新医生信息
            Result<Void> result = doctorService.updateDoctorProfile(userId, request.getUser(), request.getDoctorInfo());
            
            log.info("更新医生个人信息成功，用户ID：{}", userId);
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新医生个人信息失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "更新医生信息失败");
        }
    }

    /**
     * 更新医生在线状态
     */
    @Operation(summary = "更新医生在线状态", description = "更新医生的在线状态（0-离线，1-在线，2-忙碌）")
    @PutMapping("/online-status")
    public Result<Void> updateOnlineStatus(
            @RequestHeader("Authorization") String authorization,
            @RequestBody OnlineStatusUpdateRequest request) {
        log.info("更新医生在线状态请求，状态：{}", request.getOnlineStatus());
        
        try {
            // 1. 验证Authorization头格式
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "未提供有效的授权令牌");
            }

            // 2. 提取JWT令牌（去掉"Bearer "前缀）
            String token = authorization.substring(7);
            
            // 3. 验证令牌有效性
            if (!jwtUtil.validateToken(token)) {
                throw new BusinessException(ResultCode.TOKEN_EXPIRED.getCode(), "令牌已过期或无效");
            }

            // 4. 从令牌中获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "无法从令牌中获取用户信息");
            }

            // 5. 更新在线状态
            Result<Void> result = doctorService.updateOnlineStatus(userId, request.getOnlineStatus());
            
            log.info("更新医生在线状态成功，用户ID：{}", userId);
            return result;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新医生在线状态失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "更新在线状态失败");
        }
    }

    // ============================== 内部类 - 请求对象 ==============================

    /**
     * 医生信息更新请求对象
     */
    public static class DoctorProfileUpdateRequest {
        private User user;
        private DoctorInfo doctorInfo;

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public DoctorInfo getDoctorInfo() {
            return doctorInfo;
        }

        public void setDoctorInfo(DoctorInfo doctorInfo) {
            this.doctorInfo = doctorInfo;
        }
    }

    /**
     * 在线状态更新请求对象
     */
    public static class OnlineStatusUpdateRequest {
        private Integer onlineStatus;

        public Integer getOnlineStatus() {
            return onlineStatus;
        }

        public void setOnlineStatus(Integer onlineStatus) {
            this.onlineStatus = onlineStatus;
        }
    }
} 