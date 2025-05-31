package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.Question;
import com.lijian.mapper.QuestionMapper;
import com.lijian.service.QuestionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 面试题服务实现类
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    
    @Override
    public Page<Question> pageQuestions(Integer current, Integer size, String type, String difficulty, String keyword) {
        Page<Question> page = new Page<>(current, size);
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(Question::getType, type);
        }
        
        if (difficulty != null && !difficulty.isEmpty()) {
            queryWrapper.eq(Question::getDifficulty, difficulty);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(Question::getContent, keyword)
                    .or()
                    .like(Question::getTitle, keyword);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Question::getCreateTime);
        
        return page(page, queryWrapper);
    }
    
    @Override
    public List<Question> getRandomQuestions(Integer count, String type, String difficulty) {
        // 构建查询条件
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(Question::getType, type);
        }
        
        if (difficulty != null && !difficulty.isEmpty()) {
            queryWrapper.eq(Question::getDifficulty, difficulty);
        }
        
        // 查询符合条件的所有题目
        List<Question> allQuestions = list(queryWrapper);
        
        // 如果题目数量不足，直接返回所有题目
        if (allQuestions.size() <= count) {
            return allQuestions;
        }
        
        // 随机选择指定数量的题目
        List<Question> randomQuestions = new ArrayList<>();
        Random random = new Random();
        
        // 使用Fisher-Yates洗牌算法
        for (int i = 0; i < count; i++) {
            int randomIndex = random.nextInt(allQuestions.size() - i) + i;
            // 交换元素
            Question temp = allQuestions.get(i);
            allQuestions.set(i, allQuestions.get(randomIndex));
            allQuestions.set(randomIndex, temp);
            
            // 添加到结果集
            randomQuestions.add(allQuestions.get(i));
        }
        
        return randomQuestions;
    }
} 