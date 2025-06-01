package com.lijian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lijian.entity.Interview;
import com.lijian.result.Result;
import com.lijian.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试管理控制器
 *
 * @author lijian
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/interview")
@Validated
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    /**
     * 创建面试
     */
    @PostMapping
    public Result<Interview> createInterview(@Valid @RequestBody Interview interview) {
        Interview createdInterview = interviewService.createInterview(interview);
        return Result.success(createdInterview);
    }

    /**
     * 获取面试详情
     */
    @GetMapping("/{id}")
    public Result<Interview> getInterviewDetail(@PathVariable("id") Long id) {
        Interview interview = interviewService.getById(id);
        return Result.success(interview);
    }

    /**
     * 根据候选人ID获取面试列表
     */
    @GetMapping("/candidate/{candidateId}")
    public Result<List<Interview>> getByCandidateId(@PathVariable("candidateId") Long candidateId) {
        List<Interview> interviews = interviewService.getByCandidateId(candidateId);
        return Result.success(interviews);
    }

    /**
     * 根据面试官ID获取面试列表
     */
    @GetMapping("/interviewer/{interviewerId}")
    public Result<List<Interview>> getByInterviewerId(@PathVariable("interviewerId") Long interviewerId) {
        List<Interview> interviews = interviewService.getByInterviewerId(interviewerId);
        return Result.success(interviews);
    }

    /**
     * 根据状态获取面试列表
     */
    @GetMapping("/status/{status}")
    public Result<List<Interview>> getByStatus(@PathVariable("status") String status) {
        List<Interview> interviews = interviewService.getByStatus(status);
        return Result.success(interviews);
    }

    /**
     * 分页查询面试列表
     */
    @GetMapping("/page")
    public Result<Page<Interview>> pageInterviews(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String position,
                                                  @RequestParam(required = false) Long candidateId,
                                                  @RequestParam(required = false) Long interviewerId) {
        Page<Interview> page = new Page<>(current, size);
        Page<Interview> resultPage = interviewService.pageInterviews(page, status, position, candidateId, interviewerId);
        return Result.success(resultPage);
    }

    /**
     * 开始面试
     */
    @PostMapping("/start/{id}")
    public Result<Boolean> startInterview(@PathVariable("id") Long id) {
        boolean result = interviewService.startInterview(id);
        return Result.success(result);
    }

    /**
     * 结束面试
     */
    @PostMapping("/end/{id}")
    public Result<Boolean> endInterview(@PathVariable("id") Long id) {
        boolean result = interviewService.endInterview(id);
        return Result.success(result);
    }

    /**
     * 取消面试
     */
    @PostMapping("/cancel/{id}")
    public Result<Boolean> cancelInterview(@PathVariable("id") Long id,
                                           @RequestParam(required = false) String reason) {
        boolean result = interviewService.cancelInterview(id, reason);
        return Result.success(result);
    }

    /**
     * 更新面试信息
     */
    @PutMapping
    public Result<Boolean> updateInterview(@Valid @RequestBody Interview interview) {
        boolean result = interviewService.updateById(interview);
        return Result.success(result);
    }

    /**
     * 删除面试
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteInterview(@PathVariable("id") Long id) {
        boolean result = interviewService.removeById(id);
        return Result.success(result);
    }

    /**
     * 根据房间号获取面试信息
     */
    @GetMapping("/room/{roomCode}")
    public Result<Interview> getByRoomCode(@PathVariable("roomCode") String roomCode) {
        Interview interview = interviewService.getByRoomCode(roomCode);
        return Result.success(interview);
    }

    /**
     * 检查面试状态
     */
    @GetMapping("/check-status/{id}")
    public Result<String> checkInterviewStatus(@PathVariable("id") Long id) {
        String status = interviewService.checkInterviewStatus(id);
        return Result.success(status);
    }

    /**
     * 获取今日面试列表
     */
    @GetMapping("/today")
    public Result<List<Interview>> getTodayInterviews(@RequestParam(required = false) Long interviewerId) {
        List<Interview> interviews = interviewService.getTodayInterviews(interviewerId);
        return Result.success(interviews);
    }

    /**
     * 获取即将开始的面试
     */
    @GetMapping("/upcoming")
    public Result<List<Interview>> getUpcomingInterviews(@RequestParam(required = false) Long userId,
                                                         @RequestParam(defaultValue = "24") Integer hours) {
        List<Interview> interviews = interviewService.getUpcomingInterviews(userId, hours);
        return Result.success(interviews);
    }

    /**
     * 重新安排面试时间
     */
    @PostMapping("/reschedule/{id}")
    public Result<Boolean> rescheduleInterview(@PathVariable("id") Long id,
                                               @RequestParam LocalDateTime newTime,
                                               @RequestParam(required = false) String reason) {
        boolean result = interviewService.rescheduleInterview(id, newTime, reason);
        return Result.success(result);
    }

    /**
     * 获取面试统计信息
     */
    @GetMapping("/statistics")
    public Result<Object> getInterviewStatistics(@RequestParam(required = false) Long interviewerId,
                                                 @RequestParam(required = false) String startDate,
                                                 @RequestParam(required = false) String endDate) {
        Object statistics = interviewService.getInterviewStatistics(interviewerId, startDate, endDate);
        return Result.success(statistics);
    }

    /**
     * 批量更新面试状态
     */
    @PostMapping("/batch-status")
    public Result<Boolean> batchUpdateStatus(@RequestBody List<Long> interviewIds,
                                             @RequestParam String status) {
        boolean result = interviewService.batchUpdateStatus(interviewIds, status);
        return Result.success(result);
    }
}