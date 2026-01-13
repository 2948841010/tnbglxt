package org.example.tlbglxt.mapper;

import org.apache.ibatis.annotations.*;
import org.example.tlbglxt.entity.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据访问层接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE id = #{id} AND is_deleted = 0")
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND is_deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM sys_user WHERE email = #{email} AND is_deleted = 0")
    User selectByEmail(@Param("email") String email);

    /**
     * 插入用户
     *
     * @param user 用户信息
     * @return 影响行数
     */
    @Insert("INSERT INTO sys_user (username, password, real_name, email, phone, gender, " +
            "user_type, status, login_count, create_time, update_time, is_deleted) " +
            "VALUES (#{username}, #{password}, #{realName}, #{email}, #{phone}, #{gender}, " +
            "#{userType}, #{status}, #{loginCount}, #{createTime}, #{updateTime}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);

    /**
     * 根据ID更新用户信息
     *
     * @param user 用户信息
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET " +
            "real_name = #{realName}, email = #{email}, phone = #{phone}, " +
            "gender = #{gender}, status = #{status}, last_login_time = #{lastLoginTime}, " +
            "last_login_ip = #{lastLoginIp}, login_count = #{loginCount}, " +
            "update_time = #{updateTime}, update_by = #{updateBy} " +
            "WHERE id = #{id} AND is_deleted = 0")
    int updateById(User user);

    /**
     * 更新用户登录信息
     *
     * @param id 用户ID
     * @param lastLoginTime 最后登录时间
     * @param lastLoginIp 最后登录IP
     * @param loginCount 登录次数
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp}, " +
            "login_count = #{loginCount}, update_time = NOW() " +
            "WHERE id = #{id} AND is_deleted = 0")
    int updateLoginInfo(@Param("id") Long id, 
                       @Param("lastLoginTime") java.time.LocalDateTime lastLoginTime,
                       @Param("lastLoginIp") String lastLoginIp, 
                       @Param("loginCount") Integer loginCount);

    /**
     * 更新用户头像
     *
     * @param id 用户ID
     * @param avatar 头像URL
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET avatar = #{avatar}, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    /**
     * 逻辑删除用户
     *
     * @param id 用户ID
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET is_deleted = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 查询所有医生用户
     *
     * @return 医生用户列表
     */
    @Select("SELECT * FROM sys_user WHERE user_type = 1 AND status = 1 AND is_deleted = 0 ORDER BY create_time DESC")
    List<User> selectDoctorUsers();

    // ======================= 统计相关方法 =======================

    /**
     * 统计总用户数（不包括已删除的）
     *
     * @return 总用户数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0")
    long countTotalUsers();

    /**
     * 统计活跃用户数（指定时间之后有登录记录的用户）
     *
     * @param since 起始时间
     * @return 活跃用户数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0 AND last_login_time >= #{since}")
    long countActiveUsers(@Param("since") LocalDateTime since);

    /**
     * 统计在线用户数（最近指定时间内有登录记录的用户）
     *
     * @param recentTime 最近时间
     * @return 在线用户数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0 AND last_login_time >= #{recentTime}")
    long countOnlineUsers(@Param("recentTime") LocalDateTime recentTime);
    
    /**
     * 统计指定用户类型的在线用户数
     *
     * @param recentTime 最近活动时间阈值
     * @param userType 用户类型
     * @return 在线用户数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0 AND last_login_time >= #{recentTime} AND user_type = #{userType}")
    long countOnlineUsersByType(@Param("recentTime") LocalDateTime recentTime, @Param("userType") Integer userType);

    /**
     * 统计指定时间范围内新增用户数
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增用户数
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0 AND create_time BETWEEN #{startTime} AND #{endTime}")
    long countNewUsers(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // ======================= 管理员用户管理方法 =======================

    /**
     * 分页查询用户列表（带搜索和筛选）
     *
     * @param offset 偏移量
     * @param limit 限制数量
     * @param keyword 关键词搜索
     * @param userType 用户类型筛选
     * @param status 用户状态筛选
     * @param startTime 注册开始时间
     * @param endTime 注册结束时间
     * @return 用户列表
     */
    List<User> selectUserListWithConditions(@Param("offset") int offset, 
                                           @Param("limit") int limit,
                                           @Param("keyword") String keyword, 
                                           @Param("userType") Integer userType,
                                           @Param("status") Integer status, 
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户总数（带搜索和筛选）
     *
     * @param keyword 关键词搜索
     * @param userType 用户类型筛选
     * @param status 用户状态筛选
     * @param startTime 注册开始时间
     * @param endTime 注册结束时间
     * @return 用户总数
     */
    long countUserListWithConditions(@Param("keyword") String keyword, 
                                    @Param("userType") Integer userType,
                                    @Param("status") Integer status, 
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE sys_user SET status = #{status}, update_time = NOW() WHERE id = #{id} AND is_deleted = 0")
    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 批量逻辑删除用户
     *
     * @param ids 用户ID列表
     * @return 影响行数
     */
    int batchDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 动态更新用户信息（只更新非空字段）
     *
     * @param user 用户信息
     * @return 影响行数
     */
    int updateUserSelective(User user);
} 