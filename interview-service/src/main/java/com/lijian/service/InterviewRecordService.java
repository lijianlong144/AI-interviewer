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

    /**
     * 批量保存面试记录并分析
     * @param records 面试记录列表
     * @return 是否成功
     */
    boolean batchSaveAndAnalyze(List<InterviewRecord> records);

    /**
     * 重新分析面试记录
     * @param recordId 记录ID
     * @return 分析后的记录
     */
    InterviewRecord reanalyzeRecord(Long recordId);

    /**
     * 获取面试记录统计信息
     * @param interviewId 面试ID
     * @return 统计信息
     */
    Object getRecordStatistics(Long interviewId);

    /**
     * 根据评分范围获取记录
     * @param interviewId 面试ID
     * @param minScore 最小分数
     * @param maxScore 最大分数
     * @return 记录列表
     */
    List<InterviewRecord> getByScoreRange(Long interviewId, Double minScore, Double maxScore);

    /**
     * 获取高分记录
     * @param interviewId 面试ID
     * @param threshold 分数阈值
     * @return 高分记录列表
     */
    List<InterviewRecord> getHighScoreRecords(Long interviewId, Double threshold);

    /**
     * 获取低分记录
     * @param interviewId 面试ID
     * @param threshold 分数阈值
     * @return 低分记录列表
     */
    List<InterviewRecord> getLowScoreRecords(Long interviewId, Double threshold);

    /**
     * 导出面试记录
     * @param interviewId 面试ID
     * @return 导出文件路径
     */
    String exportRecords(Long interviewId);

    /**
     * 根据问题类型获取记录
     * @param interviewId 面试ID
     * @param questionType 问题类型
     * @return 记录列表
     */
    List<InterviewRecord> getByQuestionType(Long interviewId, String questionType);

    /**
     * 计算平均评分
     * @param interviewId 面试ID
     * @return 平均评分
     */
    Double calculateAverageScore(Long interviewId);

    /**
     * 获取最高分记录
     * @param interviewId 面试ID
     * @return 最高分记录
     */
    InterviewRecord getHighestScoreRecord(Long interviewId);

    /**
     * 获取最低分记录
     * @param interviewId 面试ID
     * @return 最低分记录
     */
    InterviewRecord getLowestScoreRecord(Long interviewId);

    /**
     * 根据关键词匹配情况获取记录
     * @param interviewId 面试ID
     * @param hasKeywords 是否有关键词匹配
     * @return 记录列表
     */
    List<InterviewRecord> getByKeywordMatch(Long interviewId, Boolean hasKeywords);
}