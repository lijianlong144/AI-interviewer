package com.lijian.controller;

import com.lijian.entity.Application;
import com.lijian.entity.Interview;
import com.lijian.entity.Position;
import com.lijian.result.Result;
import com.lijian.service.InterviewProcessService;
import com.lijian.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试流程控制器
 */
@RestController
@RequestMapping("/api/interview-process")
@Validated
public class InterviewProcessController {

    @Autowired
    private InterviewProcessService interviewProcessService;

    /**
     * HR发布职位
     */
    @PostMapping("/position/publish")
    public Result<Position> publishPosition(@Valid @RequestBody Position position) {
        Position publishedPosition = interviewProcessService.publishPosition(position);
        return Result.success(publishedPosition);
    }

    /**
     * 候选人投递简历
     */
    @PostMapping("/apply")
    public Result<Application> applyPosition(@RequestParam(value ="positionId" ) Long positionId, @RequestParam(value = "resumeId") Long resumeId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Application application = interviewProcessService.applyPosition(currentUserId, positionId, resumeId);
        return Result.success(application);
    }

    /**
     * HR初筛简历
     */
    @PutMapping("/screen/{applicationId}")
    public Result<Boolean> screenResume(@PathVariable("applicationId") Long applicationId,
                                       @RequestParam(value = "status") String status,
                                       @RequestParam(value = "remark", required = false) String remark) {
        boolean result = interviewProcessService.screenResume(applicationId, status, remark);
        return Result.success(result);
    }

    /**
     * 安排面试
     */
    @PostMapping("/schedule")
    public Result<Interview> scheduleInterview(@RequestParam(value = "applicationId") Long applicationId,
                                             @RequestParam(value = "interviewerId",required = false) Long interviewerId,
                                             @RequestParam(value = "scheduledTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledTime,
                                             @RequestParam(value = "duration") Integer duration) {
        Interview interview = interviewProcessService.scheduleInterview(applicationId, interviewerId, scheduledTime, duration);
        return Result.success(interview);
    }

    /**
     * 发送面试邀请邮件
     */
    @PostMapping("/invitation/{interviewId}")
    public Result<Long> sendInterviewInvitation(@PathVariable("interviewId") Long interviewId) {
        Long logId = interviewProcessService.sendInterviewInvitation(interviewId);
        return Result.success(logId);
    }

    /**
     * 候选人确认面试
     */
    @PutMapping("/confirm/{interviewId}")
    public Result<Boolean> confirmInterview(@PathVariable("interviewId") Long interviewId,
                                          @RequestParam boolean confirmed,
                                          @RequestParam(required = false) String reason) {
        boolean result = interviewProcessService.confirmInterview(interviewId, confirmed, reason);
        return Result.success(result);
    }

    /**
     * 开始面试
     */
    @PutMapping("/start/{interviewId}")
    public Result<Boolean> startInterview(@PathVariable("interviewId") Long interviewId) {
        boolean result = interviewProcessService.startInterview(interviewId);
        return Result.success(result);
    }

    /**
     * 结束面试
     */
    @PutMapping("/end/{interviewId}")
    public Result<Boolean> endInterview(@PathVariable("interviewId") Long interviewId) {
        boolean result = interviewProcessService.endInterview(interviewId);
        return Result.success(result);
    }

    /**
     * 生成面试报告
     */
    @PostMapping("/report/{interviewId}")
    public Result<Long> generateInterviewReport(@PathVariable("interviewId") Long interviewId) {
        Long reportId = interviewProcessService.generateInterviewReport(interviewId);
        return Result.success(reportId);
    }

    /**
     * 发送面试结果通知
     */
    @PostMapping("/result/{interviewId}")
    public Result<Long> sendInterviewResult(@PathVariable("interviewId") Long interviewId,
                                          @RequestParam String result,
                                          @RequestParam String feedback,
                                          @RequestParam(required = false) String nextSteps) {
        Long logId = interviewProcessService.sendInterviewResult(interviewId, result, feedback, nextSteps);
        return Result.success(logId);
    }

    /**
     * 获取候选人的面试流程状态
     */
    @GetMapping("/candidate/process")
    public Result<List<Object>> getCandidateInterviewProcess() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<Object> processList = interviewProcessService.getCandidateInterviewProcess(currentUserId);
        return Result.success(processList);
    }

    /**
     * 获取职位的申请和面试统计
     */
    @GetMapping("/position/statistics/{positionId}")
    public Result<Object> getPositionStatistics(@PathVariable("positionId") Long positionId) {
        Object statistics = interviewProcessService.getPositionStatistics(positionId);
        return Result.success(statistics);
    }
}