package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.Interview;
import com.lijian.entity.InterviewRecord;
import com.lijian.entity.InterviewReport;
import com.lijian.exception.BusinessException;
import com.lijian.mapper.InterviewReportMapper;
import com.lijian.service.InterviewRecordService;
import com.lijian.service.InterviewReportService;
import com.lijian.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 面试报告服务实现类
 */
@Service
public class InterviewReportServiceImpl extends ServiceImpl<InterviewReportMapper, InterviewReport> implements InterviewReportService {

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private InterviewRecordService interviewRecordService;

    @Override
    public InterviewReport getByInterviewId(Long interviewId) {
        LambdaQueryWrapper<InterviewReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewReport::getInterviewId, interviewId);
        return getOne(queryWrapper);
    }

    @Override
    public List<InterviewReport> getByCandidateId(Long candidateId) {
        // 首先获取候选人的所有面试
        List<Interview> interviews = interviewService.getByCandidateId(candidateId);
        if (interviews.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> interviewIds = interviews.stream()
                .map(Interview::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<InterviewReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(InterviewReport::getInterviewId, interviewIds);
        queryWrapper.orderByDesc(InterviewReport::getCreateTime);
        return list(queryWrapper);
    }

    @Override
    @Transactional
    public InterviewReport generateReport(Long interviewId) {
        // 先检查是否已存在报告
        InterviewReport existingReport = getByInterviewId(interviewId);
        if (existingReport != null) {
            return existingReport;
        }

        // 获取面试信息
        Interview interview = interviewService.getById(interviewId);
        if (interview == null) {
            throw new BusinessException("面试不存在");
        }

        // 获取面试记录列表
        List<InterviewRecord> records = interviewRecordService.getByInterviewId(interviewId);
        if (records.isEmpty()) {
            throw new BusinessException("没有面试记录，无法生成报告");
        }

        // 创建新的面试报告
        InterviewReport report = new InterviewReport();
        report.setInterviewId(interviewId);

        // 计算各项评分
        calculateScores(report, records);

        // 分析优势和不足
        analyzeStrengthsAndWeaknesses(report, records);

        // 生成建议和总结
        generateSuggestionsAndSummary(report, records, interview);

        // 保存报告
        save(report);

        // 更新面试总分
        interview.setTotalScore(report.getOverallScore());
        interviewService.updateById(interview);

        return report;
    }

    @Override
    public boolean updateHrComment(Long id, String hrComment) {
        InterviewReport report = getById(id);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }

        report.setHrComment(hrComment);
        return updateById(report);
    }

    @Override
    public boolean updateRecommendation(Long id, String recommendation) {
        InterviewReport report = getById(id);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }

        report.setRecommendation(recommendation);
        return updateById(report);
    }

    @Override
    @Transactional
    public List<InterviewReport> batchGenerateReports(List<Long> interviewIds) {
        if (interviewIds == null || interviewIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<InterviewReport> reports = new ArrayList<>();
        for (Long interviewId : interviewIds) {
            try {
                InterviewReport report = generateReport(interviewId);
                reports.add(report);
            } catch (Exception e) {
                // 记录日志，继续处理下一个
                log.error("生成面试报告失败，面试ID: {}", interviewId, e);
            }
        }

        return reports;
    }

    @Override
    public Object getReportStatistics(Long interviewerId, String startDate, String endDate) {
        LambdaQueryWrapper<InterviewReport> queryWrapper = new LambdaQueryWrapper<>();

        // 如果有面试官ID，需要通过面试表关联查询
        if (interviewerId != null) {
            List<Interview> interviews = interviewService.getByInterviewerId(interviewerId);
            if (interviews.isEmpty()) {
                return new HashMap<>();
            }

            List<Long> interviewIds = interviews.stream()
                    .map(Interview::getId)
                    .collect(Collectors.toList());
            queryWrapper.in(InterviewReport::getInterviewId, interviewIds);
        }

        if (StringUtils.hasText(startDate)) {
            queryWrapper.ge(InterviewReport::getCreateTime, LocalDate.parse(startDate).atStartOfDay());
        }

        if (StringUtils.hasText(endDate)) {
            queryWrapper.le(InterviewReport::getCreateTime, LocalDate.parse(endDate).plusDays(1).atStartOfDay());
        }

        List<InterviewReport> reports = list(queryWrapper);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalReports", reports.size());

        if (!reports.isEmpty()) {
            // 评分统计
            List<BigDecimal> scores = reports.stream()
                    .filter(r -> r.getOverallScore() != null)
                    .map(InterviewReport::getOverallScore)
                    .collect(Collectors.toList());

            if (!scores.isEmpty()) {
                statistics.put("averageScore", calculateAverage(scores));
                statistics.put("maxScore", scores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
                statistics.put("minScore", scores.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
            }

            // 推荐决策统计
            Map<String, Long> recommendationStats = reports.stream()
                    .filter(r -> StringUtils.hasText(r.getRecommendation()))
                    .collect(Collectors.groupingBy(
                            InterviewReport::getRecommendation,
                            Collectors.counting()
                    ));
            statistics.put("recommendationStats", recommendationStats);
        }

        return statistics;
    }

    @Override
    public List<InterviewReport> getByRecommendation(String recommendation) {
        LambdaQueryWrapper<InterviewReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterviewReport::getRecommendation, recommendation);
        queryWrapper.orderByDesc(InterviewReport::getOverallScore);
        return list(queryWrapper);
    }

    @Override
    public List<InterviewReport> getByScoreRange(Double minScore, Double maxScore) {
        LambdaQueryWrapper<InterviewReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(InterviewReport::getOverallScore,
                BigDecimal.valueOf(minScore), BigDecimal.valueOf(maxScore));
        queryWrapper.orderByDesc(InterviewReport::getOverallScore);
        return list(queryWrapper);
    }

    @Override
    public String exportReport(Long id, String format) {
        InterviewReport report = getById(id);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }

        // 这里应该实现实际的导出逻辑
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = "interview_report_" + id + "_" + timestamp + "." + format.toLowerCase();
        String filePath = "/exports/" + fileName;

        // 实际项目中需要根据format生成对应格式的文件
        // if ("PDF".equalsIgnoreCase(format)) {
        //     PDFUtil.generateReport(report, filePath);
        // } else if ("EXCEL".equalsIgnoreCase(format)) {
        //     ExcelUtil.generateReport(report, filePath);
        // }

        return filePath;
    }

    @Override
    @Transactional
    public InterviewReport regenerateReport(Long interviewId, Boolean forceRegenerate) {
        InterviewReport existingReport = getByInterviewId(interviewId);

        if (existingReport != null && !forceRegenerate) {
            return existingReport;
        }

        if (existingReport != null) {
            // 删除现有报告
            removeById(existingReport.getId());
        }

        // 重新生成
        return generateReport(interviewId);
    }

    @Override
    public Object getReportTemplate() {
        Map<String, Object> template = new HashMap<>();

        // 报告结构模板
        template.put("basicInfo", Arrays.asList("候选人姓名", "应聘职位", "面试时间", "面试官"));
        template.put("scores", Arrays.asList("技术能力", "沟通能力", "逻辑思维", "专业素养", "综合评分"));
        template.put("analysis", Arrays.asList("优势分析", "不足分析", "改进建议"));
        template.put("conclusion", Arrays.asList("AI综合评价", "HR评语", "推荐决策"));

        // 评分标准
        Map<String, String> scoreStandards = new HashMap<>();
        scoreStandards.put("90-100", "优秀");
        scoreStandards.put("80-89", "良好");
        scoreStandards.put("70-79", "一般");
        scoreStandards.put("60-69", "较差");
        scoreStandards.put("0-59", "不合格");
        template.put("scoreStandards", scoreStandards);

        return template;
    }

    @Override
    public List<InterviewReport> getExcellentReports(Integer limit) {
        LambdaQueryWrapper<InterviewReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(InterviewReport::getOverallScore, BigDecimal.valueOf(85));
        queryWrapper.orderByDesc(InterviewReport::getOverallScore);
        queryWrapper.last("LIMIT " + limit);
        return list(queryWrapper);
    }

    @Override
    public Double calculateOverallScore(Long interviewId) {
        List<InterviewRecord> records = interviewRecordService.getByInterviewId(interviewId);

        List<BigDecimal> scores = records.stream()
                .filter(r -> r.getAiScore() != null)
                .map(InterviewRecord::getAiScore)
                .collect(Collectors.toList());

        if (scores.isEmpty()) {
            return 0.0;
        }

        return calculateAverage(scores).doubleValue();
    }

    @Override
    public String generateSuggestions(Long interviewId) {
        List<InterviewRecord> records = interviewRecordService.getByInterviewId(interviewId);
        StringBuilder suggestions = new StringBuilder();

        // 分析低分记录
        List<InterviewRecord> lowScoreRecords = records.stream()
                .filter(r -> r.getAiScore() != null && r.getAiScore().compareTo(BigDecimal.valueOf(60)) < 0)
                .collect(Collectors.toList());

        if (!lowScoreRecords.isEmpty()) {
            suggestions.append("需要改进的方面：\n");
            for (InterviewRecord record : lowScoreRecords) {
                suggestions.append("- 加强对\"")
                        .append(record.getQuestionContent())
                        .append("\"相关知识的学习和理解\n");
            }
        }

        // 分析关键词匹配情况
        long noKeywordRecords = records.stream()
                .filter(r -> !StringUtils.hasText(r.getKeywordsMatched()))
                .count();

        if (noKeywordRecords > records.size() / 2) {
            suggestions.append("- 建议加强专业术语的使用，提高回答的专业性\n");
        }

        // 分析回答时长
        double avgAnswerDuration = records.stream()
                .filter(r -> r.getAnswerDuration() != null)
                .mapToInt(InterviewRecord::getAnswerDuration)
                .average()
                .orElse(0.0);

        if (avgAnswerDuration < 30) {
            suggestions.append("- 建议提供更详细和完整的回答\n");
        } else if (avgAnswerDuration > 180) {
            suggestions.append("- 建议回答更加简洁明了，抓住要点\n");
        }

        return suggestions.toString();
    }

    @Override
    public Double getAverageScoreByPosition(String position) {
        // 通过面试表查询该职位的所有面试
        LambdaQueryWrapper<Interview> interviewWrapper = new LambdaQueryWrapper<>();
        interviewWrapper.eq(Interview::getPosition, position);
        List<Interview> interviews = interviewService.list(interviewWrapper);

        if (interviews.isEmpty()) {
            return 0.0;
        }

        List<Long> interviewIds = interviews.stream()
                .map(Interview::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<InterviewReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.in(InterviewReport::getInterviewId, interviewIds);
        reportWrapper.isNotNull(InterviewReport::getOverallScore);
        List<InterviewReport> reports = list(reportWrapper);

        if (reports.isEmpty()) {
            return 0.0;
        }

        List<BigDecimal> scores = reports.stream()
                .map(InterviewReport::getOverallScore)
                .collect(Collectors.toList());

        return calculateAverage(scores).doubleValue();
    }

    @Override
    public boolean needsUpdate(Long interviewId) {
        InterviewReport report = getByInterviewId(interviewId);
        if (report == null) {
            return true;
        }

        // 检查是否有新的面试记录
        List<InterviewRecord> records = interviewRecordService.getByInterviewId(interviewId);

        // 如果面试记录的最后更新时间晚于报告的更新时间，则需要更新
        return records.stream()
                .anyMatch(record -> record.getUpdateTime().isAfter(report.getUpdateTime()));
    }

    @Override
    public Integer getGenerationProgress(Long interviewId) {
        // 简化实现，实际项目中可能需要更复杂的进度跟踪
        InterviewReport report = getByInterviewId(interviewId);
        if (report == null) {
            return 0;
        }

        int progress = 0;
        if (report.getOverallScore() != null) progress += 20;
        if (StringUtils.hasText(report.getStrengths())) progress += 20;
        if (StringUtils.hasText(report.getWeaknesses())) progress += 20;
        if (StringUtils.hasText(report.getSuggestions())) progress += 20;
        if (StringUtils.hasText(report.getAiSummary())) progress += 20;

        return progress;
    }

    /**
     * 计算各项评分
     */
    private void calculateScores(InterviewReport report, List<InterviewRecord> records) {
        List<InterviewRecord> scoredRecords = records.stream()
                .filter(r -> r.getAiScore() != null)
                .collect(Collectors.toList());

        if (!scoredRecords.isEmpty()) {
            BigDecimal totalScore = calculateAverage(
                    scoredRecords.stream()
                            .map(InterviewRecord::getAiScore)
                            .collect(Collectors.toList())
            );

            report.setOverallScore(totalScore);

            // 根据问题类型计算分项得分
            Map<String, List<InterviewRecord>> typeGroups = scoredRecords.stream()
                    .filter(r -> StringUtils.hasText(r.getQuestionType()))
                    .collect(Collectors.groupingBy(InterviewRecord::getQuestionType));

            // 技术能力评分
            List<InterviewRecord> techRecords = typeGroups.getOrDefault("TECHNICAL", new ArrayList<>());
            if (!techRecords.isEmpty()) {
                report.setTechnicalScore(calculateAverage(
                        techRecords.stream().map(InterviewRecord::getAiScore).collect(Collectors.toList())
                ));
            } else {
                report.setTechnicalScore(totalScore);
            }

            // 其他评分项使用总分作为默认值
            report.setCommunicationScore(totalScore);
            report.setLogicScore(totalScore);
            report.setProfessionalScore(totalScore);
        }
    }

    /**
     * 分析优势和不足
     */
    private void analyzeStrengthsAndWeaknesses(InterviewReport report, List<InterviewRecord> records) {
        StringBuilder strengths = new StringBuilder();
        StringBuilder weaknesses = new StringBuilder();

        for (InterviewRecord record : records) {
            if (record.getAiScore() != null) {
                if (record.getAiScore().compareTo(BigDecimal.valueOf(80)) >= 0) {
                    strengths.append("- 在\"")
                            .append(record.getQuestionContent())
                            .append("\"问题上表现优秀\n");
                } else if (record.getAiScore().compareTo(BigDecimal.valueOf(60)) < 0) {
                    weaknesses.append("- 在\"")
                            .append(record.getQuestionContent())
                            .append("\"问题上表现不足\n");
                }
            }
        }

        // 分析关键词匹配情况
        long totalRecords = records.size();
        long withKeywords = records.stream()
                .filter(r -> StringUtils.hasText(r.getKeywordsMatched()))
                .count();

        if (withKeywords > totalRecords * 0.7) {
            strengths.append("- 专业术语使用恰当，体现了良好的专业素养\n");
        } else if (withKeywords < totalRecords * 0.3) {
            weaknesses.append("- 专业术语使用较少，建议加强相关知识学习\n");
        }

        report.setStrengths(strengths.toString());
        report.setWeaknesses(weaknesses.toString());
    }

    /**
     * 生成建议和总结
     */
    private void generateSuggestionsAndSummary(InterviewReport report, List<InterviewRecord> records, Interview interview) {
        // 生成改进建议
        String suggestions = generateSuggestions(interview.getId());
        report.setSuggestions(suggestions);

        // 生成AI总结
        StringBuilder aiSummary = new StringBuilder();

        if (report.getOverallScore() != null) {
            BigDecimal score = report.getOverallScore();
            if (score.compareTo(BigDecimal.valueOf(85)) >= 0) {
                aiSummary.append("候选人表现优秀，");
            } else if (score.compareTo(BigDecimal.valueOf(70)) >= 0) {
                aiSummary.append("候选人表现良好，");
            } else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
                aiSummary.append("候选人表现一般，");
            } else {
                aiSummary.append("候选人表现较差，");
            }

            aiSummary.append("综合评分为").append(score).append("分。");

            // 根据分数给出推荐建议
            if (score.compareTo(BigDecimal.valueOf(80)) >= 0) {
                aiSummary.append("建议录用。");
                report.setRecommendation("RECOMMEND");
            } else if (score.compareTo(BigDecimal.valueOf(60)) >= 0) {
                aiSummary.append("可以考虑进一步评估或安排二轮面试。");
                report.setRecommendation("NEUTRAL");
            } else {
                aiSummary.append("建议不予录用。");
                report.setRecommendation("NOT_RECOMMEND");
            }
        }

        report.setAiSummary(aiSummary.toString());
    }

    /**
     * 计算平均值
     */
    private BigDecimal calculateAverage(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InterviewReportServiceImpl.class);
}