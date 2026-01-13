package org.example.tlbglxt.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public class User {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    @JsonIgnore
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 性别（0-女，1-男）
     */
    @JsonIgnore
    private Integer gender;
    
    /**
     * 设置性别（支持字符串格式）
     */
    @JsonSetter("gender")
    public void setGenderFromString(Object genderValue) {
        if (genderValue == null) {
            this.gender = null;
        } else if (genderValue instanceof Integer) {
            this.gender = (Integer) genderValue;
        } else if (genderValue instanceof String) {
            String genderStr = (String) genderValue;
            switch (genderStr) {
                case "男":
                    this.gender = 1;
                    break;
                case "女":
                    this.gender = 0;
                    break;
                case "未知":
                default:
                    this.gender = null;
                    break;
            }
        } else {
            this.gender = null;
        }
    }
    
    /**
     * 获取性别字符串表示
     */
    @JsonGetter("gender")
    public String getGenderString() {
        if (gender == null) {
            return "未知";
        }
        return gender == 1 ? "男" : "女";
    }

    /**
     * 出生日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime birthday;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户类型（0-普通用户，1-医生，2-管理员）
     */
    private Integer userType;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 登录次数
     */
    private Integer loginCount;

    /**
     * 备注
     */
    private String remark;

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
    @JsonIgnore
    private Integer isDeleted;
} 