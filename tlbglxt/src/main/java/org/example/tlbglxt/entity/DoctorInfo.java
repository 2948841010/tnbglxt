package org.example.tlbglxt.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生信息实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class DoctorInfo {

    /**
     * 医生信息ID
     */
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 医生编号
     */
    private String doctorNo;

    /**
     * 所属科室
     */
    private String department;

    /**
     * 职称
     */
    private String title;

    /**
     * 执业资格证书号
     */
    private String qualificationCert;

    /**
     * 专业特长
     */
    private String speciality;

    /**
     * 从业年限
     */
    private Integer workYears;

    /**
     * 从业经验年限（别名，用于管理端）
     */
    private Integer experience;

    /**
     * 所属医院
     */
    private String hospital;

    /**
     * 是否可接诊（0-不可接诊，1-可接诊）
     */
    private Integer available;

    /**
     * 个人简介
     */
    private String introduction;

    /**
     * 咨询费用
     */
    private BigDecimal consultationFee;

    /**
     * 在线状态（0-离线，1-在线，2-忙碌）
     */
    private Integer onlineStatus;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 咨询次数
     */
    private Integer consultationCount;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 更新人
     */
    private Long updateBy;

    /**
     * 是否删除（0-否，1-是）
     */
    private Integer isDeleted;

    // 关联的用户信息（用于返回完整的医生信息）
    private User userInfo;
} 