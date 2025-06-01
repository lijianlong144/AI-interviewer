package com.lijian.controller;

import com.lijian.result.Result;
import com.lijian.entity.InterviewRecord;
import com.lijian.service.InterviewRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 面试记录控制器
 *
 * @author lijian
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/interview-record")
@Validated
public class InterviewRecordController {

    @Autowired
    private InterviewRecordService recordService;

    /**
     * 保存面试记录
     */
    @PostMapping
    public Result<InterviewRecord> saveRecord(@Valid @RequestBody InterviewRecord record) {
        boolean result = recordService.saveAndAnalyze(record);
        return Result.success(record);
    }

    /**
     * 获取面试记录详情
     */
    @GetMapping("/{id}")
    public Result<InterviewRecord> getRecordDetail(@PathVariable("id") Long id) {
        InterviewRecord record = recordService.getById(id);
        return Result.success(record);
    }

    /**
     * 根据面试ID获取所有记录
     */
    @GetMapping("/interview/{interviewId}")
    public Result<List<InterviewRecord>> getByInterviewId(@PathVariable("interviewId") Long interviewId) {
        List<InterviewRecord> records = recordService.getByInterviewId(interviewId);
        return Result.success(records);
    }

    /**
     * 根据面试ID和题目ID获取记录
     */
    @GetMapping("/interview/{interviewId}/question/{questionId}")
    public Result<InterviewRecord> getByInterviewIdAndQuestionId(@PathVariable("interviewId") Long interviewId,
                                                                 @PathVariable("questionId") Long questionId) {
        InterviewRecord record = recordService.getByInterviewIdAndQuestionId(interviewId, questionId);
        return Result.success(record);
    }

    /**
     * 更新面试记录
     */
    @PutMapping
    public Result<Boolean> updateRecord(@Valid @RequestBody InterviewRecord record) {
        boolean result = recordService.updateById(record);
        return Result.success(result);
    }

    /**
     * 删除面试记录
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteRecord(@PathVariable("id") Long id) {
        boolean result = recordService.removeById(id);
        return Result.success(result);
    }

    /**
     * 批量保存面试记录
     */
    @PostMapping("/batch")
    public Result<Boolean> batchSaveRecords(@RequestBody List<InterviewRecord> records) {
        boolean result = recordService.batchSaveAndAnalyze(records);
        return Result.success(result);
    }

    /**
     * 重新分析面试记录
     */
    @PostMapping("/reanalyze/{id}")
    public Result<InterviewRecord> reanalyzeRecord(@PathVariable("id") Long id) {
        InterviewRecord record = recordService.reanalyzeRecord(id);
        return Result.success(record);
    }

    /**
     * 获取面试记录统计
     */
    @GetMapping("/statistics/{interviewId}")
    public Result<Object> getRecordStatistics(@PathVariable("interviewId") Long interviewId) {
        Object statistics = recordService.getRecordStatistics(interviewId);
        return Result.success(statistics);
    }

    /**
     * 根据评分范围获取记录
     */
    @GetMapping("/score-range")
    public Result<List<InterviewRecord>> getByScoreRange(@RequestParam @NotNull Long interviewId,
                                                         @RequestParam Double minScore,
                                                         @RequestParam Double maxScore) {
        List<InterviewRecord> records = recordService.getByScoreRange(interviewId, minScore, maxScore);
        return Result.success(records);
    }

    /**
     * 获取高分记录
     */
    @GetMapping("/high-score/{interviewId}")
    public Result<List<InterviewRecord>> getHighScoreRecords(@PathVariable("interviewId") Long interviewId,
                                                             @RequestParam(defaultValue = "80") Double threshold) {
        List<InterviewRecord> records = recordService.getHighScoreRecords(interviewId, threshold);
        return Result.success(records);
    }

    /**
     * 获取低分记录
     */
    @GetMapping("/low-score/{interviewId}")
    public Result<List<InterviewRecord>> getLowScoreRecords(@PathVariable("interviewId") Long interviewId,
                                                            @RequestParam(defaultValue = "60") Double threshold) {
        List<InterviewRecord> records = recordService.getLowScoreRecords(interviewId, threshold);
        return Result.success(records);
    }

    /**
     * 导出面试记录
     */
    @GetMapping("/export/{interviewId}")
    public Result<String> exportRecords(@PathVariable("interviewId") Long interviewId) {
        String exportPath = recordService.exportRecords(interviewId);
        return Result.success(exportPath);
    }
}