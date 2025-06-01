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

    /**
     * 根据类型获取问题列表
     * @param type 题目类型
     * @return 问题列表
     */
    List<Question> getByType(String type);

    /**
     * 根据难度获取问题列表
     * @param difficulty 难度等级
     * @return 问题列表
     */
    List<Question> getByDifficulty(Integer difficulty);

    /**
     * 根据职位获取推荐题目
     * @param position 职位名称
     * @param count 题目数量
     * @return 推荐题目列表
     */
    List<Question> getRecommendQuestions(String position, Integer count);

    /**
     * 更新问题状态
     * @param questionId 问题ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateQuestionStatus(Long questionId, Integer status);

    /**
     * 增加问题使用次数
     * @param questionId 问题ID
     * @return 是否成功
     */
    boolean increaseUseCount(Long questionId);

    /**
     * 根据关键词搜索题目
     * @param keyword 关键词
     * @return 题目列表
     */
    List<Question> searchByKeyword(String keyword);

    /**
     * 根据标签获取题目
     * @param tags 标签列表
     * @return 题目列表
     */
    List<Question> getByTags(List<String> tags);

    /**
     * 获取热门题目（按使用次数排序）
     * @param count 数量
     * @return 热门题目列表
     */
    List<Question> getPopularQuestions(Integer count);

    /**
     * 根据创建者ID获取题目列表
     * @param creatorId 创建者ID
     * @return 题目列表
     */
    List<Question> getByCreatorId(Long creatorId);

    /**
     * 批量更新题目状态
     * @param questionIds 题目ID列表
     * @param status 状态
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<Long> questionIds, Integer status);
}