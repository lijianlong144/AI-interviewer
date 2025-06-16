package com.lijian.entity;



import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_interview_question")
public class InterviewQuestionEntity {

    @TableId
    private Long id;

    private Long interviewId;

    private Long interviewQuestionId; // 来自题库的ID，可为null，原来是questionId

    private String questionContent;

    private String questionType; // TECHNICAL / BEHAVIORAL / SITUATIONAL

    private Integer aiGenerated; // 1 是AI生成

    private Integer sequence;

    private Date createTime;

    private Date updateTime;

    private Integer isDeleted;
}

