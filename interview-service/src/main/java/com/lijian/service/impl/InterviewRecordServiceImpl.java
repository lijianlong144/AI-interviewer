package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.InterviewRecord;
import com.lijian.entity.Question;
import com.lijian.exception.BusinessException;
import com.lijian.mapper.InterviewRecordMapper;
import com.lijian.service.InterviewRecordService;
import com.lijian.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 面试记录服务实现类
 */
@Service
public class InterviewRecordServiceImpl extends ServiceImpl<InterviewRecordMapper, InterviewRecord> implements InterviewRecordService {

    @Autowired
    private QuestionService questionService;

    @Override
    public List<InterviewRecord> getByInterviewId(Long interviewId) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);
        queryWrapper.orderByAsc(InterviewRecord::getSequence);
        return list(queryWrapper);
    }

    @Override
    @Transactional
    public boolean saveAndAnalyze(InterviewRecord record) {
        // 设置序号
        if (record.getSequence() == null) {
            record.setSequence(getNextSequence(record.getInterviewId()));
        }

        // 如果有题目ID，则获取题目信息进行分析
        if (record.getQuestionId() != null) {
            analyzeRecordWithQuestion(record);
        } else {
            // 如果是AI生成的问题，进行基础分析
            analyzeRecordWithoutQuestion(record);
        }

        return save(record);
    }

    @Override
    public InterviewRecord getByInterviewIdAndQuestionId(Long interviewId, Long questionId) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);
        queryWrapper.eq(InterviewRecord::getQuestionId, questionId);
        return getOne(queryWrapper);
    }

    @Override
    @Transactional
    public boolean batchSaveAndAnalyze(List<InterviewRecord> records) {
        if (records == null || records.isEmpty()) {
            return false;
        }

        for (InterviewRecord record : records) {
            saveAndAnalyze(record);
        }

        return true;
    }

    @Override
    @Transactional
    public InterviewRecord reanalyzeRecord(Long recordId) {
        InterviewRecord record = getById(recordId);
        if (record == null) {
            throw new BusinessException("记录不存在");
        }

        // 重新分析
        if (record.getQuestionId() != null) {
            analyzeRecordWithQuestion(record);
        } else {
            analyzeRecordWithoutQuestion(record);
        }

        updateById(record);
        return record;
    }

    @Override
    public Object getRecordStatistics(Long interviewId) {
        List<InterviewRecord> records = getByInterviewId(interviewId);

        Map<String, Object> statistics = new HashMap<>();

        if (records.isEmpty()) {
            return statistics;
        }

        // 基础统计
        statistics.put("totalRecords", records.size());

        // 评分统计
        List<InterviewRecord> scoredRecords = records.stream()
                .filter(r -> r.getAiScore() != null)
                .collect(Collectors.toList());

        if (!scoredRecords.isEmpty()) {
            Double avgScore = scoredRecords.stream()
                    .mapToDouble(r -> r.getAiScore().doubleValue())
                    .average()
                    .orElse(0.0);

            Double maxScore = scoredRecords.stream()
                    .mapToDouble(r -> r.getAiScore().doubleValue())
                    .max()
                    .orElse(0.0);

            Double minScore = scoredRecords.stream()
                    .mapToDouble(r -> r.getAiScore().doubleValue())
                    .min()
                    .orElse(0.0);

            statistics.put("averageScore", BigDecimal.valueOf(avgScore).setScale(2, RoundingMode.HALF_UP));
            statistics.put("maxScore", BigDecimal.valueOf(maxScore).setScale(2, RoundingMode.HALF_UP));
            statistics.put("minScore", BigDecimal.valueOf(minScore).setScale(2, RoundingMode.HALF_UP));
        }

        // 问题类型统计
        Map<String, Long> typeStats = records.stream()
                .filter(r -> StringUtils.hasText(r.getQuestionType()))
                .collect(Collectors.groupingBy(
                        InterviewRecord::getQuestionType,
                        Collectors.counting()
                ));
        statistics.put("questionTypeStats", typeStats);

        // 关键词匹配统计
        long withKeywords = records.stream()
                .filter(r -> StringUtils.hasText(r.getKeywordsMatched()))
                .count();
        statistics.put("recordsWithKeywords", withKeywords);

        return statistics;
    }

    @Override
    public List<InterviewRecord> getByScoreRange(Long interviewId, Double minScore, Double maxScore) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);
        queryWrapper.between(InterviewRecord::getAiScore,
                BigDecimal.valueOf(minScore), BigDecimal.valueOf(maxScore));
        queryWrapper.orderByDesc(InterviewRecord::getAiScore);
        return list(queryWrapper);
    }

    @Override
    public List<InterviewRecord> getHighScoreRecords(Long interviewId, Double threshold) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);
        queryWrapper.ge(InterviewRecord::getAiScore, BigDecimal.valueOf(threshold));
        queryWrapper.orderByDesc(InterviewRecord::getAiScore);
        return list(queryWrapper);
    }

    @Override
    public List<InterviewRecord> getLowScoreRecords(Long interviewId, Double threshold) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);
        queryWrapper.lt(InterviewRecord::getAiScore, BigDecimal.valueOf(threshold));
        queryWrapper.orderByAsc(InterviewRecord::getAiScore);
        return list(queryWrapper);
    }

    @Override
    public String exportRecords(Long interviewId) {
        // 这里应该实现导出逻辑，返回文件路径
        // 简化实现，实际项目中需要生成Excel或PDF文件
        List<InterviewRecord> records = getByInterviewId(interviewId);

        // 模拟生成文件路径
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = "interview_records_" + interviewId + "_" + timestamp + ".xlsx";
        String filePath = "/exports/" + fileName;

        // 这里应该有实际的文件生成逻辑
        // ExcelUtil.exportRecords(records, filePath);

        return filePath;
    }

    @Override
    public List<InterviewRecord> getByQuestionType(Long interviewId, String questionType) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);
        queryWrapper.eq(InterviewRecord::getQuestionType, questionType);
        queryWrapper.orderByAsc(InterviewRecord::getSequence);
        return list(queryWrapper);
    }

    @Override
    public Double calculateAverageScore(Long interviewId) {
        List<InterviewRecord> records = getByInterviewId(interviewId);

        List<BigDecimal> scores = records.stream()
                .filter(r -> r.getAiScore() != null)
                .map(InterviewRecord::getAiScore)
                .collect(Collectors.toList());

        if (scores.isEmpty()) {
            return 0.0;
        }

        BigDecimal sum = scores.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    @Override
    public InterviewRecord getHighestScoreRecord(Long interviewId) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);
        queryWrapper.isNotNull(InterviewRecord::getAiScore);
        queryWrapper.orderByDesc(InterviewRecord::getAiScore);
        queryWrapper.last("LIMIT 1");
        return getOne(queryWrapper);
    }

    @Override
    public InterviewRecord getLowestScoreRecord(Long interviewId) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);
        queryWrapper.isNotNull(InterviewRecord::getAiScore);
        queryWrapper.orderByAsc(InterviewRecord::getAiScore);
        queryWrapper.last("LIMIT 1");
        return getOne(queryWrapper);
    }

    @Override
    public List<InterviewRecord> getByKeywordMatch(Long interviewId, Boolean hasKeywords) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);

        if (hasKeywords) {
            queryWrapper.isNotNull(InterviewRecord::getKeywordsMatched)
                    .ne(InterviewRecord::getKeywordsMatched, "");
        } else {
            queryWrapper.and(wrapper ->
                    wrapper.isNull(InterviewRecord::getKeywordsMatched)
                            .or()
                            .eq(InterviewRecord::getKeywordsMatched, "")
            );
        }

        queryWrapper.orderByAsc(InterviewRecord::getSequence);
        return list(queryWrapper);
    }

    /**
     * 获取下一个序号
     */
    private Integer getNextSequence(Long interviewId) {
        LambdaQueryWrapper<InterviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewRecord::getInterviewId, interviewId);
        queryWrapper.orderByDesc(InterviewRecord::getSequence);
        queryWrapper.last("LIMIT 1");

        InterviewRecord lastRecord = getOne(queryWrapper);
        return lastRecord == null ? 1 : lastRecord.getSequence() + 1;
    }

    /**
     * 基于题库题目进行分析
     */
    private void analyzeRecordWithQuestion(InterviewRecord record) {
        Question question = questionService.getById(record.getQuestionId());
        if (question == null) {
            return;
        }

        // 设置问题内容和类型
        record.setQuestionContent(question.getContent());
        record.setQuestionType("BANK");

        // 增加题目使用次数
        questionService.increaseUseCount(question.getId());

        // 关键词匹配分析
        if (StringUtils.hasText(question.getKeywords()) && StringUtils.hasText(record.getAnswerText())) {
            analyzeKeywords(record, question.getKeywords());
        }

        // 简单评分计算
        calculateSimpleScore(record, question);
    }

    /**
     * 不基于题库题目的分析
     */
    private void analyzeRecordWithoutQuestion(InterviewRecord record) {
        record.setQuestionType("AI");

        // 简单的文本分析评分
        if (StringUtils.hasText(record.getAnswerText())) {
            // 基于回答长度和质量的简单评分
            int answerLength = record.getAnswerText().length();
            BigDecimal score = BigDecimal.valueOf(Math.min(100, answerLength / 10.0));
            record.setAiScore(score);

            // 简单的AI分析
            record.setAiAnalysis("基于回答长度和结构的初步分析");
        }
    }

    /**
     * 关键词分析
     */
    private void analyzeKeywords(InterviewRecord record, String keywords) {
        String[] keywordArray = keywords.split(",");
        List<String> matchedKeywords = new ArrayList<>();

        String answerText = record.getAnswerText().toLowerCase();

        for (String keyword : keywordArray) {
            String trimmedKeyword = keyword.trim().toLowerCase();
            if (answerText.contains(trimmedKeyword)) {
                matchedKeywords.add(keyword.trim());
            }
        }

        if (!matchedKeywords.isEmpty()) {
            record.setKeywordsMatched(String.join(",", matchedKeywords));
        }
    }

    /**
     * 计算简单评分
     */
    private void calculateSimpleScore(InterviewRecord record, Question question) {
        if (!StringUtils.hasText(record.getAnswerText())) {
            record.setAiScore(BigDecimal.ZERO);
            return;
        }

        double score = 0.0;

        // 基于关键词匹配的评分（60%权重）
        if (StringUtils.hasText(question.getKeywords()) && StringUtils.hasText(record.getKeywordsMatched())) {
            String[] allKeywords = question.getKeywords().split(",");
            String[] matchedKeywords = record.getKeywordsMatched().split(",");
            double keywordScore = (double) matchedKeywords.length / allKeywords.length * 100;
            score += keywordScore * 0.6;
        }

        // 基于回答长度的评分（20%权重）
        int answerLength = record.getAnswerText().length();
        double lengthScore = Math.min(100, answerLength / 5.0); // 500字符满分
        score += lengthScore * 0.2;

        // 基于回答完整性的评分（20%权重）
        // 简单判断：包含标点符号、有层次等
        double completenessScore = calculateCompletenessScore(record.getAnswerText());
        score += completenessScore * 0.2;

        record.setAiScore(BigDecimal.valueOf(Math.min(100, score)).setScale(2, RoundingMode.HALF_UP));

        // 生成简单的AI分析
        generateSimpleAnalysis(record, question);
    }

    /**
     * 计算完整性评分
     */
    private double calculateCompletenessScore(String answerText) {
        double score = 0.0;

        // 检查是否有标点符号
        if (answerText.matches(".*[。！？.!?].*")) {
            score += 30;
        }

        // 检查是否有逻辑词汇
        String[] logicWords = {"首先", "其次", "然后", "最后", "因为", "所以", "但是", "然而"};
        for (String word : logicWords) {
            if (answerText.contains(word)) {
                score += 10;
                break;
            }
        }

        // 检查句子数量
        String[] sentences = answerText.split("[。！？.!?]");
        if (sentences.length >= 3) {
            score += 30;
        } else if (sentences.length >= 2) {
            score += 20;
        }

        // 检查是否有具体例子
        if (answerText.contains("例如") || answerText.contains("比如") || answerText.contains("举例")) {
            score += 30;
        }

        return Math.min(100, score);
    }

    /**
     * 生成简单的AI分析
     */
    private void generateSimpleAnalysis(InterviewRecord record, Question question) {
        StringBuilder analysis = new StringBuilder();

        BigDecimal score = record.getAiScore();
        if (score == null) {
            return;
        }

        if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
            analysis.append("回答质量优秀，");
        } else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
            analysis.append("回答质量良好，");
        } else {
            analysis.append("回答质量需要改进，");
        }

        if (StringUtils.hasText(record.getKeywordsMatched())) {
            analysis.append("关键词匹配度较高，");
        } else {
            analysis.append("建议增强相关专业术语的使用，");
        }

        if (record.getAnswerText().length() > 200) {
            analysis.append("回答详细充分。");
        } else {
            analysis.append("建议提供更详细的说明。");
        }

        record.setAiAnalysis(analysis.toString());
    }
}