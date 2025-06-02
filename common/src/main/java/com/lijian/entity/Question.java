package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题库实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_question")
public class Question extends BaseEntity {
    
    /**
     * 题目标题
     */
    private String title;
    
    /**
     * 题目详细内容
     */
    private String content;
//    @TableName
//    @TableField("category_id")
//    private Long categoryId;
    /**
     * 题型：TECHNICAL/BEHAVIORAL/SITUATIONAL
     */
    private String type;
    
    /**
     * 难度1-5
     */
    private Integer difficulty;
    
    /**
     * 适用职位
     */
    private String position;
    
    /**
     * 标签，逗号分隔
     */
    @TableField(exist = false)
    private String tags;
    
    /**
     * 参考答案
     */
    private String standardAnswer;
    
    /**
     * 关键词，逗号分隔
     */
    private String keywords;
    
    /**
     * 创建人ID
     */
    private Long creatorId;
    
    /**
     * 使用次数
     */
    private Integer useCount;
    
    /**
     * 状态：0-禁用 1-启用
     */
    private Integer status;
    private Integer isDeleted;
} 