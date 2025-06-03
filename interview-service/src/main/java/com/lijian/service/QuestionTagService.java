package com.lijian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.dto.QuestionCreateDTO;
import com.lijian.entity.Question;
import com.lijian.entity.QuestionTag;

import java.util.List;

/**
 * 题目标签服务接口
 */
public interface QuestionTagService extends IService<QuestionTag> {

    /**
     * 根据标签名称获取标签
     * @param tagName 标签名称
     * @return 标签对象
     */
    QuestionTag getByTagName(String tagName);

    /**
     * 批量获取或创建标签
     * @param tagNames 标签名称列表
     * @return 标签ID列表
     */
    List<Long> getOrCreateTags(List<String> tagNames);


} 