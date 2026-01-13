package org.example.tlbglxt.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.PageResult;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.common.ResultCode;
import org.example.tlbglxt.dto.request.chat.CreateConsultationRequest;
import org.example.tlbglxt.dto.request.chat.SendMessageRequest;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.entity.chat.ConsultationChat;
import org.example.tlbglxt.entity.chat.ConsultationMessage;
import org.example.tlbglxt.repository.mongo.ConsultationChatRepository;
import org.example.tlbglxt.service.ConsultationChatService;
import org.example.tlbglxt.service.DoctorService;
import org.example.tlbglxt.service.UserService;
import org.example.tlbglxt.service.WebSocketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 问诊聊天服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationChatServiceImpl implements ConsultationChatService {

    private final ConsultationChatRepository consultationChatRepository;
    private final UserService userService;
    private final DoctorService doctorService;
    private final WebSocketService webSocketService;

    @Override
    public Result<ConsultationChat> createConsultation(Long patientId, CreateConsultationRequest request) {
        try {
            // 验证患者信息
            Result<User> patientResult = userService.getUserById(patientId);
            if (!patientResult.isSuccess()) {
                return Result.error(ResultCode.USER_NOT_EXIST);
            }
            User patient = patientResult.getData();

            // 验证医生信息
            Result<User> doctorResult = userService.getUserById(request.getDoctorId());
            if (!doctorResult.isSuccess()) {
                return Result.error(ResultCode.USER_NOT_EXIST);
            }
            User doctor = doctorResult.getData();

            // 获取医生详细信息
            var doctorInfoResult = doctorService.getDoctorInfoByUserId(request.getDoctorId());
            if (!doctorInfoResult.isSuccess()) {
                return Result.error(ResultCode.DATA_NOT_EXIST);
            }
            var doctorInfo = doctorInfoResult.getData();

            // 生成问诊编号
            String consultationNo = generateConsultationNo();

            // 创建问诊记录
            ConsultationChat consultation = new ConsultationChat();
            consultation.setConsultationNo(consultationNo);
            consultation.setPatientId(patientId);
            consultation.setDoctorId(request.getDoctorId());
            consultation.setConsultationType(1); // 固定为对话问诊
            consultation.setStatus(1); // 待接诊
            consultation.setChiefComplaint(request.getChiefComplaint());
            consultation.setFee(request.getFee());

            // 设置患者信息
            ConsultationChat.PatientInfo patientInfo = new ConsultationChat.PatientInfo();
            patientInfo.setName(patient.getRealName());
            patientInfo.setPhone(patient.getPhone());
            patientInfo.setAvatar(patient.getAvatar()); // 设置患者头像
            // 这里可以从用户扩展信息中获取年龄和性别，暂时设置默认值
            patientInfo.setAge(0);
            patientInfo.setGender("未知");
            consultation.setPatientInfo(patientInfo);

            // 设置医生信息
            ConsultationChat.DoctorInfo doctorInfoData = new ConsultationChat.DoctorInfo();
            doctorInfoData.setName(doctor.getRealName());
            doctorInfoData.setDepartment(doctorInfo.getDepartment());
            doctorInfoData.setTitle(doctorInfo.getTitle());
            doctorInfoData.setAvatar(doctor.getAvatar()); // 设置医生头像
            consultation.setDoctorInfo(doctorInfoData);

            // 创建患者的初始症状描述消息
            List<ConsultationMessage> messages = new ArrayList<>();
            ConsultationMessage initialMessage = new ConsultationMessage();
            initialMessage.setMessageId(UUID.randomUUID().toString());
            initialMessage.setSenderId(patientId);
            initialMessage.setSenderType("patient");
            initialMessage.setMessageType("text");
            initialMessage.setContent("症状描述：" + request.getChiefComplaint());
            initialMessage.setSendTime(LocalDateTime.now());
            initialMessage.setIsRead(false);
            messages.add(initialMessage);
            
            consultation.setMessages(messages);

            // 设置时间
            LocalDateTime now = LocalDateTime.now();
            consultation.setCreateTime(now);
            consultation.setUpdateTime(now);

            // 保存到MongoDB
            ConsultationChat savedConsultation = consultationChatRepository.save(consultation);

            // 通知医生有新的咨询
            webSocketService.notifyNewConsultation(savedConsultation);

            log.info("创建问诊成功，问诊编号：{}", consultationNo);
            return Result.success(savedConsultation);

        } catch (Exception e) {
            log.error("创建问诊失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<ConsultationMessage> sendMessage(Long senderId, String senderType, SendMessageRequest request) {
        try {
            // 查找问诊记录
            Optional<ConsultationChat> consultationOpt = consultationChatRepository.findByConsultationNo(request.getConsultationNo());
            if (consultationOpt.isEmpty()) {
                return Result.error(ResultCode.DATA_NOT_EXIST, "问诊记录不存在");
            }

            ConsultationChat consultation = consultationOpt.get();

            // 验证发送者权限
            if (!"patient".equals(senderType) && !"doctor".equals(senderType)) {
                return Result.error(ResultCode.FORBIDDEN, "无效的发送者类型");
            }

            if ("patient".equals(senderType) && !senderId.equals(consultation.getPatientId())) {
                return Result.error(ResultCode.FORBIDDEN, "无权限发送消息");
            }

            if ("doctor".equals(senderType) && !senderId.equals(consultation.getDoctorId())) {
                return Result.error(ResultCode.FORBIDDEN, "无权限发送消息");
            }

            // 创建消息
            ConsultationMessage message = new ConsultationMessage();
            message.setMessageId(UUID.randomUUID().toString());
            message.setSenderId(senderId);
            message.setSenderType(senderType);
            message.setMessageType(request.getMessageType());
            message.setContent(request.getContent());
            message.setMediaUrl(request.getMediaUrl());
            message.setMediaSize(request.getMediaSize());
            message.setFileName(request.getFileName());
            message.setFileSize(request.getFileSize());
            message.setContentType(request.getContentType());
            message.setSendTime(LocalDateTime.now());
            message.setIsRead(false);

            // 添加消息到问诊记录
            if (consultation.getMessages() == null) {
                consultation.setMessages(new ArrayList<>());
            }
            consultation.getMessages().add(message);
            consultation.setUpdateTime(LocalDateTime.now());

            // 更新问诊状态为进行中
            if (consultation.getStatus() == 1) {
                consultation.setStatus(2); // 进行中
                if (consultation.getStartTime() == null) {
                    consultation.setStartTime(LocalDateTime.now());
                }
            }

            // 保存更新
            ConsultationChat savedConsultation = consultationChatRepository.save(consultation);

            // 通过WebSocket发送实时消息通知
            webSocketService.sendMessageToConsultation(request.getConsultationNo(), message);

            // 如果是医生发送消息，通知医生回复列表更新
            if ("doctor".equals(senderType)) {
                webSocketService.notifyDoctorConsultationListUpdate(senderId, "new_reply", savedConsultation);
            }

            log.info("发送消息成功，问诊编号：{}，消息ID：{}", request.getConsultationNo(), message.getMessageId());
            return Result.success(message);

        } catch (Exception e) {
            log.error("发送消息失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<ConsultationChat> getConsultationDetail(String consultationNo) {
        try {
            Optional<ConsultationChat> consultationOpt = consultationChatRepository.findByConsultationNo(consultationNo);
            if (consultationOpt.isEmpty()) {
                return Result.error(ResultCode.DATA_NOT_EXIST, "问诊记录不存在");
            }
            return Result.success(consultationOpt.get());
        } catch (Exception e) {
            log.error("获取问诊详情失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<List<ConsultationChat>> getPatientConsultations(Long patientId) {
        try {
            List<ConsultationChat> consultations = consultationChatRepository.findByPatientIdOrderByCreateTimeDesc(patientId);
            return Result.success(consultations);
        } catch (Exception e) {
            log.error("获取患者问诊列表失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<List<ConsultationChat>> getDoctorConsultations(Long doctorId) {
        try {
            List<ConsultationChat> consultations = consultationChatRepository.findByDoctorIdOrderByCreateTimeDesc(doctorId);
            return Result.success(consultations);
        } catch (Exception e) {
            log.error("获取医生问诊列表失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<PageResult<ConsultationChat>> getPatientConsultationsPage(Long patientId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<ConsultationChat> pageData = consultationChatRepository.findByPatientId(patientId, pageable);

            PageResult<ConsultationChat> result = PageResult.of(
                page, 
                size, 
                pageData.getTotalElements(), 
                pageData.getContent()
            );

            return Result.success(result);
        } catch (Exception e) {
            log.error("分页获取患者问诊列表失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<PageResult<ConsultationChat>> getDoctorConsultationsPage(Long doctorId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<ConsultationChat> pageData = consultationChatRepository.findByDoctorId(doctorId, pageable);

            PageResult<ConsultationChat> result = PageResult.of(
                page, 
                size, 
                pageData.getTotalElements(), 
                pageData.getContent()
            );

            return Result.success(result);
        } catch (Exception e) {
            log.error("分页获取医生问诊列表失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<PageResult<ConsultationChat>> getDoctorConsultationsPageByStatus(Long doctorId, int page, int size, String statusFilter) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<ConsultationChat> pageData;

            if (statusFilter == null || statusFilter.trim().isEmpty()) {
                // 如果没有状态过滤，返回所有
                pageData = consultationChatRepository.findByDoctorId(doctorId, pageable);
            } else {
                // 解析状态过滤参数 (例如 "2,3")
                String[] statusArray = statusFilter.split(",");
                List<Integer> statusList = new ArrayList<>();
                for (String status : statusArray) {
                    try {
                        statusList.add(Integer.parseInt(status.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("无效的状态参数: {}", status);
                    }
                }
                
                if (statusList.isEmpty()) {
                    // 如果解析失败，返回所有
                    pageData = consultationChatRepository.findByDoctorId(doctorId, pageable);
                } else {
                    // 查询指定状态的咨询
                    pageData = consultationChatRepository.findByDoctorIdAndStatusIn(doctorId, statusList, pageable);
                }
            }

            PageResult<ConsultationChat> result = PageResult.of(
                page, 
                size, 
                pageData.getTotalElements(), 
                pageData.getContent()
            );

            return Result.success(result);
        } catch (Exception e) {
            log.error("分页获取医生指定状态问诊列表失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<Void> acceptConsultation(Long doctorId, String consultationNo) {
        try {
            Optional<ConsultationChat> consultationOpt = consultationChatRepository.findByConsultationNo(consultationNo);
            if (consultationOpt.isEmpty()) {
                return Result.error(ResultCode.DATA_NOT_EXIST, "问诊记录不存在");
            }

            ConsultationChat consultation = consultationOpt.get();

            // 验证医生权限
            if (!doctorId.equals(consultation.getDoctorId())) {
                return Result.error(ResultCode.FORBIDDEN, "无权限接诊");
            }

            // 只有待接诊状态才能接诊
            if (consultation.getStatus() != 1) {
                return Result.error(ResultCode.ERROR, "当前状态不允许接诊");
            }

            // 记录旧状态
            Integer oldStatus = consultation.getStatus();

            // 更新状态为进行中
            consultation.setStatus(2);
            consultation.setStartTime(LocalDateTime.now());
            consultation.setUpdateTime(LocalDateTime.now());

            ConsultationChat savedConsultation = consultationChatRepository.save(consultation);

            // 通知咨询状态变更
            webSocketService.notifyConsultationStatusChange(consultationNo, oldStatus, 2, savedConsultation);
            // 通知医生咨询列表更新
            webSocketService.notifyDoctorConsultationListUpdate(doctorId, "consultation_status_changed", savedConsultation);

            log.info("医生接诊成功，问诊编号：{}", consultationNo);
            return Result.success();

        } catch (Exception e) {
            log.error("医生接诊失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<Void> completeConsultation(String consultationNo) {
        try {
            Optional<ConsultationChat> consultationOpt = consultationChatRepository.findByConsultationNo(consultationNo);
            if (consultationOpt.isEmpty()) {
                return Result.error(ResultCode.DATA_NOT_EXIST, "问诊记录不存在");
            }

            ConsultationChat consultation = consultationOpt.get();

            // 只有进行中的问诊才能完成
            if (consultation.getStatus() != 2) {
                return Result.error(ResultCode.ERROR, "当前状态不允许完成");
            }

            // 记录旧状态
            Integer oldStatus = consultation.getStatus();

            // 更新状态为已完成
            consultation.setStatus(3);
            consultation.setEndTime(LocalDateTime.now());
            consultation.setUpdateTime(LocalDateTime.now());

            ConsultationChat savedConsultation = consultationChatRepository.save(consultation);

            // 通知咨询状态变更
            webSocketService.notifyConsultationStatusChange(consultationNo, oldStatus, 3, savedConsultation);
            // 通知医生咨询列表更新
            webSocketService.notifyDoctorConsultationListUpdate(consultation.getDoctorId(), "consultation_completed", savedConsultation);

            log.info("完成问诊成功，问诊编号：{}", consultationNo);
            return Result.success();

        } catch (Exception e) {
            log.error("完成问诊失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<Void> cancelConsultation(String consultationNo, Long userId) {
        try {
            Optional<ConsultationChat> consultationOpt = consultationChatRepository.findByConsultationNo(consultationNo);
            if (consultationOpt.isEmpty()) {
                return Result.error(ResultCode.DATA_NOT_EXIST, "问诊记录不存在");
            }

            ConsultationChat consultation = consultationOpt.get();

            // 验证权限（患者或医生都可以取消）
            if (!userId.equals(consultation.getPatientId()) && !userId.equals(consultation.getDoctorId())) {
                return Result.error(ResultCode.FORBIDDEN, "无权限取消问诊");
            }

            // 只有待接诊和进行中的问诊才能取消
            if (consultation.getStatus() != 1 && consultation.getStatus() != 2) {
                return Result.error(ResultCode.ERROR, "当前状态不允许取消");
            }

            // 记录旧状态
            Integer oldStatus = consultation.getStatus();

            // 更新状态为已取消
            consultation.setStatus(4);
            consultation.setUpdateTime(LocalDateTime.now());

            ConsultationChat savedConsultation = consultationChatRepository.save(consultation);

            // 通知咨询状态变更
            webSocketService.notifyConsultationStatusChange(consultationNo, oldStatus, 4, savedConsultation);
            // 通知医生咨询列表更新
            webSocketService.notifyDoctorConsultationListUpdate(consultation.getDoctorId(), "consultation_status_changed", savedConsultation);

            log.info("取消问诊成功，问诊编号：{}", consultationNo);
            return Result.success();

        } catch (Exception e) {
            log.error("取消问诊失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<Void> markMessageAsRead(String consultationNo, String messageId, Long userId) {
        try {
            Optional<ConsultationChat> consultationOpt = consultationChatRepository.findByConsultationNo(consultationNo);
            if (consultationOpt.isEmpty()) {
                return Result.error(ResultCode.DATA_NOT_EXIST, "问诊记录不存在");
            }

            ConsultationChat consultation = consultationOpt.get();

            // 验证权限
            if (!userId.equals(consultation.getPatientId()) && !userId.equals(consultation.getDoctorId())) {
                return Result.error(ResultCode.FORBIDDEN, "无权限操作");
            }

            // 查找并更新消息状态
            if (consultation.getMessages() != null) {
                for (ConsultationMessage message : consultation.getMessages()) {
                    if (message.getMessageId().equals(messageId) && !message.getSenderId().equals(userId)) {
                        message.setIsRead(true);
                        break;
                    }
                }
            }

            consultation.setUpdateTime(LocalDateTime.now());
            consultationChatRepository.save(consultation);

            return Result.success();

        } catch (Exception e) {
            log.error("标记消息已读失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<List<ConsultationChat>> getActiveConsultations(Long userId) {
        try {
            List<ConsultationChat> consultations = consultationChatRepository.findActiveConsultationsByUserId(userId);
            return Result.success(consultations);
        } catch (Exception e) {
            log.error("获取活跃问诊失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<Long> getTodayConsultationCount(Long doctorId) {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            long count = consultationChatRepository.countTodayConsultationsByDoctorId(doctorId, startOfDay, endOfDay);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取今日问诊数量失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<Long> getPendingConsultationsCount(Long doctorId) {
        try {
            long count = consultationChatRepository.countPendingConsultationsByDoctorId(doctorId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取待回复咨询数量失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<Long> getWaitingConsultationsCount(Long doctorId) {
        try {
            long count = consultationChatRepository.countConsultationsByDoctorIdAndStatus(doctorId, 1);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取待接诊咨询数量失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<Long> getOngoingConsultationsCount(Long doctorId) {
        try {
            long count = consultationChatRepository.countConsultationsByDoctorIdAndStatus(doctorId, 2);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取正在接诊咨询数量失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<Long> getCompletedConsultationsCount(Long doctorId) {
        try {
            long count = consultationChatRepository.countConsultationsByDoctorIdAndStatus(doctorId, 3);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取已完成咨询数量失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Result<List<ConsultationChat>> getRecentConsultations(Long doctorId, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updateTime"));
            List<ConsultationChat> consultations = consultationChatRepository.findRecentByDoctorId(doctorId, pageable);
            return Result.success(consultations);
        } catch (Exception e) {
            log.error("获取最近咨询列表失败", e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 生成问诊编号
     */
    private String generateConsultationNo() {
        String prefix = "CONS_";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + timestamp + suffix;
    }

    // ======================= 管理员统计相关方法实现 =======================

    @Override
    public long getTotalConsultationCount() {
        try {
            return consultationChatRepository.count();
        } catch (Exception e) {
            log.error("获取总咨询数量失败", e);
            return 0;
        }
    }

    @Override
    public long getConsultationCountByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (startTime == null || endTime == null) {
                log.warn("获取指定时间范围咨询数量时间参数不能为空");
                return 0;
            }
            return consultationChatRepository.countByCreateTimeBetween(startTime, endTime);
        } catch (Exception e) {
            log.error("获取指定时间范围咨询数量失败", e);
            return 0;
        }
    }

    @Override
    public long getCompletedConsultationCount() {
        try {
            return consultationChatRepository.countByStatus(3); // 状态3表示已完成
        } catch (Exception e) {
            log.error("获取已完成咨询总数失败", e);
            return 0;
        }
    }

    @Override
    public long getCompletedConsultationCountByDate(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (startTime == null || endTime == null) {
                log.warn("获取指定时间范围已完成咨询数量时间参数不能为空");
                return 0;
            }
            return consultationChatRepository.countByStatusAndEndTimeBetween(3, startTime, endTime);
        } catch (Exception e) {
            log.error("获取指定时间范围已完成咨询数量失败", e);
            return 0;
        }
    }

    @Override
    public long getOngoingConsultationCount() {
        try {
            return consultationChatRepository.countByStatus(2); // 状态2表示进行中
        } catch (Exception e) {
            log.error("获取正在进行咨询数量失败", e);
            return 0;
        }
    }
} 