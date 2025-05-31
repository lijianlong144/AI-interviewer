package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.Interview;
import com.lijian.entity.InterviewRecord;
import com.lijian.entity.InterviewReport;
import com.lijian.mapper.InterviewReportMapper;
import com.lijian.service.InterviewRecordService;
import com.lijian.service.InterviewReportService;
import com.lijian.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 面试报告服务实现类
 */
@Service
public class InterviewReportServiceImpl extends ServiceImpl<InterviewReportMapper, InterviewReport> implements InterviewReportService {
    
    @Autowired
    private InterviewService interviewService;
    
    @Autowired
    private InterviewRecordService interviewRecordService;
    
    @Override
    public InterviewReport getByInterviewId(Long interviewId) {
        LambdaQueryWrapper<InterviewReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewReport::getInterviewId, interviewId);
        return getOne(queryWrapper);
    }
    
    @Override
    @Transactional
    public InterviewReport generateReport(Long interviewId) {
        // 先检查是否已存在报告
        InterviewReport existingReport = getByInterviewId(interviewId);
        if (existingReport != null) {
            return existingReport;
        }
        
        // 获取面试信息
        Interview interview = interviewService.getById(interviewId);
        if (interview == null) {
            return null;
        }
        
        // 获取面试记录列表
        List<InterviewRecord> records = interviewRecordService.getByInterviewId(interviewId);
        if (records.isEmpty()) {
            return null;
        }
        
        // 创建新的面试报告
        InterviewReport report = new InterviewReport();
        report.setInterviewId(interviewId);
        
        // 计算各项评分
        calculateScores(report, records);
        
        // 分析优势和不足
        analyzeStrengthsAndWeaknesses(report, records);
        
        // 保存报告
        save(report);
        
        // 更新面试总分
        interview.setTotalScore(report.getOverallScore());
        interviewService.updateById(interview);
        
        return report;
    }
    
    @Override
    public boolean updateHrComment(Long id, String hrComment) {
        InterviewReport report = getById(id);
        if (report == null) {
            return false;
        }
        
        report.setHrComment(hrComment);
        return updateById(report);
    }
    
    @Override
    public boolean updateRecommendation(Long id, String recommendation) {
        InterviewReport report = getById(id);
        if (report == null) {
            return false;
        }
        
        report.setRecommendation(recommendation);
        return updateById(report);
    }
    
    /**
     * 计算各项评分
     */
    private void calculateScores(InterviewReport report, List<InterviewRecord> records) {
        // 获取所有有评分的记录
        List<InterviewRecord> scoredRecords = records.stream()
                .filter(r -> r.getAiScore() != null)
                .collect(Collectors.toList());
        
        if (!scoredRecords.isEmpty()) {
            // 计算总体评分（所有记录的平均分）
            BigDecimal totalScore = scoredRecords.stream()
                    .map(InterviewRecord::getAiScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(scoredRecords.size()), 2, RoundingMode.HALF_UP);
            
            report.setOverallScore(totalScore);
            
            // 简单起见，这里将总体评分作为各项能力的评分
            // 实际项目中可能需要更复杂的评分逻辑
            report.setTechnicalScore(totalScore);
            report.setCommunicationScore(totalScore);
            report.setLogicScore(totalScore);
            report.setProfessionalScore(totalScore);
        }
    }
    
    /**
     * 分析优势和不足
     */
    private void analyzeStrengthsAndWeaknesses(InterviewReport report, List<InterviewRecord> records) {
        // 简单示例，实际项目中可能需要更复杂的AI分析
        StringBuilder strengths = new StringBuilder();
        StringBuilder weaknesses = new StringBuilder();
        StringBuilder suggestions = new StringBuilder();
        StringBuilder aiSummary = new StringBuilder();
        
        // 分析高分和低分记录
        for (InterviewRecord record : records) {
            if (record.getAiScore() != null) {
                if (record.getAiScore().compareTo(new BigDecimal(80)) >= 0) {
                    strengths.append("- 在问题\"").append(record.getQuestionContent()).append("\"中表现优秀\n");
                } else if (record.getAiScore().compareTo(new BigDecimal(60)) < 0) {
                    weaknesses.append("- 在问题\"").append(record.getQuestionContent()).append("\"中表现不足\n");
                    suggestions.append("- 建议加强对\"").append(record.getQuestionContent()).append("\"相关知识的学习\n");
                }
            }
        }
        
        // 生成AI总结
        if (report.getOverallScore() != null) {
            if (report.getOverallScore().compareTo(new BigDecimal(80)) >= 0) {
                aiSummary.append("候选人整体表现优秀，建议考虑录用。");
            } else if (report.getOverallScore().compareTo(new BigDecimal(60)) >= 0) {
                aiSummary.append("候选人整体表现一般，可以考虑进一步评估。");
            } else {
                aiSummary.append("候选人整体表现较差，建议不予录用。");
            }
        }
        
        report.setStrengths(strengths.toString());
        report.setWeaknesses(weaknesses.toString());
        report.setSuggestions(suggestions.toString());
        report.setAiSummary(aiSummary.toString());
    }
} 