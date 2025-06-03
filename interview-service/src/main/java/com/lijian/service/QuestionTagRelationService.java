package com.lijian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.QuestionTagRelation;

import java.util.List;

/**
 * 题目标签关联服务接口
 */
public interface QuestionTagRelationService extends IService<QuestionTagRelation> {

    /**
     * 创建题目和标签的关联关系
     * @param questionId 题目ID
     * @param tagIds 标签ID列表
     * @return 是否成功
     */
    boolean createRelations(Long questionId, List<Long> tagIds);
    
    /**
     * 根据题目ID删除关联关系
     * @param questionId 题目ID
     * @return 是否成功
     */
    boolean deleteByQuestionId(Long questionId);
} 