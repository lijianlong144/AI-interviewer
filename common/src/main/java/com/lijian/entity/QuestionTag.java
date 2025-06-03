package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目标签实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_question_tag")
public class QuestionTag extends BaseEntity {
    
    /**
     * 标签名称
     */
    private String tagName;
    
    /**
     * 是否删除：0-否 1-是
     */
    private Integer isDeleted;
} 