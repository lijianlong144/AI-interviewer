package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 面试回答实体类
 */
@Data
@TableName("t_interview_answer")
public class InterviewAnswerEntity {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 面试ID
     */
    private Long interviewId;

    /**
     * 问题ID
     */
    private Long interviewQuestionId;

    /**
     * 回答内容
     */
    private String answerContent;

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
     * 回答时间
     */
    private Date answerTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除：0-否 1-是
     */
    private Integer isDeleted;
} 