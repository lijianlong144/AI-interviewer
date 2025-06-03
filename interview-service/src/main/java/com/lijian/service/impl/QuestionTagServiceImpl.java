package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.QuestionTag;
import com.lijian.mapper.QuestionTagMapper;
import com.lijian.service.QuestionTagService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目标签服务实现类
 */
@Service
public class QuestionTagServiceImpl extends ServiceImpl<QuestionTagMapper, QuestionTag> implements QuestionTagService {

    @Override
    public QuestionTag getByTagName(String tagName) {
        LambdaQueryWrapper<QuestionTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionTag::getTagName, tagName);
        queryWrapper.eq(QuestionTag::getIsDeleted, 0);
        return getOne(queryWrapper);
    }

    @Override
    public List<Long> getOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> tagIds = new ArrayList<>();
        
        for (String tagName : tagNames) {
            // 查找是否已存在该标签
            QuestionTag existingTag = getByTagName(tagName);
            
            if (existingTag != null) {
                // 标签已存在，直接使用
                tagIds.add(existingTag.getId());
            } else {
                // 标签不存在，创建新标签
                QuestionTag newTag = new QuestionTag();
                newTag.setTagName(tagName);
                newTag.setIsDeleted(0);
                save(newTag);
                tagIds.add(newTag.getId());
            }
        }
        
        return tagIds;
    }
} 