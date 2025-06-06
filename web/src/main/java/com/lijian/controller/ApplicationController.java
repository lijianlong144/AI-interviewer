package com.lijian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lijian.entity.Application;
import com.lijian.result.Result;
import com.lijian.service.ApplicationService;
import com.lijian.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 职位申请控制器
 */
@RestController
@RequestMapping("/api/application")
@Validated
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    /**
     * 创建职位申请
     */
    @PostMapping
    public Result<Application> createApplication(@Valid @RequestBody Application application) {
        // 设置当前用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        application.setCandidateId(currentUserId);
        
        Application createdApplication = applicationService.createApplication(application);
        return Result.success(createdApplication);
    }

    /**
     * 获取申请详情
     */
    @GetMapping("/{id}")
    public Result<Application> getApplicationDetail(@PathVariable("id") Long id) {
        Application application = applicationService.getById(id);
        return Result.success(application);
    }

    /**
     * 更新申请状态（HR操作）
     */
    @PutMapping("/status/{id}")
    public Result<Boolean> updateStatus(@PathVariable("id") Long id,
                                        @RequestParam(value = "status") String status,
                                        @RequestParam(value = "remark", required = false) String remark) {
        boolean result = applicationService.updateStatus(id, status, remark);
        return Result.success(result);
    }

    /**
     * 获取当前用户的所有申请
     */
    @GetMapping("/my")
    public Result<List<Application>> getMyApplications() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<Application> applications = applicationService.getApplicationsByCandidateId(currentUserId);
        return Result.success(applications);
    }

    /**
     * 检查当前用户是否已申请职位
     */
    @GetMapping("/check")
    public Result<Boolean> checkIfApplied(@RequestParam(value = "positionId" ) Long positionId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean hasApplied = applicationService.checkIfApplied(currentUserId, positionId);
        return Result.success(hasApplied);
    }

    /**
     * 获取职位的所有申请（HR操作）
     */
    @GetMapping("/position/{positionId}")
    public Result<List<Application>> getApplicationsByPosition(@PathVariable("positionId") Long positionId) {
        List<Application> applications = applicationService.getApplicationsByPositionId(positionId);
        return Result.success(applications);
    }

    /**
     * 分页查询申请列表（HR操作）
     */
    @GetMapping("/page")
    public Result<Page<Application>> pageApplications(@RequestParam(value = "current", defaultValue = "1") Integer current,
                                                     @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                     @RequestParam(value = "candidateId", required = false) Long candidateId,
                                                     @RequestParam(value = "positionId", required = false) Long positionId,
                                                     @RequestParam(value = "status", required = false) String status) {
        Page<Application> page = new Page<>(current, size);
        Page<Application> resultPage = applicationService.pageApplications(page, candidateId, positionId, status);
        return Result.success(resultPage);
    }

    /**
     * 获取申请状态统计（HR操作）
     */
    @GetMapping("/statistics")
    public Result<Map<String, Long>> getStatusStatistics() {
        Map<String, Long> statistics = applicationService.getStatusStatistics();
        return Result.success(statistics);
    }
}