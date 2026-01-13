package org.example.tlbglxt.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.entity.DoctorInfo;
import org.example.tlbglxt.entity.User;
import org.example.tlbglxt.mapper.DoctorInfoMapper;
import org.example.tlbglxt.mapper.UserMapper;
import org.example.tlbglxt.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 咨询服务实现类
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class ConsultationServiceImpl implements ConsultationService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DoctorInfoMapper doctorInfoMapper;

    @Override
    public List<DoctorInfo> getAvailableDoctors(String department, String keyword, Integer page, Integer size) {
        log.info("查询可咨询医生列表 - 科室：{}，关键词：{}，页码：{}，大小：{}", 
                department, keyword, page, size);

        try {
            // 1. 获取所有医生用户（user_type = 1，状态正常）
            List<User> doctorUsers = userMapper.selectDoctorUsers();
            
            if (doctorUsers.isEmpty()) {
                log.warn("系统中暂无医生用户");
                return new ArrayList<>();
            }

            // 2. 获取医生的详细信息并组合
            List<DoctorInfo> allDoctors = new ArrayList<>();
            for (User doctorUser : doctorUsers) {
                DoctorInfo doctorInfo = doctorInfoMapper.selectByUserId(doctorUser.getId());
                if (doctorInfo != null && doctorInfo.getStatus() == 1) { // 只返回状态正常的医生
                    doctorInfo.setUserInfo(doctorUser);
                    allDoctors.add(doctorInfo);
                }
            }

            // 3. 根据条件筛选
            List<DoctorInfo> filteredDoctors = allDoctors.stream()
                    .filter(doctor -> {
                        // 科室筛选
                        if (StringUtils.hasText(department)) {
                            return department.equals(doctor.getDepartment());
                        }
                        return true;
                    })
                    .filter(doctor -> {
                        // 关键词搜索（搜索医生姓名、科室、专长）
                        if (StringUtils.hasText(keyword)) {
                            String lowerKeyword = keyword.toLowerCase();
                            return (doctor.getUserInfo().getRealName() != null && 
                                   doctor.getUserInfo().getRealName().toLowerCase().contains(lowerKeyword))
                                || (doctor.getDepartment() != null && 
                                   doctor.getDepartment().toLowerCase().contains(lowerKeyword))
                                || (doctor.getSpeciality() != null && 
                                   doctor.getSpeciality().toLowerCase().contains(lowerKeyword));
                        }
                        return true;
                    })
                    .collect(Collectors.toList());

            // 4. 分页处理
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, filteredDoctors.size());
            
            if (startIndex >= filteredDoctors.size()) {
                return new ArrayList<>();
            }

            List<DoctorInfo> pagedDoctors = filteredDoctors.subList(startIndex, endIndex);
            
            log.info("查询医生列表成功 - 总数：{}，当前页：{}，返回：{}", 
                    filteredDoctors.size(), page, pagedDoctors.size());
            
            return pagedDoctors;

        } catch (Exception e) {
            log.error("查询可咨询医生列表失败", e);
            throw new RuntimeException("查询医生列表失败", e);
        }
    }

    @Override
    public DoctorInfo getDoctorDetail(Long doctorId) {
        log.info("查询医生详细信息，医生ID：{}", doctorId);

        try {
            // 1. 验证用户是否为医生
            User doctorUser = userMapper.selectById(doctorId);
            if (doctorUser == null || doctorUser.getUserType() != 1) {
                log.warn("用户不存在或不是医生，用户ID：{}", doctorId);
                return null;
            }

            // 2. 获取医生详细信息
            DoctorInfo doctorInfo = doctorInfoMapper.selectByUserId(doctorId);
            if (doctorInfo == null) {
                log.warn("医生详细信息不存在，医生ID：{}", doctorId);
                return null;
            }

            // 3. 组合用户基础信息
            doctorInfo.setUserInfo(doctorUser);

            log.info("查询医生详细信息成功，医生：{}", doctorUser.getRealName());
            return doctorInfo;

        } catch (Exception e) {
            log.error("查询医生详细信息失败，医生ID：{}", doctorId, e);
            throw new RuntimeException("查询医生详情失败", e);
        }
    }

    @Override
    public List<String> getAllDepartments() {
        log.info("查询所有科室列表");

        try {
            List<DoctorInfo> allDoctorInfos = doctorInfoMapper.selectAllActiveDoctors();
            
            List<String> departments = allDoctorInfos.stream()
                    .map(DoctorInfo::getDepartment)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());

            log.info("查询科室列表成功，共{}个科室", departments.size());
            return departments;

        } catch (Exception e) {
            log.error("查询科室列表失败", e);
            throw new RuntimeException("查询科室列表失败", e);
        }
    }
} 