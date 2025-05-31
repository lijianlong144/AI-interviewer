package com.lijian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.lijian.result.Result;
import com.lijian.entity.Interview;
import com.lijian.result.Result;
import com.lijian.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
        boolean result = interviewService.save(interview);
        return Result.success(interview);
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
} 