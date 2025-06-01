package com.lijian.dto;

import jakarta.validation.constraints.NotBlank;


import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * 创建面试记录DTO
 *
 * @author lijian
 * @since 1.0.0
 */
@Data
public class InterviewRecordCreateDTO {

    /**
     * 面试ID
     */
    @NotNull(message = "面试ID不能为空")
    private Long interviewId;

    /**
     * 题目ID（如果来自题库）
     */
    private Long questionId;

    /**
     * 实际问题内容
     */
    @NotBlank(message = "问题内容不能为空")
    private String questionContent;

    /**
     * 问题来源：AI/BANK（题库）
     */
    private String questionType;

    /**
     * 回答文本
     */
    @NotBlank(message = "回答内容不能为空")
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
     * 问题顺序
     */
    private Integer sequence;
}