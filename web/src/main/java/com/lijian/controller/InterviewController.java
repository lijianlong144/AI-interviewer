package com.lijian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lijian.dto.InterviewDTO;
import com.lijian.entity.Interview;
import com.lijian.result.Result;
import com.lijian.service.InterviewService;
import com.lijian.utils.SecurityUtils;
import com.lijian.enums.InterviewStatusEnum;
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
        // 设置创建者ID为当前登录用户ID
        Long currentUserId = com.lijian.utils.SecurityUtils.getCurrentUserId();
        interview.setCreatorId(currentUserId);
        
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
    public Result<Page<Interview>> pageInterviews(@RequestParam(value = "current", defaultValue = "1") Integer current,
                                                  @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                  @RequestParam(value = "status", required = false) String status,
                                                  @RequestParam(value = "position", required = false) String position,
                                                  @RequestParam(value = "candidateId", required = false) Long candidateId,
                                                  @RequestParam(value = "interviewerId", required = false) Long interviewerId) {
        Page<Interview> page = new Page<>(current, size);
        Page<Interview> resultPage = interviewService.pageInterviews(page, status, position, candidateId, interviewerId);
        return Result.success(resultPage);
    }
    
    /**
     * 分页查询面试列表，包含候选人姓名
     */
    @GetMapping("/page/with-candidate")
    public Result<Page<InterviewDTO>> pageInterviewsWithCandidate(
                                                  @RequestParam(value = "current", defaultValue = "1") Integer current,
                                                  @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                  @RequestParam(value = "status", required = false) String status,
                                                  @RequestParam(value = "position", required = false) String position,
                                                  @RequestParam(value = "candidateId", required = false) Long candidateId,
                                                  @RequestParam(value = "candidateName", required = false) String candidateName) {
        Page<InterviewDTO> page = new Page<>(current, size);
        Page<InterviewDTO> resultPage = interviewService.pageInterviewsWithCandidate(
            page, status, position, candidateId, null, candidateName);
        return Result.success(resultPage);
    }

    /**
     * 开始面试 (通过房间号)
     */
    @PostMapping("/start")
    public Result<Boolean> startInterview(@RequestParam("roomCode") String roomCode) {
        boolean result = interviewService.startInterviewByRoomCode(roomCode);
        return Result.success(result);
    }

    /**
     * 结束面试 (通过房间号)
     */
    @PostMapping("/end")
    public Result<Boolean> endInterview(@RequestParam("roomCode") String roomCode) {
        boolean result = interviewService.endInterviewByRoomCode(roomCode);
        return Result.success(result);
    }
    // /**
    //  * 结束面试 (通过房间号)
    //  */
    // @PostMapping("/end")
    // public Result<Boolean> endInterviewById(@RequestParam("InterviewId") String InterviewId) {


    //     boolean result = interviewService.endInterviewByRoomCode(roomCode);
    //     return Result.success(result);
    // }

    /**
     * 取消面试
     */
    @PostMapping("/cancel/{id}")
    public Result<Boolean> cancelInterview(@PathVariable("id") Long id,
                                           @RequestParam(value = "reason", required = false) String reason) {
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
        // 验证房间号格式
        if (!roomCode.matches("^IV-\\d{6}-[a-zA-Z0-9]{8}$")) {
            return Result.error("房间号格式不正确，正确格式为：IV-xxxxxx-xxxxxxxx");
        }
        
        Interview interview = interviewService.getByRoomCode(roomCode);
        
        // 检查面试是否存在
        if (interview == null) {
            return Result.error("房间号不存在，请确认后重试");
        }

        // 检查面试状态
        if (InterviewStatusEnum.CANCELLED.getCode().equals(interview.getStatus())) {
            return Result.error("该面试已取消");
        }
        
        // 检查面试时间
        if (interview.getScheduledTime() != null && 
            interview.getScheduledTime().isAfter(LocalDateTime.now().plusHours(24))) {
            return Result.error("面试尚未开始，请在预定时间前后进入");
        }

        // 检查当前用户是否有权限访问该面试
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (interview.getCandidateId().equals(currentUserId) || 
            interview.getInterviewerId().equals(currentUserId) || 
            interview.getCreatorId().equals(currentUserId)) {
            return Result.success(interview);
        } else {
            return Result.error("无权限访问此面试");
        }
    }

    /**
     * 分页查询当前候选人的面试列表
     */
    @GetMapping("/candidate/page")
    public Result<Page<Interview>> pageCandidateInterviews(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        
        // 获取当前登录的候选人ID
        Long candidateId = SecurityUtils.getCurrentUserId();
        
        Page<Interview> page = new Page<>(current, size);
        Page<Interview> resultPage = interviewService.pageCandidateInterviews(
                page, candidateId, status, keyword, startDate, endDate);
        
        return Result.success(resultPage);
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
    public Result<List<Interview>> getTodayInterviews(@RequestParam(value = "interviewerId", required = false) Long interviewerId) {
        List<Interview> interviews = interviewService.getTodayInterviews(interviewerId);
        return Result.success(interviews);
    }

    /**
     * 获取即将开始的面试
     */
    @GetMapping("/upcoming")
    public Result<List<Interview>> getUpcomingInterviews(@RequestParam(value = "userId", required = false) Long userId,
                                                         @RequestParam(value = "hours", defaultValue = "24") Integer hours) {
        List<Interview> interviews = interviewService.getUpcomingInterviews(userId, hours);
        return Result.success(interviews);
    }
//TODO 没有测试
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