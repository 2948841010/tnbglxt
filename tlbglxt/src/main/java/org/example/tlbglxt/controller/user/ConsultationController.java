package org.example.tlbglxt.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.tlbglxt.common.Result;
import org.example.tlbglxt.entity.DoctorInfo;
import org.example.tlbglxt.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端咨询控制器
 *
 * @author 开发团队
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "用户端咨询管理", description = "用户端在线咨询相关接口")
@RestController
@RequestMapping("/api/user/consultation")
@Validated
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    // ============================== 医生列表查询 ==============================

    /**
     * 获取可咨询的医生列表
     */
    @Operation(summary = "获取医生列表", description = "获取系统中所有可咨询的医生列表")
    @GetMapping("/doctors")
    public Result<List<DoctorInfo>> getDoctorList(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.info("获取医生列表请求 - 科室：{}，关键词：{}，页码：{}，大小：{}", 
                department, keyword, page, size);
        
        try {
            List<DoctorInfo> doctors = consultationService.getAvailableDoctors(
                    department, keyword, page, size);
            
            log.info("获取医生列表成功，共{}位医生", doctors.size());
            return Result.success("获取成功", doctors);
            
        } catch (Exception e) {
            log.error("获取医生列表失败", e);
            return Result.error("获取医生列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取医生详细信息
     */
    @Operation(summary = "获取医生详情", description = "根据医生ID获取详细信息")
    @GetMapping("/doctors/{doctorId}")
    public Result<DoctorInfo> getDoctorDetail(@PathVariable Long doctorId) {
        log.info("获取医生详情请求，医生ID：{}", doctorId);
        
        try {
            DoctorInfo doctorInfo = consultationService.getDoctorDetail(doctorId);
            
            if (doctorInfo == null) {
                return Result.error("医生不存在");
            }
            
            log.info("获取医生详情成功，医生：{}", doctorInfo.getUserInfo().getRealName());
            return Result.success("获取成功", doctorInfo);
            
        } catch (Exception e) {
            log.error("获取医生详情失败，医生ID：{}", doctorId, e);
            return Result.error("获取医生详情失败：" + e.getMessage());
        }
    }

    // ============================== 咨询功能（暂时占位符）==============================

    /**
     * 发起咨询（占位符）
     */
    @Operation(summary = "发起咨询", description = "向指定医生发起咨询")
    @PostMapping("/create")
    public Result<Void> createConsultation(@RequestParam Long doctorId) {
        log.info("发起咨询请求，医生ID：{}", doctorId);
        
        // TODO: 实现发起咨询的逻辑
        return Result.success("咨询功能正在开发中，敬请期待！");
    }

    /**
     * 获取我的咨询列表（占位符）
     */
    @Operation(summary = "获取我的咨询", description = "获取当前用户的咨询记录")
    @GetMapping("/my")
    public Result<Void> getMyConsultations() {
        log.info("获取我的咨询请求");
        
        // TODO: 实现获取咨询列表的逻辑
        return Result.success("我的咨询功能正在开发中，敬请期待！");
    }

    /**
     * 获取咨询详情（占位符）
     */
    @Operation(summary = "获取咨询详情", description = "获取指定咨询的详细对话记录")
    @GetMapping("/{consultationId}")
    public Result<Void> getConsultationDetail(@PathVariable Long consultationId) {
        log.info("获取咨询详情请求，咨询ID：{}", consultationId);
        
        // TODO: 实现获取咨询详情的逻辑
        return Result.success("咨询详情功能正在开发中，敬请期待！");
    }

    /**
     * 获取所有科室列表
     */
    @Operation(summary = "获取科室列表", description = "获取系统中所有医生的科室列表")
    @GetMapping("/departments")
    public Result<List<String>> getDepartments() {
        log.info("获取科室列表请求");
        
        try {
            List<String> departments = consultationService.getAllDepartments();
            
            log.info("获取科室列表成功，共{}个科室", departments.size());
            return Result.success("获取成功", departments);
            
        } catch (Exception e) {
            log.error("获取科室列表失败", e);
            return Result.error("获取科室列表失败：" + e.getMessage());
        }
    }
} 