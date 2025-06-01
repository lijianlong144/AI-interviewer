package com.lijian.controller;

import com.lijian.result.Result;
import com.lijian.entity.InterviewReport;
import com.lijian.service.InterviewReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
        InterviewReport report = reportService.getByInterviewId(interviewId);
        return Result.success(report);
    }

    /**
     * 根据候选人ID获取报告列表
     */
    @GetMapping("/candidate/{candidateId}")
    public Result<List<InterviewReport>> getByCandidateId(@PathVariable("candidateId") Long candidateId) {
        List<InterviewReport> reports = reportService.getByCandidateId(candidateId);
        return Result.success(reports);
    }

    /**
     * 生成面试报告
     */
    @PostMapping("/generate/{interviewId}")
    public Result<InterviewReport> generateReport(@PathVariable("interviewId") Long interviewId) {
        InterviewReport report = reportService.generateReport(interviewId);
        return Result.success(report);
    }

    /**
     * 更新面试报告
     */
    @PutMapping
    public Result<Boolean> updateReport(@Valid @RequestBody InterviewReport report) {
        boolean result = reportService.updateById(report);
        return Result.success(result);
    }

    /**
     * 更新HR评语
     */
    @PostMapping("/hr-comment/{id}")
    public Result<Boolean> updateHrComment(@PathVariable("id") Long id,
                                           @RequestParam String hrComment) {
        boolean result = reportService.updateHrComment(id, hrComment);
        return Result.success(result);
    }

    /**
     * 更新推荐决策
     */
    @PostMapping("/recommendation/{id}")
    public Result<Boolean> updateRecommendation(@PathVariable("id") Long id,
                                                @RequestParam String recommendation) {
        boolean result = reportService.updateRecommendation(id, recommendation);
        return Result.success(result);
    }

    /**
     * 批量生成报告
     */
    @PostMapping("/batch-generate")
    public Result<List<InterviewReport>> batchGenerateReports(@RequestBody List<Long> interviewIds) {
        List<InterviewReport> reports = reportService.batchGenerateReports(interviewIds);
        return Result.success(reports);
    }

    /**
     * 获取报告统计信息
     */
    @GetMapping("/statistics")
    public Result<Object> getReportStatistics(@RequestParam(required = false) Long interviewerId,
                                              @RequestParam(required = false) String startDate,
                                              @RequestParam(required = false) String endDate) {
        Object statistics = reportService.getReportStatistics(interviewerId, startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 根据推荐决策获取报告列表
     */
    @GetMapping("/recommendation/{recommendation}")
    public Result<List<InterviewReport>> getByRecommendation(@PathVariable("recommendation") String recommendation) {
        List<InterviewReport> reports = reportService.getByRecommendation(recommendation);
        return Result.success(reports);
    }

    /**
     * 根据评分范围获取报告列表
     */
    @GetMapping("/score-range")
    public Result<List<InterviewReport>> getByScoreRange(@RequestParam Double minScore,
                                                         @RequestParam Double maxScore) {
        List<InterviewReport> reports = reportService.getByScoreRange(minScore, maxScore);
        return Result.success(reports);
    }

    /**
     * 导出面试报告
     */
    @GetMapping("/export/{id}")
    public Result<String> exportReport(@PathVariable("id") Long id,
                                       @RequestParam(defaultValue = "PDF") String format) {
        String exportPath = reportService.exportReport(id, format);
        return Result.success(exportPath);
    }

    /**
     * 重新生成报告
     */
    @PostMapping("/regenerate/{interviewId}")
    public Result<InterviewReport> regenerateReport(@PathVariable("interviewId") Long interviewId,
                                                    @RequestParam(defaultValue = "false") Boolean forceRegenerate) {
        InterviewReport report = reportService.regenerateReport(interviewId, forceRegenerate);
        return Result.success(report);
    }

    /**
     * 获取面试报告模板
     */
    @GetMapping("/template")
    public Result<Object> getReportTemplate() {
        Object template = reportService.getReportTemplate();
        return Result.success(template);
    }

    /**
     * 删除面试报告
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteReport(@PathVariable("id") Long id) {
        boolean result = reportService.removeById(id);
        return Result.success(result);
    }

    /**
     * 获取优秀报告列表（用于学习参考）
     */
    @GetMapping("/excellent")
    public Result<List<InterviewReport>> getExcellentReports(@RequestParam(defaultValue = "10") Integer limit) {
        List<InterviewReport> reports = reportService.getExcellentReports(limit);
        return Result.success(reports);
    }
}