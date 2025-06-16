package com.lijian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.InterviewQuestionEntity;
import com.lijian.mapper.InterviewQuestionMapper;
import com.lijian.service.InterviewQuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * 面试问题服务实现类
 */
@Service
public class InterviewQuestionServiceImpl extends ServiceImpl<InterviewQuestionMapper, InterviewQuestionEntity> implements InterviewQuestionService {
    private static final Logger log = LoggerFactory.getLogger(InterviewQuestionServiceImpl.class);

    @Override
    public void saveQuestion(InterviewQuestionEntity question) {
        try {
            // 确保必要字段不为空
            if (question.getInterviewId() == null) {
                throw new IllegalArgumentException("面试ID不能为空");
            }
            
            if (question.getQuestionContent() == null || question.getQuestionContent().isEmpty()) {
                throw new IllegalArgumentException("问题内容不能为空");
            }
            
            // 保存问题
            boolean saved = this.save(question);
            if (!saved) {
                throw new RuntimeException("保存面试问题失败");
            }
            log.info("成功保存面试问题: interviewId={}, questionId={}", question.getInterviewId(), question.getInterviewQuestionId());
        } catch (DataIntegrityViolationException e) {
            log.error("保存面试问题违反数据完整性约束: {}", e.getMessage());
            if (e.getMessage().contains("foreign key constraint fails") && 
                e.getMessage().contains("fk_interview_question_interview_id")) {
                throw new RuntimeException("面试ID不存在，无法保存问题", e);
            }
            throw e;
        } catch (Exception e) {
            log.error("保存面试问题时发生错误: {}", e.getMessage());
            throw e;
        }
    }
}