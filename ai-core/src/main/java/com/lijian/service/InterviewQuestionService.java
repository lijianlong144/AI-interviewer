package com.lijian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.InterviewQuestionEntity;

/**
 * 面试问题服务接口
 */
public interface InterviewQuestionService extends IService<InterviewQuestionEntity> {
    
    /**
     * 保存面试问题
     * @param question 面试问题实体
     */
    void saveQuestion(InterviewQuestionEntity question);
} 