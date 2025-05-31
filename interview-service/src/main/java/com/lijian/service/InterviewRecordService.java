package com.lijian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.InterviewRecord;

import java.util.List;

/**
 * 面试记录服务接口
 */
public interface InterviewRecordService extends IService<InterviewRecord> {
    
    /**
     * 根据面试ID查询面试记录列表
     * @param interviewId 面试ID
     * @return 面试记录列表
     */
    List<InterviewRecord> getByInterviewId(Long interviewId);
    
    /**
     * 保存面试记录并分析
     * @param record 面试记录
     * @return 是否成功
     */
    boolean saveAndAnalyze(InterviewRecord record);
    
    /**
     * 根据题目ID和面试ID查询面试记录
     * @param interviewId 面试ID
     * @param questionId 题目ID
     * @return 面试记录
     */
    InterviewRecord getByInterviewIdAndQuestionId(Long interviewId, Long questionId);
} 