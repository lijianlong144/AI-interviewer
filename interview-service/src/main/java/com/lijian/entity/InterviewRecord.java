package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lijian.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 面试记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_interview_record")
public class InterviewRecord extends BaseEntity {
    
    /**
     * 面试ID
     */
    private Long interviewId;
    
    /**
     * 题目ID（如果来自题库）
     */
    private Long questionId;
    
    /**
     * 实际问题内容
     */
    private String questionContent;
    
    /**
     * 问题来源：AI/BANK（题库）
     */
    private String questionType;
    
    /**
     * 回答文本
     */
    private String answerText;
    
    /**
     * 回答音频URL
     */
    private String answerAudioUrl;
    
    /**
     * 思考时长(秒)
     */
    private Integer thinkDuration;
    
    /**
     * 回答时长(秒)
     */
    private Integer answerDuration;
    
    /**
     * AI评分(0-100)
     */
    private BigDecimal aiScore;
    
    /**
     * AI分析
     */
    private String aiAnalysis;
    
    /**
     * 匹配到的关键词
     */
    private String keywordsMatched;
    
    /**
     * 问题顺序
     */
    private Integer sequence;
} 