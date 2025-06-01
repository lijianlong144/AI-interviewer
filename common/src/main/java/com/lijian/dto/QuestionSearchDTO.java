package com.lijian.dto;

import lombok.Data;

import java.util.List;
/**
 * 题目搜索DTO
 *
 * @author lijian
 * @since 1.0.0
 */
@Data
public class QuestionSearchDTO {

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 题目类型
     */
    private String type;

    /**
     * 难度等级
     */
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
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 页面大小
     */
    private Integer size = 10;
}