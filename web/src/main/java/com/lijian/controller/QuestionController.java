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
    public Result<Page<Question>> pageQuestions(@RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(required = false) String difficulty) {
        Page<Question> page = new Page<>(current, size);
        // 这里需要在QuestionService中实现pageQuestions方法
        // Page<Question> resultPage = questionService.pageQuestions(page, category, difficulty);
        Page<Question> resultPage = questionService.page(page);
        return Result.success(resultPage);
    }
    
    /**
     * 根据分类获取问题列表
     */
    @GetMapping("/category/{category}")
    public Result<List<Question>> getByCategory(@PathVariable("category") String category) {
        // 这里需要在QuestionService中实现getByCategory方法
        // List<Question> questions = questionService.getByCategory(category);
        return Result.success(null);
    }
    
    /**
     * 根据难度获取问题列表
     */
    @GetMapping("/difficulty/{difficulty}")
    public Result<List<Question>> getByDifficulty(@PathVariable("difficulty") String difficulty) {
        // 这里需要在QuestionService中实现getByDifficulty方法
        // List<Question> questions = questionService.getByDifficulty(difficulty);
        return Result.success(null);
    }
} 