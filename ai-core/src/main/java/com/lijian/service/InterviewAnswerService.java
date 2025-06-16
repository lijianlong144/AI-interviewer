package com.lijian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.InterviewAnswerEntity;

/**
 * 面试回答服务接口
 */
public interface InterviewAnswerService extends IService<InterviewAnswerEntity> {
    
    /**
     * 保存面试回答
     * @param answer 面试回答实体
     */
    void saveAnswer(InterviewAnswerEntity answer);
} 