package com.lijian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.Question;

import java.util.List;

/**
 * 面试题服务接口
 */
public interface QuestionService extends IService<Question> {
    
    /**
     * 分页查询面试题
     * @param current 当前页码
     * @param size 每页大小
     * @param type 题目类型
     * @param difficulty 难度
     * @param keyword 关键词
     * @return 分页结果
     */
    Page<Question> pageQuestions(Integer current, Integer size, String type, String difficulty, String keyword);
    
    /**
     * 获取随机面试题
     * @param count 数量
     * @param type 题目类型
     * @param difficulty 难度
     * @return 随机面试题列表
     */
    List<Question> getRandomQuestions(Integer count, String type, String difficulty);
} 