package org.example.tlbglxt.entity.health;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 健康记录基础实体类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
public abstract class BaseHealthRecord {
    
    @Id
    private String id;
    
    /**
     * 用户ID (关联MySQL中的sys_user.id)
     */
    @Field("userId")
    private Long userId;
    
    /**
     * 创建时间
     */
    @Field("createTime")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @Field("updateTime")
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    @Field("remark")
    private String remark;
} 