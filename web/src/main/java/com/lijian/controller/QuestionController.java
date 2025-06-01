package com.lijian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lijian.result.Result;
import com.lijian.entity.Question;
import com.lijian.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 问题管理控制器
 *
 * @author lijian
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/question")
@Validated
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * 创建问题
     */
    @PostMapping
    public Result<Question> createQuestion(@Valid @RequestBody Question question) {
        boolean result = questionService.save(question);
        return Result.success(question);
    }

    /**
     * 获取问题详情
     */
    @GetMapping("/{id}")
    public Result<Question> getQuestionDetail(@PathVariable("id") Long id) {
        Question question = questionService.getById(id);
        return Result.success(question);
    }

    /**
     * 更新问题
     */
    @PutMapping
    public Result<Boolean> updateQuestion(@Valid @RequestBody Question question) {
        boolean result = questionService.updateById(question);
        return Result.success(result);
    }

    /**
     * 删除问题
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteQuestion(@PathVariable("id") Long id) {
        boolean result = questionService.removeById(id);
        return Result.success(result);
    }

    /**
     * 分页查询问题列表
     */
    @GetMapping("/page")
    public Result<Page<Question>> pageQuestions(@RequestParam(defaultValue = "1") Integer current,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) String type,
                                                @RequestParam(required = false) String difficulty,
                                                @RequestParam(required = false) String keyword) {
        Page<Question> resultPage = questionService.pageQuestions(current, size, type, difficulty, keyword);
        return Result.success(resultPage);
    }

    /**
     * 根据类型获取问题列表
     */
    @GetMapping("/type/{type}")
    public Result<List<Question>> getByType(@PathVariable("type") String type) {
        List<Question> questions = questionService.getByType(type);
        return Result.success(questions);
    }

    /**
     * 根据难度获取问题列表
     */
    @GetMapping("/difficulty/{difficulty}")
    public Result<List<Question>> getByDifficulty(@PathVariable("difficulty") Integer difficulty) {
        List<Question> questions = questionService.getByDifficulty(difficulty);
        return Result.success(questions);
    }

    /**
     * 获取随机问题
     */
    @GetMapping("/random")
    public Result<List<Question>> getRandomQuestions(@RequestParam(defaultValue = "5") Integer count,
                                                     @RequestParam(required = false) String type,
                                                     @RequestParam(required = false) String difficulty) {
        List<Question> questions = questionService.getRandomQuestions(count, type, difficulty);
        return Result.success(questions);
    }

    /**
     * 根据职位获取推荐题目
     */
    @GetMapping("/recommend")
    public Result<List<Question>> getRecommendQuestions(@RequestParam String position,
                                                        @RequestParam(defaultValue = "10") Integer count) {
        List<Question> questions = questionService.getRecommendQuestions(position, count);
        return Result.success(questions);
    }

    /**
     * 启用/禁用问题
     */
    @PostMapping("/status")
    public Result<Boolean> updateQuestionStatus(@RequestParam @NotNull(message = "问题ID不能为空") Long questionId,
                                                @RequestParam @NotNull(message = "状态不能为空") Integer status) {
        boolean result = questionService.updateQuestionStatus(questionId, status);
        return Result.success(result);
    }

    /**
     * 批量删除问题
     */
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteQuestions(@RequestBody List<Long> questionIds) {
        boolean result = questionService.removeByIds(questionIds);
        return Result.success(result);
    }

    /**
     * 增加问题使用次数
     */
    @PostMapping("/use/{id}")
    public Result<Boolean> increaseUseCount(@PathVariable("id") Long id) {
        boolean result = questionService.increaseUseCount(id);
        return Result.success(result);
    }
}