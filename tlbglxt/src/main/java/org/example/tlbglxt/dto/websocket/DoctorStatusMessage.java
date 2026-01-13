package org.example.tlbglxt.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医生状态变化WebSocket消息
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatusMessage {

    /**
     * 消息类型
     */
    private String type = "DOCTOR_STATUS_CHANGE";

    /**
     * 医生用户ID
     */
    private Long doctorId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 科室
     */
    private String department;

    /**
     * 在线状态（0-离线，1-在线，2-忙碌）
     */
    private Integer onlineStatus;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 构造方法
     */
    public DoctorStatusMessage(Long doctorId, String doctorName, String department, Integer onlineStatus) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.department = department;
        this.onlineStatus = onlineStatus;
        this.timestamp = System.currentTimeMillis();
        
        // 根据状态码设置状态文本
        switch (onlineStatus) {
            case 0:
                this.statusText = "离线";
                break;
            case 1:
                this.statusText = "在线";
                break;
            case 2:
                this.statusText = "忙碌";
                break;
            default:
                this.statusText = "未知";
        }
    }
} 