package org.example.tlbglxt.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.dto.request.SendEmailCodeRequest;
import org.example.tlbglxt.dto.request.UserLoginRequest;
import org.example.tlbglxt.dto.request.UserRegisterRequest;
import org.example.tlbglxt.dto.response.UserLoginResponse;
import org.example.tlbglxt.entity.DoctorInfo;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.service.DoctorService;
import org.example.tlbglxt.service.UserService;
import org.example.tlbglxt.service.WebSocketService;
import org.example.tlbglxt.util.JwtUtil;
import org.example.tlbglxt.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 用户认证控制器
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "用户认证", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private RedisUtil redisUtil;

    // ============================== 用户认证 ==============================

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "支持用户名/邮箱+密码登录")
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        log.info("用户登录请求，用户名：{}", request.getUsername());
        return userService.login(request);
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "用户注册，需要邮箱验证码")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid UserRegisterRequest request) {
        log.info("用户注册请求，用户名：{}，邮箱：{}", request.getUsername(), request.getEmail());
        return userService.register(request);
    }

    /**
     * 发送邮箱验证码
     */
    @Operation(summary = "发送邮箱验证码", description = "发送注册或重置密码验证码")
    @PostMapping("/send-email-code")
    public Result<Void> sendEmailCode(@RequestBody @Valid SendEmailCodeRequest request) {
        log.info("发送邮箱验证码请求，邮箱：{}，类型：{}", request.getEmail(), request.getType());
        return userService.sendEmailCode(request);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "清除用户登录状态")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("用户登出请求，token：{}", token);
        
        try {
            // 从JWT令牌中获取用户信息
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                String username = jwtUtil.getUsernameFromToken(jwtToken);
                Long userId = jwtUtil.getUserIdFromToken(jwtToken);
                
                if (userId != null) {
                    // 清理Redis中的用户在线状态
                    try {
                        String onlineKey = "user:online:" + userId;
                        redisUtil.del(onlineKey);
                        log.info("清理用户在线状态，用户ID：{}", userId);
                    } catch (Exception redisException) {
                        log.warn("清理Redis在线状态失败，用户ID：{}，错误：{}", userId, redisException.getMessage());
                    }
                    
                    // 获取用户信息
                    Result<User> userResult = userService.getUserById(userId);
                    if (userResult.isSuccess() && userResult.getData() != null) {
                        User user = userResult.getData();
                        if (user.getUserType() == 1) {
                            // 如果是医生用户，更新在线状态为离线
                            try {
                                doctorService.updateOnlineStatus(userId, 0); // 0表示离线
                                log.info("医生用户登出，已更新在线状态为离线，用户ID：{}", userId);
                                
                                // 广播医生状态变化
                                try {
                                    // 获取医生详细信息用于广播
                                    Result<DoctorInfo> doctorResult = doctorService.getDoctorInfoByUserId(userId);
                                    if (doctorResult.isSuccess() && doctorResult.getData() != null) {
                                        DoctorInfo doctorInfo = doctorResult.getData();
                                        webSocketService.broadcastDoctorStatusChange(
                                            userId, 
                                            user.getRealName(), 
                                            doctorInfo.getDepartment(), 
                                            0 // 离线状态
                                        );
                                    }
                                } catch (Exception wsException) {
                                    log.warn("广播医生状态变化失败，用户ID：{}，错误：{}", userId, wsException.getMessage());
                                    // WebSocket广播失败不影响登出流程
                                }
                                
                            } catch (Exception e) {
                                log.warn("更新医生在线状态失败，用户ID：{}，错误：{}", userId, e.getMessage());
                            }
                        }
                    }
                }
            }
            
            // TODO: 实现JWT令牌黑名单功能
            return Result.success("登出成功");
            
        } catch (Exception e) {
            log.error("用户登出处理失败", e);
            // 即使处理失败，也返回成功，不影响用户登出
            return Result.success("登出成功");
        }
    }

    /**
     * 刷新访问令牌
     */
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh-token")
    public Result<UserLoginResponse> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        log.info("刷新令牌请求");
        // TODO: 实现刷新令牌功能
        return Result.success("令牌刷新成功");
    }
} 