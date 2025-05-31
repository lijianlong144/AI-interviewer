package com.lijian.controller;

import com.lijian.result.Result;
import com.lijian.entity.InterviewReport;
import com.lijian.service.InterviewReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 面试报告控制器
 * 
 * @author lijian
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/interview-report")
@Validated
public class InterviewReportController {
    
    @Autowired
    private InterviewReportService reportService;
    
    /**
     * 获取面试报告详情
     */
    @GetMapping("/{id}")
    public Result<InterviewReport> getReportDetail(@PathVariable("id") Long id) {
        InterviewReport report = reportService.getById(id);
        return Result.success(report);
    }
    
    /**
     * 根据面试ID获取报告
     */
    @GetMapping("/interview/{interviewId}")
    public Result<InterviewReport> getReportByInterviewId(@PathVariable("interviewId") Long interviewId) {
        // 这里需要在InterviewReportService中实现getByInterviewId方法
        // InterviewReport report = reportService.getByInterviewId(interviewId);
        return Result.success(null);
    }
    
    /**
     * 根据候选人ID获取报告列表
     */
    @GetMapping("/candidate/{candidateId}")
    public Result<List<InterviewReport>> getByCandidateId(@PathVariable("candidateId") Long candidateId) {
        // 这里需要在InterviewReportService中实现getByCandidateId方法
        // List<InterviewReport> reports = reportService.getByCandidateId(candidateId);
        return Result.success(null);
    }
    
    /**
     * 生成面试报告
     */
    @PostMapping("/generate/{interviewId}")
    public Result<InterviewReport> generateReport(@PathVariable("interviewId") Long interviewId) {
        // 这里需要在InterviewReportService中实现generateReport方法
        // InterviewReport report = reportService.generateReport(interviewId);
        return Result.success(null);
    }
    
    /**
     * 更新面试报告
     */
    @PutMapping
    public Result<Boolean> updateReport(@Valid @RequestBody InterviewReport report) {
        boolean result = reportService.updateById(report);
        return Result.success(result);
    }
} 