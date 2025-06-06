package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位申请实体类
 */
@Data
// @EqualsAndHashCode(callSuper = true)
@TableName("t_application")
public class Application {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请编号
     */
    private String applicationNo;

    /**
     * 职位ID
     */
    private Long positionId;

    /**
     * 候选人ID
     */
    private Long candidateId;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 状态：PENDING/REVIEWING/PASSED/REJECTED/INTERVIEW_SCHEDULED
     */
    private String status;

    /**
     * 来源
     */
    private String source;

    /**
     * HR备注
     */
    private String hrComment;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 简历评分
     */
    private Integer score;

    /**
     * HR是否已读：0-否 1-是
     */
    private Integer isRead;

    /**
     * HR查看时间
     */
    private LocalDateTime readTime;

    /**
     * 面试ID
     */
    private Long interviewId;

    /**
     * 申请时间
     */
    private LocalDateTime applyTime;

    /**
     * 筛选时间
     */
    private LocalDateTime screenTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}