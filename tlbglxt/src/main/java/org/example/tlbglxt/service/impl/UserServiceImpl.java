package org.example.tlbglxt.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.PageResult;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.common.ResultCode;
import org.example.tlbglxt.dto.request.SendEmailCodeRequest;
import org.example.tlbglxt.dto.request.UserLoginRequest;
import org.example.tlbglxt.dto.request.UserRegisterRequest;
import org.example.tlbglxt.dto.response.UserLoginResponse;
import org.example.tlbglxt.entity.DoctorInfo;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.exception.BusinessException;
import org.example.tlbglxt.mapper.UserMapper;
import org.example.tlbglxt.service.DoctorService;
import org.example.tlbglxt.service.EmailService;
import org.example.tlbglxt.service.UserService;
import org.example.tlbglxt.service.WebSocketService;
import org.example.tlbglxt.util.JwtUtil;
import org.example.tlbglxt.util.Md5Util;
import org.example.tlbglxt.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 用户服务实现类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String EMAIL_CODE_PREFIX = "email_code:";
    private static final Integer EMAIL_CODE_EXPIRE = 300; // 5分钟
    private static final Integer EMAIL_CODE_LENGTH = 6;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisUtil redisUtil;

    @Lazy
    @Autowired
    private DoctorService doctorService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private JwtUtil jwtUtil;

    // ============================== 用户认证 ==============================

    @Override
    public Result<UserLoginResponse> login(UserLoginRequest request) {
        log.info("用户登录，用户名：{}，用户类型：{}", request.getUsername(), request.getUserType());

        try {
            // 参数校验
            if (request.getUsername() == null || request.getPassword() == null || request.getUserType() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }

            // 获取用户信息（支持用户名或邮箱登录）
            User user = getUserByUsernameOrEmail(request.getUsername());
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }

            // 检查用户类型是否匹配
            if (!user.getUserType().equals(request.getUserType())) {
                if (request.getUserType() == 1) {
                    throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该账号不是医生账号，请使用患者登录");
                } else if (request.getUserType() == 2) {
                    throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该账号不是管理员账号，无权限访问管理系统");
                } else {
                    throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该账号不是患者账号，请使用医生登录");
                }
            }

            // 检查用户状态
            if (user.getStatus() == 0) {
                throw new BusinessException(ResultCode.USER_DISABLED);
            }

            // 验证密码（使用MD5）
            if (!Md5Util.matches(request.getPassword(), user.getPassword())) {
                throw new BusinessException(ResultCode.PASSWORD_ERROR);
            }

            // 更新登录信息
            updateLoginInfo(user.getId());

            // 如果是医生用户，更新在线状态为在线
            if (user.getUserType() == 1) {
                try {
                    doctorService.updateOnlineStatus(user.getId(), 1); // 1表示在线
                    log.info("医生用户登录，已更新在线状态，用户ID：{}", user.getId());
                    
                    // 广播医生状态变化
                    try {
                        // 获取医生详细信息用于广播
                        Result<DoctorInfo> doctorResult = doctorService.getDoctorInfoByUserId(user.getId());
                        if (doctorResult.isSuccess() && doctorResult.getData() != null) {
                            DoctorInfo doctorInfo = doctorResult.getData();
                            webSocketService.broadcastDoctorStatusChange(
                                user.getId(), 
                                user.getRealName(), 
                                doctorInfo.getDepartment(), 
                                1 // 在线状态
                            );
                        }
                    } catch (Exception wsException) {
                        log.warn("广播医生状态变化失败，用户ID：{}，错误：{}", user.getId(), wsException.getMessage());
                        // WebSocket广播失败不影响登录流程
                    }
                    
                } catch (Exception e) {
                    log.warn("更新医生在线状态失败，用户ID：{}，错误：{}", user.getId(), e.getMessage());
                    // 在线状态更新失败不影响登录流程
                }
            }

            // 生成JWT令牌（包含用户类型）
            String accessToken = jwtUtil.generateAccessTokenWithUserType(user.getUsername(), user.getId(), user.getUserType());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());

            // 在Redis中设置用户在线状态
            String onlineKey = "user:online:" + user.getId();
            redisUtil.set(onlineKey, user.getUserType(), 86400); // 设置24小时过期，与token一致
            log.info("设置用户在线状态，用户ID：{}，类型：{}", user.getId(), user.getUserType());

            // 构建响应结果
            UserLoginResponse response = new UserLoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(86400L); // 24小时
            response.setUserInfo(user);

            log.info("用户登录成功，用户ID：{}", user.getId());
            return Result.success("登录成功", response);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登录失败，用户名：{}", request.getUsername(), e);
            throw new BusinessException(ResultCode.ERROR);
        }
    }

    @Override
    public Result<Void> register(UserRegisterRequest request) {
        log.info("用户注册，用户名：{}，邮箱：{}", request.getUsername(), request.getEmail());

        try {
            // 验证密码一致性
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "两次输入的密码不一致");
            }

            // 验证邮箱验证码
            if (!verifyEmailCode(request.getEmail(), request.getEmailCode(), "register")) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "邮箱验证码错误或已过期");
            }

            // 检查用户名是否已存在
            if (getUserByUsername(request.getUsername()) != null) {
                throw new BusinessException(ResultCode.USER_ALREADY_EXIST.getCode(), "用户名已存在");
            }

            // 检查邮箱是否已存在
            if (getUserByEmail(request.getEmail()) != null) {
                throw new BusinessException(ResultCode.USER_ALREADY_EXIST.getCode(), "邮箱已被注册");
            }

            // 创建用户对象
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(Md5Util.encrypt(request.getPassword())); // 使用MD5加密
            user.setEmail(request.getEmail());
            user.setRealName(request.getRealName());
            user.setPhone(request.getPhone());
            user.setUserType(0); // 普通用户
            user.setStatus(1); // 启用状态
            user.setLoginCount(0);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setIsDeleted(0);

            // 保存用户
            int result = userMapper.insertUser(user);
            if (result <= 0) {
                throw new BusinessException(ResultCode.DATA_SAVE_ERROR);
            }

            // 清除验证码
            String codeKey = EMAIL_CODE_PREFIX + "register:" + request.getEmail();
            redisUtil.del(codeKey);

            log.info("用户注册成功，用户ID：{}", user.getId());
            return Result.success("注册成功");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注册失败，用户名：{}", request.getUsername(), e);
            throw new BusinessException(ResultCode.ERROR);
        }
    }

    @Override
    public Result<Void> sendEmailCode(SendEmailCodeRequest request) {
        log.info("发送邮箱验证码，邮箱：{}，类型：{}", request.getEmail(), request.getType());

        try {
            // 检查邮箱格式（已在DTO中校验）
            String email = request.getEmail();
            String type = request.getType();

            // 检查发送频率限制
            String rateLimitKey = EMAIL_CODE_PREFIX + "rate:" + email;
            if (redisUtil.hasKey(rateLimitKey)) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "验证码发送过于频繁，请稍后再试");
            }

            // 如果是注册验证码，检查邮箱是否已被注册
            if ("register".equals(type) && getUserByEmail(email) != null) {
                throw new BusinessException(ResultCode.USER_ALREADY_EXIST.getCode(), "该邮箱已被注册");
            }

            // 生成验证码
            String code = generateEmailCode();

            // 存储验证码到Redis
            String codeKey = EMAIL_CODE_PREFIX + type + ":" + email;
            redisUtil.set(codeKey, code, EMAIL_CODE_EXPIRE);

            // 设置发送频率限制（60秒）
            redisUtil.set(rateLimitKey, "1", 60);

            // 发送邮件
            boolean sendResult = emailService.sendVerificationCode(email, code, type);
            if (!sendResult) {
                throw new BusinessException(ResultCode.ERROR.getCode(), "邮件发送失败，请稍后重试");
            }

            log.info("邮箱验证码发送成功，邮箱：{}", email);
            return Result.success("验证码已发送到您的邮箱，请查收");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送邮箱验证码失败，邮箱：{}", request.getEmail(), e);
            throw new BusinessException(ResultCode.ERROR);
        }
    }

    // ============================== 用户查询 ==============================

    @Override
    public Result<User> getUserById(Long id) {
        log.debug("根据ID查询用户，用户ID：{}", id);

        try {
            if (id == null || id <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }

            User user = userMapper.selectById(id);
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }

            return Result.success(user);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("根据ID查询用户失败，用户ID：{}", id, e);
            throw new BusinessException(ResultCode.ERROR);
        }
    }

    @Override
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return userMapper.selectByEmail(email);
    }

    @Override
    public boolean verifyEmailCode(String email, String code, String type) {
        if (email == null || code == null || type == null) {
            return false;
        }

        String codeKey = EMAIL_CODE_PREFIX + type + ":" + email;
        Object storedCode = redisUtil.get(codeKey);

        return storedCode != null && code.equals(storedCode.toString());
    }

    // ============================== 私有方法 ==============================

    /**
     * 根据用户名或邮箱获取用户信息
     */
    private User getUserByUsernameOrEmail(String usernameOrEmail) {
        // 先尝试按用户名查询
        User user = getUserByUsername(usernameOrEmail);
        if (user == null) {
            // 再尝试按邮箱查询
            user = getUserByEmail(usernameOrEmail);
        }
        return user;
    }

    /**
     * 更新用户登录信息
     */
    private void updateLoginInfo(Long userId) {
        try {
            // 获取当前用户信息
            User currentUser = userMapper.selectById(userId);
            if (currentUser == null) {
                log.warn("用户不存在，无法更新登录信息，用户ID：{}", userId);
                return;
            }

            // 使用专门的更新登录信息方法，只更新必要字段
            int result = userMapper.updateLoginInfo(
                userId,
                LocalDateTime.now(),
                "127.0.0.1", // TODO: 获取真实IP地址
                currentUser.getLoginCount() + 1
            );

            if (result > 0) {
                log.debug("用户登录信息更新成功，用户ID：{}", userId);
            } else {
                log.warn("用户登录信息更新失败，可能用户不存在，用户ID：{}", userId);
            }
        } catch (Exception e) {
            log.warn("更新用户登录信息失败，用户ID：{}", userId, e);
        }
    }

    @Override
    public Result<Void> updateUserAvatar(Long userId, String avatarUrl) {
        log.info("更新用户头像，用户ID：{}，头像URL：{}", userId, avatarUrl);

        try {
            if (userId == null || userId <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
            }

            if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "头像URL不能为空");
            }

            // 检查用户是否存在
            User existingUser = userMapper.selectById(userId);
            if (existingUser == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }

            // 更新头像
            int updateCount = userMapper.updateAvatar(userId, avatarUrl);
            if (updateCount <= 0) {
                throw new BusinessException(ResultCode.ERROR.getCode(), "更新头像失败");
            }

            log.info("用户头像更新成功，用户ID：{}", userId);
            return Result.success("头像更新成功");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户头像失败，用户ID：{}", userId, e);
            throw new BusinessException(ResultCode.ERROR);
        }
    }

    @Override
    public Result<Void> updateUser(Long userId, User user) {
        log.info("更新用户信息，用户ID：{}", userId);

        try {
            if (userId == null || userId <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
            }

            if (user == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户信息不能为空");
            }

            // 检查用户是否存在
            User existingUser = userMapper.selectById(userId);
            if (existingUser == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }

            // 设置更新字段
            user.setId(userId);
            user.setUpdateTime(LocalDateTime.now());
            
            // 不允许通过此接口更新密码，但允许管理员更新其他字段
            user.setPassword(null);

            int updateCount = userMapper.updateUserSelective(user);
            if (updateCount <= 0) {
                throw new BusinessException(ResultCode.ERROR.getCode(), "更新用户信息失败");
            }

            log.info("用户信息更新成功，用户ID：{}", userId);
            return Result.success("用户信息更新成功");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户信息失败，用户ID：{}", userId, e);
            throw new BusinessException(ResultCode.ERROR);
        }
    }

    /**
     * 生成邮箱验证码
     */
    private String generateEmailCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < EMAIL_CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    // ======================= 统计相关方法实现 =======================

    @Override
    public long getTotalUserCount() {
        try {
            return userMapper.countTotalUsers();
        } catch (Exception e) {
            log.error("获取总用户数失败", e);
            return 0;
        }
    }

    @Override
    public long getActiveUserCount(LocalDateTime since) {
        try {
            if (since == null) {
                since = LocalDateTime.now().minusDays(30); // 默认30天内
            }
            return userMapper.countActiveUsers(since);
        } catch (Exception e) {
            log.error("获取活跃用户数失败", e);
            return 0;
        }
    }

    @Override
    public long getOnlineUserCount(LocalDateTime recentTime) {
        try {
            if (recentTime == null) {
                recentTime = LocalDateTime.now().minusMinutes(15); // 默认15分钟内
            }
            return userMapper.countOnlineUsers(recentTime);
        } catch (Exception e) {
            log.error("获取在线用户数失败", e);
            return 0;
        }
    }

    @Override
    public long getOnlineUserCountByType(LocalDateTime recentTime, Integer userType) {
        try {
            if (recentTime == null) {
                recentTime = LocalDateTime.now().minusMinutes(15); // 默认15分钟内
            }
            return userMapper.countOnlineUsersByType(recentTime, userType);
        } catch (Exception e) {
            log.error("获取指定类型在线用户数失败，用户类型：{}", userType, e);
            return 0;
        }
    }

    @Override
    public long getNewUserCount(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (startTime == null || endTime == null) {
                log.warn("获取新增用户数时间参数不能为空");
                return 0;
            }
            return userMapper.countNewUsers(startTime, endTime);
        } catch (Exception e) {
            log.error("获取新增用户数失败", e);
            return 0;
        }
    }

    // ======================= 管理员用户管理方法实现 =======================

    @Override
    public PageResult<User> getUserList(int page, int size, String keyword, Integer userType, Integer status,
                                       LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.info("分页查询用户列表，页码：{}，页面大小：{}，关键词：{}，用户类型：{}，状态：{}",
                    page, size, keyword, userType, status);
            
            // 参数验证
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 20;
            
            // 计算偏移量
            int offset = (page - 1) * size;
            
            // 查询用户列表
            List<User> users = userMapper.selectUserListWithConditions(offset, size, keyword, userType, 
                                                                       status, startTime, endTime);
            
            // 统计总数
            long total = userMapper.countUserListWithConditions(keyword, userType, status, startTime, endTime);
            
            // 清除密码字段
            users.forEach(user -> user.setPassword(null));
            
            log.info("查询用户列表成功，总数：{}，返回数量：{}", total, users.size());
            return new PageResult<User>(page, size, total, users);
            
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "查询用户列表失败");
        }
    }

    @Override
    public Result<Void> updateUserStatus(Long userId, Integer status) {
        try {
            log.info("更新用户状态，用户ID：{}，状态：{}", userId, status);
            
            // 参数验证
            if (userId == null || userId <= 0) {
                return Result.error("用户ID不能为空");
            }
            if (status == null || (status != 0 && status != 1)) {
                return Result.error("状态参数无效");
            }
            
            // 检查用户是否存在
            User existUser = userMapper.selectById(userId);
            if (existUser == null) {
                return Result.error("用户不存在");
            }
            
            // 更新状态
            int rows = userMapper.updateUserStatus(userId, status);
            if (rows > 0) {
                log.info("用户状态更新成功，用户ID：{}，状态：{}", userId, status);
                return Result.success("状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
            
        } catch (Exception e) {
            log.error("更新用户状态失败，用户ID：{}，状态：{}", userId, status, e);
            return Result.error("状态更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteUser(Long userId) {
        try {
            log.info("删除用户，用户ID：{}", userId);
            
            // 参数验证
            if (userId == null || userId <= 0) {
                return Result.error("用户ID不能为空");
            }
            
            // 检查用户是否存在
            User existUser = userMapper.selectById(userId);
            if (existUser == null) {
                return Result.error("用户不存在");
            }
            
            // 检查是否为管理员用户，防止误删
            if (existUser.getUserType() != null && existUser.getUserType() == 2) {
                return Result.error("不能删除管理员用户");
            }
            
            // 逻辑删除
            int rows = userMapper.deleteById(userId);
            if (rows > 0) {
                log.info("用户删除成功，用户ID：{}", userId);
                return Result.success("用户删除成功");
            } else {
                return Result.error("用户删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除用户失败，用户ID：{}", userId, e);
            return Result.error("删除用户失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> batchDeleteUsers(List<Long> userIds) {
        try {
            log.info("批量删除用户，用户ID列表：{}", userIds);
            
            // 参数验证
            if (userIds == null || userIds.isEmpty()) {
                return Result.error("用户ID列表不能为空");
            }
            
            // 检查是否包含管理员用户
            for (Long userId : userIds) {
                User user = userMapper.selectById(userId);
                if (user != null && user.getUserType() != null && user.getUserType() == 2) {
                    return Result.error("列表中包含管理员用户，无法批量删除");
                }
            }
            
            // 批量删除
            int rows = userMapper.batchDeleteByIds(userIds);
            if (rows > 0) {
                log.info("批量删除用户成功，删除数量：{}", rows);
                return Result.success("批量删除成功，删除了 " + rows + " 个用户");
            } else {
                return Result.error("批量删除失败");
            }
            
        } catch (Exception e) {
            log.error("批量删除用户失败，用户ID列表：{}", userIds, e);
            return Result.error("批量删除用户失败：" + e.getMessage());
        }
    }

    @Override
    public long getOnlineUserCountFromRedis() {
        try {
            // 获取所有在线用户的Redis key
            Set<String> onlineKeys = redisUtil.keys("user:online:*");
            if (onlineKeys == null) {
                return 0;
            }
            
            // 过滤掉已过期的key（虽然Redis会自动清理，但为了准确性还是检查一下）
            long count = 0;
            for (String key : onlineKeys) {
                if (redisUtil.hasKey(key)) {
                    count++;
                }
            }
            
            log.debug("基于Redis统计在线用户数：{}", count);
            return count;
        } catch (Exception e) {
            log.error("基于Redis统计在线用户数失败", e);
            return 0;
        }
    }

    @Override
    public long getOnlineUserCountFromRedisByType(Integer userType) {
        try {
            // 获取所有在线用户的Redis key
            Set<String> onlineKeys = redisUtil.keys("user:online:*");
            if (onlineKeys == null) {
                return 0;
            }
            
            long count = 0;
            for (String key : onlineKeys) {
                if (redisUtil.hasKey(key)) {
                    Object storedUserType = redisUtil.get(key);
                    if (storedUserType != null && userType.equals(Integer.valueOf(storedUserType.toString()))) {
                        count++;
                    }
                }
            }
            
            log.debug("基于Redis统计在线用户数（类型{}）：{}", userType, count);
            return count;
        } catch (Exception e) {
            log.error("基于Redis统计指定类型在线用户数失败，类型：{}", userType, e);
            return 0;
        }
    }
} 