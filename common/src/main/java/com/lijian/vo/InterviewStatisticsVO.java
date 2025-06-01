package com.lijian.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 面试统计信息VO
 *
 * @author lijian
 * @since 1.0.0
 */
@Data
public class InterviewStatisticsVO {

    /**
     * 总面试数
     */
    private Long total;

    /**
     * 待进行面试数
     */
    private Long pending;

    /**
     * 进行中面试数
     */
    private Long ongoing;

    /**
     * 已完成面试数
     */
    private Long completed;

    /**
     * 已取消面试数
     */
    private Long cancelled;

    /**
     * 平均面试时长（分钟）
     */
    private Double averageDuration;

    /**
     * 平均评分
     */
    private BigDecimal averageScore;

    /**
     * 最高评分
     */
    private BigDecimal maxScore;

    /**
     * 最低评分
     */
    private BigDecimal minScore;

    /**
     * 按职位统计
     */
    private Map<String, Long> positionStats;

    /**
     * 按状态统计
     */
    private Map<String, Long> statusStats;

    /**
     * 按推荐决策统计
     */
    private Map<String, Long> recommendationStats;
}