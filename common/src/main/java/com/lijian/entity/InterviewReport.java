package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试报告实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_interview_report")
public class InterviewReport extends BaseEntity {
    
    /**
     * 面试ID
     */
    private Long interviewId;
    
    /**
     * 候选人ID
     */
    private Long candidateId;
    
    /**
     * 面试官ID
     */
    private Long interviewerId;
    
    /**
     * 应聘职位
     */
    private String position;
    
    /**
     * 面试时间
     */
    private LocalDateTime interviewTime;
    
    /**
     * 面试时长(分钟)
     */
    private Integer duration;
    
    /**
     * 总体评分
     */
    private BigDecimal overallScore;
    
    /**
     * 技术能力分
     */
    private BigDecimal technicalScore;
    
    /**
     * 沟通能力分
     */
    private BigDecimal communicationScore;
    
    /**
     * 解决问题能力分
     */
    private BigDecimal problemSolvingScore;
    
    /**
     * 文化匹配度分
     */
    private BigDecimal culturalFitScore;
    
    /**
     * 逻辑能力分
     */
    private BigDecimal logicScore;
    
    /**
     * 专业能力分
     */
    private BigDecimal professionalScore;
    
    /**
     * 总体评价
     */
    private String overallEvaluation;
    
    /**
     * 优势分析
     */
    private String strengths;
    
    /**
     * 不足分析
     */
    private String weaknesses;
    
    /**
     * 改进建议
     */
    private String suggestions;
    
    /**
     * AI综合评价
     */
    private String aiSummary;
    
    /**
     * HR评语
     */
    private String hrComment;
    
    /**
     * 推荐决策：STRONGLY_RECOMMEND/RECOMMEND/NEUTRAL/NOT_RECOMMEND
     */
    private String recommendation;
    
    /**
     * 状态：DRAFT/PUBLISHED/ARCHIVED
     */
    private String status;
} 