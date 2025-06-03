package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.QuestionTagRelation;
import com.lijian.mapper.QuestionTagRelationMapper;
import com.lijian.service.QuestionTagRelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目标签关联服务实现类
 */
@Service
public class QuestionTagRelationServiceImpl extends ServiceImpl<QuestionTagRelationMapper, QuestionTagRelation> implements QuestionTagRelationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createRelations(Long questionId, List<Long> tagIds) {
        if (questionId == null || tagIds == null || tagIds.isEmpty()) {
            return false;
        }
        
        // 先删除该题目的所有关联关系
        deleteByQuestionId(questionId);
        
        // 创建新的关联关系
        List<QuestionTagRelation> relations = new ArrayList<>();
        for (Long tagId : tagIds) {
            QuestionTagRelation relation = new QuestionTagRelation();
            relation.setQuestionId(questionId);
            relation.setTagId(tagId);
            relation.setIsDeleted(0);
            relations.add(relation);
        }
        
        return saveBatch(relations);
    }

    @Override
    public boolean deleteByQuestionId(Long questionId) {
        if (questionId == null) {
            return false;
        }
        
        LambdaQueryWrapper<QuestionTagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionTagRelation::getQuestionId, questionId);
        return remove(queryWrapper);
    }
} 