package com.lijian.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.List;

/**
 * 创建题目DTO
 *
 * @author lijian
 * @since 1.0.0
 */
@Data
public class QuestionCreateDTO {

    /**
     * 题目标题
     */
    @NotBlank(message = "题目标题不能为空")
    private String title;

    /**
     * 题目详细内容
     */
    @NotBlank(message = "题目内容不能为空")
    private String content;

    /**
     * 题型
     */
    @NotBlank(message = "题型不能为空")
    private String type;

    /**
     * 难度1-5
     */
    @NotNull(message = "难度等级不能为空")
    @Min(value = 1, message = "难度等级最小为1")
    @Max(value = 5, message = "难度等级最大为5")
    private Integer difficulty;

    /**
     * 适用职位
     */
    private String position;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 参考答案
     */
    private String referenceAnswer;

    /**
     * 关键词列表
     */
    private List<String> keywords;

    /**
     * 创建人ID
     */
    @NotNull(message = "创建人ID不能为空")
    private Long creatorId;
}
