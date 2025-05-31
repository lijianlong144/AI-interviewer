package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.InterviewRecord;
import com.lijian.entity.Question;
import com.lijian.mapper.InterviewRecordMapper;
import com.lijian.service.InterviewRecordService;
import com.lijian.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 面试记录服务实现类
 */
@Service
public class InterviewRecordServiceImpl extends ServiceImpl<InterviewRecordMapper, InterviewRecord> implements InterviewRecordService {
    
    @Autowired
    private QuestionService questionService;
    
    @Override
    public List<InterviewRecord> getByInterviewId(Long interviewId) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);
        queryWrapper.orderByAsc(InterviewRecord::getSequence);
        return list(queryWrapper);
    }
    
    @Override
    public boolean saveAndAnalyze(InterviewRecord record) {
        // 如果有题目ID，则获取题目信息
        if (record.getQuestionId() != null) {
            Question question = questionService.getById(record.getQuestionId());
            if (question != null) {
                // 设置问题内容
                record.setQuestionContent(question.getContent());
                record.setQuestionType("BANK");
                
                // 简单分析匹配关键词（实际项目中可能需要更复杂的AI分析）
                if (question.getKeywords() != null && record.getAnswerText() != null) {
                    String[] keywords = question.getKeywords().split(",");
                    StringBuilder matchedKeywords = new StringBuilder();
                    
                    for (String keyword : keywords) {
                        if (record.getAnswerText().contains(keyword)) {
                            if (matchedKeywords.length() > 0) {
                                matchedKeywords.append(",");
                            }
                            matchedKeywords.append(keyword);
                        }
                    }
                    
                    record.setKeywordsMatched(matchedKeywords.toString());
                    
                    // 简单评分（实际项目中需要更复杂的AI评分）
                    if (keywords.length > 0) {
                        int matchCount = matchedKeywords.length() > 0 ? matchedKeywords.toString().split(",").length : 0;
                        BigDecimal score = new BigDecimal(matchCount * 100.0 / keywords.length);
                        record.setAiScore(score);
                    }
                }
            }
        }
        
        return save(record);
    }
    
    @Override
    public InterviewRecord getByInterviewIdAndQuestionId(Long interviewId, Long questionId) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);
        queryWrapper.eq(InterviewRecord::getQuestionId, questionId);
        return getOne(queryWrapper);
    }
} 