package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.Question;
import com.lijian.exception.BusinessException;
import com.lijian.mapper.QuestionMapper;
import com.lijian.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
        if (StringUtils.hasText(type)) {
            queryWrapper.eq(Question::getType, type);
        }

        if (StringUtils.hasText(difficulty)) {
            queryWrapper.eq(Question::getDifficulty, Integer.valueOf(difficulty));
        }

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Question::getContent, keyword)
                    .or()
                    .like(Question::getTitle, keyword)
                    .or()
                    .like(Question::getKeywords, keyword);
        }

        // 只查询启用状态的题目
        queryWrapper.eq(Question::getStatus, 1);

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Question::getCreateTime);

        return page(page, queryWrapper);
    }

    @Override
    public List<Question> getRandomQuestions(Integer count, String type, String difficulty) {
        // 构建查询条件
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(type)) {
            queryWrapper.eq(Question::getType, type);
        }

        if (StringUtils.hasText(difficulty)) {
            queryWrapper.eq(Question::getDifficulty, Integer.valueOf(difficulty));
        }

        // 只查询启用状态的题目
        queryWrapper.eq(Question::getStatus, 1);

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

    @Override
    public List<Question> getByType(String type) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getType, type);
        queryWrapper.eq(Question::getStatus, 1);
        queryWrapper.orderByAsc(Question::getDifficulty);
        return list(queryWrapper);
    }

    @Override
    public List<Question> getByDifficulty(Integer difficulty) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getDifficulty, difficulty);
        queryWrapper.eq(Question::getStatus, 1);
        queryWrapper.orderByDesc(Question::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public List<Question> getRecommendQuestions(String position, Integer count) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();

        // 优先查找职位匹配的题目
        queryWrapper.like(Question::getPosition, position)
                .or()
                .like(Question::getKeywords, position)
                .or()
                .like(Question::getTags, position);

        queryWrapper.eq(Question::getStatus, 1);
        queryWrapper.orderByDesc(Question::getUseCount);
        queryWrapper.last("LIMIT " + count);

        List<Question> positionQuestions = list(queryWrapper);

        // 如果职位相关题目不足，补充通用题目
        if (positionQuestions.size() < count) {
            int remaining = count - positionQuestions.size();
            LambdaQueryWrapper<Question> generalWrapper = new LambdaQueryWrapper<>();
            generalWrapper.eq(Question::getStatus, 1);
            generalWrapper.orderByDesc(Question::getUseCount);
            generalWrapper.last("LIMIT " + remaining);

            List<Question> generalQuestions = list(generalWrapper);

            // 合并结果，去重
            List<Long> existIds = positionQuestions.stream()
                    .map(Question::getId)
                    .collect(Collectors.toList());

            generalQuestions.stream()
                    .filter(q -> !existIds.contains(q.getId()))
                    .forEach(positionQuestions::add);
        }

        return positionQuestions;
    }

    @Override
    public boolean updateQuestionStatus(Long questionId, Integer status) {
        Question question = getById(questionId);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        question.setStatus(status);
        return updateById(question);
    }

    @Override
    public boolean increaseUseCount(Long questionId) {
        Question question = getById(questionId);
        if (question == null) {
            return false;
        }

        question.setUseCount(question.getUseCount() == null ? 1 : question.getUseCount() + 1);
        return updateById(question);
    }

    @Override
    public List<Question> searchByKeyword(String keyword) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Question::getTitle, keyword)
                .or()
                .like(Question::getContent, keyword)
                .or()
                .like(Question::getKeywords, keyword);
        queryWrapper.eq(Question::getStatus, 1);
        queryWrapper.orderByDesc(Question::getUseCount);
        return list(queryWrapper);
    }

    @Override
    public List<Question> getByTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();

        // 构建标签查询条件
        for (int i = 0; i < tags.size(); i++) {
            if (i == 0) {
                queryWrapper.like(Question::getTags, tags.get(i));
            } else {
                queryWrapper.or().like(Question::getTags, tags.get(i));
            }
        }

        queryWrapper.eq(Question::getStatus, 1);
        queryWrapper.orderByDesc(Question::getUseCount);
        return list(queryWrapper);
    }

    @Override
    public List<Question> getPopularQuestions(Integer count) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getStatus, 1);
        queryWrapper.orderByDesc(Question::getUseCount);
        queryWrapper.last("LIMIT " + count);
        return list(queryWrapper);
    }

    @Override
    public List<Question> getByCreatorId(Long creatorId) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getCreatorId, creatorId);
        queryWrapper.orderByDesc(Question::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    public boolean batchUpdateStatus(List<Long> questionIds, Integer status) {
        if (questionIds == null || questionIds.isEmpty()) {
            return false;
        }

        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Question::getId, questionIds);
        List<Question> questions = list(queryWrapper);

        questions.forEach(question -> question.setStatus(status));
        return updateBatchById(questions);
    }
}