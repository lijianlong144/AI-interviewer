package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_interview")
public class Interview extends BaseEntity {
    
    /**
     * 面试编号
     */
    private String interviewNo;
    
    /**
     * 候选人ID
     */
    private Long candidateId;
    
    /**
     * 候选人姓名
     */
    private String candidateName;
    
    /**
     * 应聘职位
     */
    private String position;
    
    /**
     * 面试官ID
     */
    private Long interviewerId;
    
    /**
     * 预约时间
     */
    private LocalDateTime scheduledTime;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 面试时长(分钟)
     */
    private Integer duration;
    
    /**
     * 状态：PENDING/ONGOING/COMPLETED/CANCELLED
     */
    private String status;
    
    /**
     * 面试房间号
     */
    private String roomCode;
    
    /**
     * 总分
     */
    private BigDecimal totalScore;
} 