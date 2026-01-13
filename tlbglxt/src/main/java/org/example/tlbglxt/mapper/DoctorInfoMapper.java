package org.example.tlbglxt.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.tlbglxt.entity.DoctorInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 医生信息数据访问接口
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Mapper
public interface DoctorInfoMapper {

    /**
     * 根据用户ID查询医生信息
     *
     * @param userId 用户ID
     * @return 医生信息
     */
    DoctorInfo selectByUserId(@Param("userId") Long userId);

    /**
     * 根据医生编号查询医生信息
     *
     * @param doctorNo 医生编号
     * @return 医生信息
     */
    DoctorInfo selectByDoctorNo(@Param("doctorNo") String doctorNo);

    /**
     * 根据ID查询医生信息
     *
     * @param id 医生信息ID
     * @return 医生信息
     */
    DoctorInfo selectById(@Param("id") Long id);

    /**
     * 插入医生信息
     *
     * @param doctorInfo 医生信息
     * @return 影响行数
     */
    int insert(DoctorInfo doctorInfo);

    /**
     * 根据ID更新医生信息
     *
     * @param doctorInfo 医生信息
     * @return 影响行数
     */
    int updateById(DoctorInfo doctorInfo);

    /**
     * 根据用户ID更新医生信息
     *
     * @param doctorInfo 医生信息
     * @return 影响行数
     */
    int updateByUserId(DoctorInfo doctorInfo);

    /**
     * 根据ID删除医生信息（逻辑删除）
     *
     * @param id 医生信息ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户ID删除医生信息（逻辑删除）
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 查询所有活跃的医生信息
     *
     * @return 医生信息列表
     */
    List<DoctorInfo> selectAllActiveDoctors();

    // ======================= 统计相关方法 =======================

    /**
     * 统计总医生数（不包括已删除的）
     *
     * @return 总医生数
     */
    @Select("SELECT COUNT(*) FROM doctor_info WHERE is_deleted = 0")
    long countTotalDoctors();

    /**
     * 统计活跃医生数（指定时间之后有更新的医生）
     * 这里通过关联用户表获取最后登录时间
     *
     * @param since 起始时间
     * @return 活跃医生数
     */
    @Select("SELECT COUNT(*) FROM doctor_info di " +
            "INNER JOIN sys_user su ON di.user_id = su.id " +
            "WHERE di.is_deleted = 0 AND su.is_deleted = 0 " +
            "AND su.last_login_time >= #{since}")
    long countActiveDoctors(@Param("since") LocalDateTime since);

    /**
     * 统计在线医生数（在线状态为1的医生）
     *
     * @return 在线医生数
     */
    @Select("SELECT COUNT(*) FROM doctor_info WHERE is_deleted = 0 AND online_status = 1")
    long countOnlineDoctors();

    // ======================= 管理员相关查询方法 =======================

    /**
     * 分页查询医生列表（管理员用）
     */
    @Select("<script>" +
            "SELECT " +
            "di.id, di.doctor_no, di.department, di.title, di.work_years as experience, " +
            "di.introduction, di.speciality, COALESCE(di.status, 1) as available, di.online_status, " +
            "di.consultation_count, di.rating, di.create_time, di.update_time, " +
            "su.id as user_id, su.username, su.real_name, su.email, su.phone, " +
            "su.gender, su.birthday as birth_date, su.avatar, su.status, su.last_login_time, " +
            "su.login_count " +
            "FROM doctor_info di " +
            "INNER JOIN sys_user su ON di.user_id = su.id " +
            "WHERE di.is_deleted = 0 AND su.is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (su.real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR su.username LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='department != null and department != \"\"'>" +
            "AND di.department = #{department} " +
            "</if>" +
            "<if test='title != null and title != \"\"'>" +
            "AND di.title = #{title} " +
            "</if>" +
            "<if test='onlineStatus != null'>" +
            "AND di.online_status = #{onlineStatus} " +
            "</if>" +
            "<if test='available != null'>" +
            "AND COALESCE(di.status, 1) = #{available} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND su.status = #{status} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND su.create_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND su.create_time &lt;= #{endTime} " +
            "</if>" +
            "ORDER BY di.create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<Map<String, Object>> selectDoctorListForAdmin(@Param("offset") int offset, 
                                                       @Param("size") int size,
                                                       @Param("keyword") String keyword,
                                                       @Param("department") String department,
                                                       @Param("title") String title,
                                                       @Param("onlineStatus") Integer onlineStatus,
                                                       @Param("available") Integer available,
                                                       @Param("status") Integer status,
                                                       @Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 统计医生列表总数（管理员用）
     */
    @Select("<script>" +
            "SELECT COUNT(*) " +
            "FROM doctor_info di " +
            "INNER JOIN sys_user su ON di.user_id = su.id " +
            "WHERE di.is_deleted = 0 AND su.is_deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (su.real_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR su.username LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='department != null and department != \"\"'>" +
            "AND di.department = #{department} " +
            "</if>" +
            "<if test='title != null and title != \"\"'>" +
            "AND di.title = #{title} " +
            "</if>" +
            "<if test='onlineStatus != null'>" +
            "AND di.online_status = #{onlineStatus} " +
            "</if>" +
            "<if test='available != null'>" +
            "AND COALESCE(di.status, 1) = #{available} " +
            "</if>" +
            "<if test='status != null'>" +
            "AND su.status = #{status} " +
            "</if>" +
            "<if test='startTime != null'>" +
            "AND su.create_time >= #{startTime} " +
            "</if>" +
            "<if test='endTime != null'>" +
            "AND su.create_time &lt;= #{endTime} " +
            "</if>" +
            "</script>")
    long countDoctorListForAdmin(@Param("keyword") String keyword,
                                @Param("department") String department,
                                @Param("title") String title,
                                @Param("onlineStatus") Integer onlineStatus,
                                @Param("available") Integer available,
                                @Param("status") Integer status,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);

    /**
     * 统计可接诊医生数
     */
    @Select("SELECT COUNT(*) FROM doctor_info di " +
            "INNER JOIN sys_user su ON di.user_id = su.id " +
            "WHERE di.is_deleted = 0 AND su.is_deleted = 0 " +
            "AND COALESCE(di.status, 1) = 1 AND su.status = 1")
    long countAvailableDoctors();

    /**
     * 查询医生详情（管理员用）
     */
    @Select("SELECT " +
            "di.id, di.doctor_no, di.department, di.title, di.work_years as experience, " +
            "di.introduction, di.speciality, COALESCE(di.status, 1) as available, di.online_status, " +
            "di.consultation_count, di.rating, di.create_time, di.update_time, " +
            "su.id as user_id, su.username, su.real_name, su.email, su.phone, " +
            "su.gender, su.birthday as birth_date, su.avatar, su.status, su.last_login_time, " +
            "su.login_count, su.create_time as user_create_time " +
            "FROM doctor_info di " +
            "INNER JOIN sys_user su ON di.user_id = su.id " +
            "WHERE di.id = #{doctorId} AND di.is_deleted = 0 AND su.is_deleted = 0")
    Map<String, Object> selectDoctorDetailForAdmin(@Param("doctorId") Long doctorId);
} 