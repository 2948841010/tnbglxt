package org.example.tlbglxt.service;

import org.example.tlbglxt.common.PageResult;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.dto.request.SendEmailCodeRequest;
import org.example.tlbglxt.dto.request.UserLoginRequest;
import org.example.tlbglxt.dto.request.UserRegisterRequest;
import org.example.tlbglxt.dto.response.UserLoginResponse;
import org.example.tlbglxt.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 登录响应结果
     */
    Result<UserLoginResponse> login(UserLoginRequest request);

    /**
     * 用户注册
     *
     * @param request 注册请求参数
     * @return 注册结果
     */
    Result<Void> register(UserRegisterRequest request);

    /**
     * 发送邮箱验证码
     *
     * @param request 发送验证码请求参数
     * @return 发送结果
     */
    Result<Void> sendEmailCode(SendEmailCodeRequest request);

    /**
     * 根据用户ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Result<User> getUserById(Long id);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 根据邮箱获取用户信息
     *
     * @param email 邮箱
     * @return 用户信息
     */
    User getUserByEmail(String email);

    /**
     * 验证邮箱验证码
     *
     * @param email 邮箱
     * @param code 验证码
     * @param type 验证码类型
     * @return 验证结果
     */
    boolean verifyEmailCode(String email, String code, String type);

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param avatarUrl 头像URL
     * @return 更新结果
     */
    Result<Void> updateUserAvatar(Long userId, String avatarUrl);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    Result<Void> updateUser(Long userId, User user);

    // ======================= 统计相关方法 =======================

    /**
     * 获取总用户数
     *
     * @return 总用户数
     */
    long getTotalUserCount();

    /**
     * 获取活跃用户数
     *
     * @param since 指定时间之后的用户
     * @return 活跃用户数
     */
    long getActiveUserCount(LocalDateTime since);

    /**
     * 获取在线用户数
     *
     * @param recentTime 最近活动时间
     * @return 在线用户数
     */
    long getOnlineUserCount(LocalDateTime recentTime);
    
    /**
     * 统计指定用户类型的在线用户数
     *
     * @param recentTime 最近活动时间阈值
     * @param userType 用户类型（0-普通用户，1-医生，2-管理员）
     * @return 在线用户数
     */
    long getOnlineUserCountByType(LocalDateTime recentTime, Integer userType);

    /**
     * 基于Redis统计在线用户数
     *
     * @return 所有在线用户数
     */
    long getOnlineUserCountFromRedis();

    /**
     * 基于Redis统计指定类型的在线用户数
     *
     * @param userType 用户类型（0-普通用户，1-医生，2-管理员）
     * @return 在线用户数
     */
    long getOnlineUserCountFromRedisByType(Integer userType);

    /**
     * 获取新增用户数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增用户数
     */
    long getNewUserCount(LocalDateTime startTime, LocalDateTime endTime);

    // ======================= 管理员用户管理方法 =======================

    /**
     * 分页查询用户列表（管理员）
     *
     * @param page 页码
     * @param size 页面大小
     * @param keyword 关键词搜索（用户名、邮箱、真实姓名）
     * @param userType 用户类型筛选
     * @param status 用户状态筛选
     * @param startTime 注册开始时间
     * @param endTime 注册结束时间
     * @return 分页用户列表
     */
    PageResult<User> getUserList(int page, int size, String keyword, Integer userType, Integer status,
                                LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 更新用户状态（管理员）
     *
     * @param userId 用户ID
     * @param status 状态（0-禁用，1-启用）
     * @return 更新结果
     */
    Result<Void> updateUserStatus(Long userId, Integer status);

    /**
     * 删除用户（管理员）
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    Result<Void> deleteUser(Long userId);

    /**
     * 批量删除用户（管理员）
     *
     * @param userIds 用户ID列表
     * @return 删除结果
     */
    Result<Void> batchDeleteUsers(List<Long> userIds);
} 