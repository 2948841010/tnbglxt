package org.example.tlbglxt.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.PageResult;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.common.ResultCode;
import org.example.tlbglxt.entity.DoctorInfo;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.exception.BusinessException;
import org.example.tlbglxt.mapper.DoctorInfoMapper;
import org.example.tlbglxt.repository.mongo.ConsultationChatRepository;
import org.example.tlbglxt.service.DoctorService;
import org.example.tlbglxt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医生服务实现类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class DoctorServiceImpl implements DoctorService {

    @Autowired
    private DoctorInfoMapper doctorInfoMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ConsultationChatRepository consultationChatRepository;

    @Override
    public Result<DoctorInfo> getDoctorInfoByUserId(Long userId) {
        log.info("获取医生信息，用户ID：{}", userId);

        try {
            if (userId == null || userId <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
            }

            // 1. 获取用户基础信息
            Result<User> userResult = userService.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }

            User user = userResult.getData();

            // 2. 验证用户是否为医生
            if (user.getUserType() == null || !user.getUserType().equals(1)) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该用户不是医生");
            }

            // 3. 获取医生专业信息
            DoctorInfo doctorInfo = doctorInfoMapper.selectByUserId(userId);
            if (doctorInfo == null) {
                // 如果医生信息不存在，创建默认的医生信息
                log.info("医生信息不存在，创建默认信息，用户ID：{}", userId);
                doctorInfo = createDefaultDoctorInfo(user);
            }

            // 4. 设置关联的用户信息
            doctorInfo.setUserInfo(user);

            log.info("获取医生信息成功，用户ID：{}", userId);
            return Result.success("获取成功", doctorInfo);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取医生信息失败，用户ID：{}", userId, e);
            throw new BusinessException(ResultCode.ERROR);
        }
    }

    @Override
    public Result<DoctorInfo> getDoctorInfoByDoctorNo(String doctorNo) {
        log.info("根据医生编号获取医生信息，医生编号：{}", doctorNo);

        try {
            if (doctorNo == null || doctorNo.trim().isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "医生编号不能为空");
            }

            DoctorInfo doctorInfo = doctorInfoMapper.selectByDoctorNo(doctorNo);
            if (doctorInfo == null) {
                throw new BusinessException(ResultCode.DATA_NOT_EXIST.getCode(), "医生信息不存在");
            }

            // 获取关联的用户信息
            Result<User> userResult = userService.getUserById(doctorInfo.getUserId());
            if (userResult.isSuccess() && userResult.getData() != null) {
                doctorInfo.setUserInfo(userResult.getData());
            }

            log.info("根据医生编号获取医生信息成功，医生编号：{}", doctorNo);
            return Result.success("获取成功", doctorInfo);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("根据医生编号获取医生信息失败，医生编号：{}", doctorNo, e);
            throw new BusinessException(ResultCode.ERROR);
        }
    }

    @Override
    @Transactional
    public Result<Void> updateDoctorProfile(Long userId, User user, DoctorInfo doctorInfo) {
        log.info("更新医生个人信息，用户ID：{}", userId);

        try {
            if (userId == null || userId <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
            }

            // 1. 更新用户基础信息
            if (user != null) {
                user.setId(userId);
                user.setUpdateTime(LocalDateTime.now());
                Result<Void> userUpdateResult = userService.updateUser(userId, user);
                if (!userUpdateResult.isSuccess()) {
                    throw new BusinessException(ResultCode.ERROR.getCode(), "更新用户基础信息失败");
                }
            }

            // 2. 更新医生专业信息
            if (doctorInfo != null) {
                doctorInfo.setUserId(userId);
                doctorInfo.setUpdateTime(LocalDateTime.now());
                doctorInfo.setUpdateBy(userId);

                // 检查医生信息是否存在
                DoctorInfo existingDoctorInfo = doctorInfoMapper.selectByUserId(userId);
                if (existingDoctorInfo == null) {
                    // 创建新的医生信息
                    doctorInfo.setCreateTime(LocalDateTime.now());
                    doctorInfo.setCreateBy(userId);
                    doctorInfo.setStatus(1);
                    doctorInfo.setIsDeleted(0);
                    doctorInfo.setRating(java.math.BigDecimal.valueOf(5.00));
                    doctorInfo.setConsultationCount(0);
                    doctorInfo.setOnlineStatus(0);

                    int insertCount = doctorInfoMapper.insert(doctorInfo);
                    if (insertCount <= 0) {
                        throw new BusinessException(ResultCode.ERROR.getCode(), "创建医生信息失败");
                    }
                } else {
                    // 更新现有医生信息
                    int updateCount = doctorInfoMapper.updateByUserId(doctorInfo);
                    if (updateCount <= 0) {
                        throw new BusinessException(ResultCode.ERROR.getCode(), "更新医生信息失败");
                    }
                }
            }

            log.info("更新医生个人信息成功，用户ID：{}", userId);
            return Result.success("更新成功");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新医生个人信息失败，用户ID：{}", userId, e);
            throw new BusinessException(ResultCode.ERROR);
        }
    }

    @Override
    public Result<DoctorInfo> createDoctorInfo(DoctorInfo doctorInfo) {
        log.info("创建医生信息档案，用户ID：{}", doctorInfo.getUserId());

        try {
            if (doctorInfo == null || doctorInfo.getUserId() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "医生信息不能为空");
            }

            // 检查医生信息是否已存在
            DoctorInfo existingDoctorInfo = doctorInfoMapper.selectByUserId(doctorInfo.getUserId());
            if (existingDoctorInfo != null) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXIST.getCode(), "医生信息已存在");
            }

            // 设置默认值
            doctorInfo.setCreateTime(LocalDateTime.now());
            doctorInfo.setUpdateTime(LocalDateTime.now());
            doctorInfo.setStatus(1);
            doctorInfo.setIsDeleted(0);
            doctorInfo.setRating(java.math.BigDecimal.valueOf(5.00));
            doctorInfo.setConsultationCount(0);
            doctorInfo.setOnlineStatus(0);

            int insertCount = doctorInfoMapper.insert(doctorInfo);
            if (insertCount <= 0) {
                throw new BusinessException(ResultCode.ERROR.getCode(), "创建医生信息失败");
            }

            log.info("创建医生信息档案成功，用户ID：{}", doctorInfo.getUserId());
            return Result.success("创建成功", doctorInfo);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建医生信息档案失败，用户ID：{}", doctorInfo.getUserId(), e);
            throw new BusinessException(ResultCode.ERROR);
        }
    }

    @Override
    public Result<Void> updateOnlineStatus(Long userId, Integer onlineStatus) {
        log.info("更新医生在线状态，用户ID：{}，状态：{}", userId, onlineStatus);

        try {
            if (userId == null || userId <= 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
            }

            if (onlineStatus == null || (onlineStatus < 0 || onlineStatus > 2)) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "在线状态值无效");
            }

            DoctorInfo doctorInfo = new DoctorInfo();
            doctorInfo.setUserId(userId);
            doctorInfo.setOnlineStatus(onlineStatus);
            doctorInfo.setUpdateTime(LocalDateTime.now());
            doctorInfo.setUpdateBy(userId);

            int updateCount = doctorInfoMapper.updateByUserId(doctorInfo);
            if (updateCount <= 0) {
                throw new BusinessException(ResultCode.ERROR.getCode(), "更新在线状态失败");
            }

            log.info("更新医生在线状态成功，用户ID：{}", userId);
            return Result.success("更新成功");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新医生在线状态失败，用户ID：{}", userId, e);
            throw new BusinessException(ResultCode.ERROR);
        }
    }

    @Override
    public boolean isDoctorExists(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }

        try {
            DoctorInfo doctorInfo = doctorInfoMapper.selectByUserId(userId);
            return doctorInfo != null;
        } catch (Exception e) {
            log.error("检查医生是否存在失败，用户ID：{}", userId, e);
            return false;
        }
    }

    /**
     * 创建默认的医生信息
     */
    private DoctorInfo createDefaultDoctorInfo(User user) {
        DoctorInfo doctorInfo = new DoctorInfo();
        doctorInfo.setUserId(user.getId());
        doctorInfo.setDoctorNo(user.getUsername()); // 使用用户名作为医生编号
        doctorInfo.setDepartment("内分泌科"); // 默认科室
        doctorInfo.setTitle("医师"); // 默认职称
        doctorInfo.setHospital("默认医院"); // 默认医院
        doctorInfo.setRating(java.math.BigDecimal.valueOf(5.00));
        doctorInfo.setConsultationCount(0);
        doctorInfo.setOnlineStatus(0);
        doctorInfo.setStatus(1);
        doctorInfo.setCreateTime(LocalDateTime.now());
        doctorInfo.setUpdateTime(LocalDateTime.now());
        doctorInfo.setCreateBy(user.getId());
        doctorInfo.setUpdateBy(user.getId());
        doctorInfo.setIsDeleted(0);

        try {
            doctorInfoMapper.insert(doctorInfo);
            log.info("创建默认医生信息成功，用户ID：{}", user.getId());
        } catch (Exception e) {
            log.error("创建默认医生信息失败，用户ID：{}", user.getId(), e);
        }

        return doctorInfo;
    }

    // ======================= 统计相关方法实现 =======================

    @Override
    public long getTotalDoctorCount() {
        try {
            return doctorInfoMapper.countTotalDoctors();
        } catch (Exception e) {
            log.error("获取总医生数失败", e);
            return 0;
        }
    }

    @Override
    public long getActiveDoctorCount(LocalDateTime since) {
        try {
            if (since == null) {
                since = LocalDateTime.now().minusDays(30); // 默认30天内
            }
            return doctorInfoMapper.countActiveDoctors(since);
        } catch (Exception e) {
            log.error("获取活跃医生数失败", e);
            return 0;
        }
    }

    @Override
    public long getOnlineDoctorCount() {
        try {
            // 使用基于Redis的在线用户统计，userType=1表示医生
            return userService.getOnlineUserCountFromRedisByType(1);
        } catch (Exception e) {
            log.error("获取在线医生数失败", e);
            // 如果Redis统计失败，回退到数据库统计
            try {
                log.warn("Redis统计失败，回退到数据库统计");
                return doctorInfoMapper.countOnlineDoctors();
            } catch (Exception dbException) {
                log.error("数据库统计也失败", dbException);
            return 0;
            }
        }
    }

    // ======================= 管理员相关方法实现 =======================

    @Override
    public PageResult<Map<String, Object>> getDoctorListForAdmin(int page, int size, String keyword, 
                                                                String department, String title, Integer onlineStatus, 
                                                                Integer available, Integer status, 
                                                                LocalDateTime startTime, LocalDateTime endTime) {
        try {
            log.info("管理员查询医生列表，页码：{}，页面大小：{}", page, size);
            
            // 计算分页偏移量
            int offset = (page - 1) * size;
            
            // 查询医生列表数据
            List<Map<String, Object>> doctorList = doctorInfoMapper.selectDoctorListForAdmin(
                offset, size, keyword, department, title, onlineStatus, available, status, startTime, endTime);
            
            // 为每个医生动态计算实际咨询数量
            for (Map<String, Object> doctor : doctorList) {
                Long doctorId = (Long) doctor.get("id");
                Long userId = (Long) doctor.get("user_id"); 
                log.info("处理医生统计 - 医生ID：{}，用户ID：{}，姓名：{}", doctorId, userId, doctor.get("real_name"));
                
                if (userId != null) {
                    try {
                        // 注意：ConsultationChat中的doctorId字段实际存储的是用户ID
                        long actualConsultationCount = consultationChatRepository.countByDoctorId(userId);
                        log.info("用户ID：{} (医生ID：{}) 的咨询数量：{}", userId, doctorId, actualConsultationCount);
                        doctor.put("consultation_count", actualConsultationCount);
                    } catch (Exception e) {
                        log.warn("计算医生咨询数量失败，用户ID：{}，医生ID：{}，错误：{}", userId, doctorId, e.getMessage());
                        doctor.put("consultation_count", 0);
                    }
                }
            }
            
            // 查询总数
            long total = doctorInfoMapper.countDoctorListForAdmin(
                keyword, department, title, onlineStatus, available, status, startTime, endTime);
            
            // 构建分页结果
            PageResult<Map<String, Object>> result = new PageResult<>();
            result.setRecords(doctorList);
            result.setTotal(total);
            result.setCurrent(page);
            result.setSize(size);
            result.setPages((long) Math.ceil((double) total / size));
            
            return result;
            
        } catch (Exception e) {
            log.error("查询医生列表失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "查询医生列表失败");
        }
    }

    @Override
    public Map<String, Object> getDoctorStatistics() {
        try {
            log.info("查询医生统计数据");
            
            Map<String, Object> stats = new HashMap<>();
            
            // 总医生数
            stats.put("total", getTotalDoctorCount());
            
            // 在线医生数（基于Redis）
            stats.put("online", getOnlineDoctorCount());
            
            // 可接诊医生数
            long availableDoctors = doctorInfoMapper.countAvailableDoctors();
            stats.put("available", availableDoctors);
            
            // 咨询中医生数（模拟数据，实际可以从咨询表查询）
            long consultingDoctors = Math.round(availableDoctors * 0.4);
            stats.put("consulting", consultingDoctors);
            
            return stats;
            
        } catch (Exception e) {
            log.error("查询医生统计数据失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "查询医生统计数据失败");
        }
    }

    @Override
    public Result<Void> updateDoctorAvailable(Long doctorId, Integer available) {
        try {
            log.info("更新医生可接诊状态，医生ID：{}，状态：{}", doctorId, available);
            
            if (doctorId == null || doctorId <= 0) {
                return Result.error("医生ID不能为空");
            }
            
            if (available == null || (available != 0 && available != 1)) {
                return Result.error("可接诊状态参数错误");
            }
            
            // 检查医生是否存在
            DoctorInfo doctorInfo = doctorInfoMapper.selectById(doctorId);
            if (doctorInfo == null) {
                return Result.error("医生不存在");
            }
            
            // 更新可接诊状态
            DoctorInfo updateInfo = new DoctorInfo();
            updateInfo.setId(doctorId);
            updateInfo.setStatus(available);  // 使用status字段表示可接诊状态
            updateInfo.setUpdateTime(LocalDateTime.now());
            
            int rows = doctorInfoMapper.updateById(updateInfo);
            if (rows > 0) {
                log.info("医生可接诊状态更新成功，医生ID：{}", doctorId);
                return Result.success("医生可接诊状态更新成功");
            } else {
                return Result.error("医生可接诊状态更新失败");
            }
            
        } catch (Exception e) {
            log.error("更新医生可接诊状态失败，医生ID：{}", doctorId, e);
            return Result.error("更新医生可接诊状态失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateDoctorUserStatus(Long doctorId, Integer status) {
        try {
            log.info("更新医生用户状态，医生ID：{}，状态：{}", doctorId, status);
            
            if (doctorId == null || doctorId <= 0) {
                return Result.error("医生ID不能为空");
            }
            
            if (status == null || (status != 0 && status != 1)) {
                return Result.error("用户状态参数错误");
            }
            
            // 获取医生信息
            DoctorInfo doctorInfo = doctorInfoMapper.selectById(doctorId);
            if (doctorInfo == null) {
                return Result.error("医生不存在");
            }
            
            // 更新用户状态
            return userService.updateUserStatus(doctorInfo.getUserId(), status);
            
        } catch (Exception e) {
            log.error("更新医生用户状态失败，医生ID：{}", doctorId, e);
            return Result.error("更新医生用户状态失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateDoctorInfo(Long doctorId, Map<String, Object> doctorData) {
        try {
            log.info("更新医生信息，医生ID：{}", doctorId);
            
            if (doctorId == null || doctorId <= 0) {
                return Result.error("医生ID不能为空");
            }
            
            // 获取当前医生信息
            DoctorInfo currentDoctor = doctorInfoMapper.selectById(doctorId);
            if (currentDoctor == null) {
                return Result.error("医生不存在");
            }
            
            // 更新医生信息
            DoctorInfo updateInfo = new DoctorInfo();
            updateInfo.setId(doctorId);
            updateInfo.setUpdateTime(LocalDateTime.now());
            
            // 从doctorData中提取医生专业信息
            if (doctorData.containsKey("department")) {
                updateInfo.setDepartment((String) doctorData.get("department"));
            }
            if (doctorData.containsKey("title")) {
                updateInfo.setTitle((String) doctorData.get("title"));
            }
            if (doctorData.containsKey("experience")) {
                Object expObj = doctorData.get("experience");
                if (expObj instanceof Integer) {
                    updateInfo.setExperience((Integer) expObj);
                } else if (expObj instanceof String) {
                    updateInfo.setExperience(Integer.valueOf((String) expObj));
                }
            }
            if (doctorData.containsKey("introduction")) {
                updateInfo.setIntroduction((String) doctorData.get("introduction"));
            }
            if (doctorData.containsKey("speciality")) {
                updateInfo.setSpeciality((String) doctorData.get("speciality"));
            }
            if (doctorData.containsKey("available")) {
                Object availObj = doctorData.get("available");
                if (availObj instanceof Integer) {
                    updateInfo.setStatus((Integer) availObj);  // 使用status字段表示可接诊状态
                } else if (availObj instanceof String) {
                    updateInfo.setStatus(Integer.valueOf((String) availObj));
                }
            }
            
            // 更新医生专业信息
            int doctorRows = doctorInfoMapper.updateById(updateInfo);
            
            // 更新用户基础信息
            if (doctorData.containsKey("realName") || doctorData.containsKey("email") || 
                doctorData.containsKey("phone") || doctorData.containsKey("gender") ||
                doctorData.containsKey("status")) {
                
                User updateUser = new User();
                updateUser.setId(currentDoctor.getUserId());
                
                if (doctorData.containsKey("realName")) {
                    updateUser.setRealName((String) doctorData.get("realName"));
                }
                if (doctorData.containsKey("email")) {
                    updateUser.setEmail((String) doctorData.get("email"));
                }
                if (doctorData.containsKey("phone")) {
                    updateUser.setPhone((String) doctorData.get("phone"));
                }
                if (doctorData.containsKey("gender")) {
                    updateUser.setGenderFromString(doctorData.get("gender"));
                }
                if (doctorData.containsKey("status")) {
                    Object statusObj = doctorData.get("status");
                    if (statusObj instanceof Integer) {
                        updateUser.setStatus((Integer) statusObj);
                    } else if (statusObj instanceof String) {
                        updateUser.setStatus(Integer.valueOf((String) statusObj));
                    }
                }
                
                userService.updateUser(currentDoctor.getUserId(), updateUser);
            }
            
            if (doctorRows > 0) {
                log.info("医生信息更新成功，医生ID：{}", doctorId);
                return Result.success("医生信息更新成功");
            } else {
                return Result.error("医生信息更新失败");
            }
            
        } catch (Exception e) {
            log.error("更新医生信息失败，医生ID：{}", doctorId, e);
            return Result.error("更新医生信息失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> deleteDoctor(Long doctorId) {
        try {
            log.info("删除医生，医生ID：{}", doctorId);
            
            if (doctorId == null || doctorId <= 0) {
                return Result.error("医生ID不能为空");
            }
            
            // 获取医生信息
            DoctorInfo doctorInfo = doctorInfoMapper.selectById(doctorId);
            if (doctorInfo == null) {
                return Result.error("医生不存在");
            }
            
            // 逻辑删除医生信息
            int doctorRows = doctorInfoMapper.deleteById(doctorId);
            
            // 逻辑删除用户信息
            Result<Void> userResult = userService.deleteUser(doctorInfo.getUserId());
            
            if (doctorRows > 0 && userResult.isSuccess()) {
                log.info("医生删除成功，医生ID：{}", doctorId);
                return Result.success("医生删除成功");
            } else {
                return Result.error("医生删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除医生失败，医生ID：{}", doctorId, e);
            return Result.error("删除医生失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> batchDeleteDoctors(List<Long> doctorIds) {
        try {
            log.info("批量删除医生，医生ID列表：{}", doctorIds);
            
            if (doctorIds == null || doctorIds.isEmpty()) {
                return Result.error("医生ID列表不能为空");
            }
            
            int successCount = 0;
            for (Long doctorId : doctorIds) {
                try {
                    Result<Void> result = deleteDoctor(doctorId);
                    if (result.isSuccess()) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("删除医生失败，医生ID：{}，错误：{}", doctorId, e.getMessage());
                }
            }
            
            if (successCount > 0) {
                log.info("批量删除医生完成，成功删除：{}个", successCount);
                return Result.success("批量删除完成，成功删除 " + successCount + " 个医生");
            } else {
                return Result.error("批量删除失败");
            }
            
        } catch (Exception e) {
            log.error("批量删除医生失败", e);
            return Result.error("批量删除医生失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getDoctorDetailForAdmin(Long doctorId) {
        try {
            log.info("查询医生详情，医生ID：{}", doctorId);
            
            if (doctorId == null || doctorId <= 0) {
                return Result.error("医生ID不能为空");
            }
            
            // 查询医生详细信息（包含用户基础信息）
            Map<String, Object> doctorDetail = doctorInfoMapper.selectDoctorDetailForAdmin(doctorId);
            if (doctorDetail == null) {
                return Result.error("医生不存在");
            }
            
            // 动态计算实际咨询数量
            try {
                Long userId = (Long) doctorDetail.get("user_id");
                if (userId != null) {
                    // 注意：ConsultationChat中的doctorId字段实际存储的是用户ID
                    long actualConsultationCount = consultationChatRepository.countByDoctorId(userId);
                    log.info("查询医生详情 - 用户ID：{} (医生ID：{}) 的咨询数量：{}", userId, doctorId, actualConsultationCount);
                    doctorDetail.put("consultation_count", actualConsultationCount);
                } else {
                    log.warn("医生详情中找不到用户ID，医生ID：{}", doctorId);
                    doctorDetail.put("consultation_count", 0);
                }
            } catch (Exception e) {
                log.warn("计算医生咨询数量失败，医生ID：{}，错误：{}", doctorId, e.getMessage());
                doctorDetail.put("consultation_count", 0);
            }
            
            // 清除敏感信息
            doctorDetail.remove("password");
            
            return Result.success(doctorDetail);
            
        } catch (Exception e) {
            log.error("查询医生详情失败，医生ID：{}", doctorId, e);
            return Result.error("查询医生详情失败：" + e.getMessage());
        }
    }
} 