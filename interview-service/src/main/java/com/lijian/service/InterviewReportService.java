package com.lijian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.InterviewReport;

/**
 * 面试报告服务接口
 */
public interface InterviewReportService extends IService<InterviewReport> {
    
    /**
     * 根据面试ID获取面试报告
     * @param interviewId 面试ID
     * @return 面试报告
     */
    InterviewReport getByInterviewId(Long interviewId);
    
    /**
     * 生成面试报告
     * @param interviewId 面试ID
     * @return 面试报告
     */
    InterviewReport generateReport(Long interviewId);
    
    /**
     * 更新HR评语
     * @param id 报告ID
     * @param hrComment HR评语
     * @return 是否成功
     */
    boolean updateHrComment(Long id, String hrComment);
    
    /**
     * 更新推荐决策
     * @param id 报告ID
     * @param recommendation 推荐决策
     * @return 是否成功
     */
    boolean updateRecommendation(Long id, String recommendation);
} 