package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

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
     * 逻辑能力分
     */
    private BigDecimal logicScore;
    
    /**
     * 专业能力分
     */
    private BigDecimal professionalScore;
    
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
} 