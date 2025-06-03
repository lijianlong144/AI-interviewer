package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 题目标签关联实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_question_tag_relation")
public class QuestionTagRelation extends BaseEntity {
    
    /**
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 标签ID
     */
    private Long tagId;
    
    /**
     * 是否删除：0-否 1-是
     */
    private Integer isDeleted;
} 