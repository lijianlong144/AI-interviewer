package com.lijian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.InterviewReport;

import java.util.List;

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
     * 根据候选人ID获取报告列表
     * @param candidateId 候选人ID
     * @return 报告列表
     */
    List<InterviewReport> getByCandidateId(Long candidateId);

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

    /**
     * 批量生成报告
     * @param interviewIds 面试ID列表
     * @return 报告列表
     */
    List<InterviewReport> batchGenerateReports(List<Long> interviewIds);

    /**
     * 获取报告统计信息
     * @param interviewerId 面试官ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    Object getReportStatistics(Long interviewerId, String startDate, String endDate);

    /**
     * 根据推荐决策获取报告列表
     * @param recommendation 推荐决策
     * @return 报告列表
     */
    List<InterviewReport> getByRecommendation(String recommendation);

    /**
     * 根据评分范围获取报告列表
     * @param minScore 最小分数
     * @param maxScore 最大分数
     * @return 报告列表
     */
    List<InterviewReport> getByScoreRange(Double minScore, Double maxScore);

    /**
     * 导出面试报告
     * @param id 报告ID
     * @param format 导出格式
     * @return 导出文件路径
     */
    String exportReport(Long id, String format);

    /**
     * 重新生成报告
     * @param interviewId 面试ID
     * @param forceRegenerate 是否强制重新生成
     * @return 面试报告
     */
    InterviewReport regenerateReport(Long interviewId, Boolean forceRegenerate);

    /**
     * 获取面试报告模板
     * @return 报告模板
     */
    Object getReportTemplate();

    /**
     * 获取优秀报告列表
     * @param limit 数量限制
     * @return 优秀报告列表
     */
    List<InterviewReport> getExcellentReports(Integer limit);

    /**
     * 计算综合评分
     * @param interviewId 面试ID
     * @return 综合评分
     */
    Double calculateOverallScore(Long interviewId);

    /**
     * 生成评价建议
     * @param interviewId 面试ID
     * @return 评价建议
     */
    String generateSuggestions(Long interviewId);

    /**
     * 获取同职位平均分
     * @param position 职位名称
     * @return 平均分
     */
    Double getAverageScoreByPosition(String position);

    /**
     * 检查报告是否需要更新
     * @param interviewId 面试ID
     * @return 是否需要更新
     */
    boolean needsUpdate(Long interviewId);

    /**
     * 获取报告生成进度
     * @param interviewId 面试ID
     * @return 生成进度（0-100）
     */
    Integer getGenerationProgress(Long interviewId);
}