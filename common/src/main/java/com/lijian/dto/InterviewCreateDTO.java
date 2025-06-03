package com.lijian.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 创建面试DTO
 *
 * @author lijian
 * @since 1.0.0
 */
@Data
public class InterviewCreateDTO {

    /**
     * 面试标题
     */
//    @NotBlank(message = "面试标题不能为空")
//    private String title;

    /**
     * 应聘职位
     */
    @NotBlank(message = "应聘职位不能为空")
    private String position;

    /**
     * 面试描述
     */
    private String description;

    /**
     * 候选人ID
     */
    @NotNull(message = "候选人ID不能为空")
    private Long candidateId;

    /**
     * 创建人ID（面试官ID）
     */
    @NotNull(message = "创建人ID不能为空")
    private Long creatorId;

    /**
     * 预约时间
     */
    private LocalDateTime scheduledTime;
}