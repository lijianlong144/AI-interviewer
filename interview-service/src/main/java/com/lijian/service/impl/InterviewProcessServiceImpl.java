package com.lijian.service.impl;

import com.lijian.entity.*;
import com.lijian.service.*;
import com.lijian.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.math.BigDecimal;

/**
 * 面试流程服务实现类
 */
@Slf4j
@Service
public class InterviewProcessServiceImpl implements InterviewProcessService {

    //初始化日志
//    private static final Logger log = LoggerFactory.getLogger(InterviewProcessService.class);

    @Autowired
    private PositionService positionService;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private InterviewReportService interviewReportService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SysUserService userService;

    @Override
    @Transactional
    public Position publishPosition(Position position) {
        return positionService.createPosition(position);
    }

    @Override
    @Transactional
    public Application applyPosition(Long candidateId, Long positionId, Long resumeId) {
        // 检查职位是否存在
        Position position = positionService.getById(positionId);
        if (position == null || position.getStatus() != 1) {
            throw new RuntimeException("职位不存在或已关闭");
        }

        // 检查简历是否存在
        Resume resume = resumeService.getById(resumeId);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }

        // 检查是否已申请过该职位
        boolean hasApplied = applicationService.checkIfApplied(candidateId, positionId);
        if (hasApplied) {
            throw new RuntimeException("您已申请过该职位");
        }

        // 创建申请
        Application application = new Application();
        application.setCandidateId(candidateId);
        application.setPositionId(positionId);
        application.setResumeId(resumeId);
        application.setStatus("PENDING");
        application.setSource("WEBSITE"); // 默认来源
        application.setIsRead(0); // 未读
        application.setApplyTime(LocalDateTime.now());
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        
        // 生成申请编号
        application.setApplicationNo(generateApplicationNo());

        applicationService.save(application);
        return application;
    }

    /**
     * HR初筛简历TODO不知道为什么必须加入rollbackFor？
     * @param applicationId 申请ID
     * @param status 状态：PASSED/REJECTED
     * @param remark 备注
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean screenResume(Long applicationId, String status, String remark) {
        // 检查申请是否存在
        Application application = applicationService.getById(applicationId);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }

        // 只有处于PENDING状态的申请才能进行筛选
        if (!"PENDING".equals(application.getStatus())) {
            throw new RuntimeException("申请状态不正确，无法进行筛选");
        }

        // 更新申请状态
        return applicationService.updateStatus(applicationId, status, remark);
    }

    @Override
    @Transactional
    public Interview scheduleInterview(Long applicationId, Long interviewerId, LocalDateTime scheduledTime, Integer duration) {
        // 检查申请是否存在
        Application application = applicationService.getById(applicationId);
        if (application == null) {
            throw new RuntimeException("申请不存在");
        }

        // 只有通过初筛的申请才能安排面试
        if (!"PASSED".equals(application.getStatus())) {
            throw new RuntimeException("申请未通过初筛，无法安排面试");
        }

        // 检查面试官是否存在
        SysUser interviewer = userService.getById(interviewerId);
        if (interviewer == null) {
            throw new RuntimeException("面试官不存在");
        }

        // 检查时间冲突
        boolean hasConflict = interviewService.checkTimeConflict(application.getCandidateId(), interviewerId, scheduledTime, duration);
        if (hasConflict) {
            throw new RuntimeException("面试时间冲突，请选择其他时间");
        }

        // 获取职位信息
        Position position = positionService.getById(application.getPositionId());

        // 创建面试
        Interview interview = new Interview();
        interview.setInterviewNo(generateInterviewNo());
        interview.setCandidateId(application.getCandidateId());
        interview.setPosition(position.getTitle());
        interview.setInterviewerId(interviewerId);
        interview.setScheduledTime(scheduledTime);
        interview.setDuration(duration);
        interview.setStatus("PENDING");
        interview.setRoomCode(interviewService.generateRoomCode());
        interview.setCreateTime(LocalDateTime.now());
        interview.setUpdateTime(LocalDateTime.now());
        interview.setTitle(position.getTitle());
        interview.setCreatorId(SecurityUtils.getCurrentUserId());
        interview.setApplicationId(applicationId);
        interview.setPositionId(position.getId());
        log.info("interviewService：---------------",interview);
        interviewService.save(interview);

        // 更新申请状态和面试ID
        application.setStatus("INTERVIEW_SCHEDULED");
        application.setInterviewId(interview.getId());
        application.setUpdateTime(LocalDateTime.now());
        applicationService.updateById(application);

        return interview;
    }

    @Override
    @Transactional
    public Long sendInterviewInvitation(Long interviewId) {
        // 获取面试信息
        Interview interview = interviewService.getById(interviewId);
        if (interview == null) {
            throw new RuntimeException("面试不存在");
        }

        // 获取候选人信息
        SysUser candidate = userService.getById(interview.getCandidateId());
        if (candidate == null) {
            throw new RuntimeException("候选人不存在");
        }

        // 获取面试官信息
        SysUser interviewer = userService.getById(interview.getInterviewerId());
        if (interviewer == null) {
            throw new RuntimeException("面试官不存在");
        }

        // 格式化面试时间
        String interviewTime = interview.getScheduledTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // 面试地点（这里使用虚拟面试房间链接）
        String interviewLocation = "线上面试，面试房间号：" + interview.getRoomCode();

        // 发送邮件
        return emailService.sendInterviewInvitation(
                candidate.getEmail(),
                candidate.getRealName(),
                interview.getPosition(),
                interviewTime,
                interviewLocation,
                interviewer.getUsername(),
                "如有问题，请联系HR邮箱：hr@371723.xyz"
        );
    }

    @Override
    @Transactional
    public boolean confirmInterview(Long interviewId, boolean confirmed, String reason) {
        // 获取面试信息
        Interview interview = interviewService.getById(interviewId);
        if (interview == null) {
            throw new RuntimeException("面试不存在");
        }

        // 只有处于PENDING状态的面试才能进行确认
        if (!"PENDING".equals(interview.getStatus())) {
            throw new RuntimeException("面试状态不正确，无法进行确认");
        }

        // 更新面试状态
        if (confirmed) {
            interview.setStatus("CONFIRMED");
        } else {
            interview.setStatus("CANCELLED");
            // 如果有remark字段，可以添加取消原因
            // interview.setRemark(reason);
        }

        interview.setUpdateTime(LocalDateTime.now());
        return interviewService.updateById(interview);
    }

    @Override
    @Transactional
    public boolean startInterview(Long interviewId) {
        return interviewService.startInterview(interviewId);
    }

    @Override
    @Transactional
    public boolean endInterview(Long interviewId) {
        return interviewService.endInterview(interviewId);
    }

    @Override
    @Transactional
    public Long generateInterviewReport(Long interviewId) {
        // 获取面试信息
        Interview interview = interviewService.getById(interviewId);
        if (interview == null) {
            throw new RuntimeException("面试不存在");
        }

        // 只有已结束的面试才能生成报告
        if (!"COMPLETED".equals(interview.getStatus())) {
            throw new RuntimeException("面试未结束，无法生成报告");
        }

        // 创建面试报告
        InterviewReport report = new InterviewReport();
        report.setInterviewId(interviewId);
        report.setCandidateId(interview.getCandidateId());
        report.setInterviewerId(interview.getInterviewerId());
        report.setPosition(interview.getPosition());
        report.setInterviewTime(interview.getScheduledTime());
        report.setDuration(interview.getDuration());
        report.setTechnicalScore(new BigDecimal("0.0")); // 这里需要根据实际情况设置
        report.setCommunicationScore(new BigDecimal("0.0")); // 这里需要根据实际情况设置
        report.setProblemSolvingScore(new BigDecimal("0.0")); // 这里需要根据实际情况设置
        report.setCulturalFitScore(new BigDecimal("0.0")); // 这里需要根据实际情况设置
        report.setOverallEvaluation(""); // 这里需要根据实际情况设置
        report.setStrengths(""); // 这里需要根据实际情况设置
        report.setWeaknesses(""); // 这里需要根据实际情况设置
        report.setRecommendation(""); // 这里需要根据实际情况设置
        report.setStatus("DRAFT");
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());

        interviewReportService.save(report);
        return report.getId();
    }

    @Override
    @Transactional
    public Long sendInterviewResult(Long interviewId, String result, String feedback, String nextSteps) {
        // 获取面试信息
        Interview interview = interviewService.getById(interviewId);
        if (interview == null) {
            throw new RuntimeException("面试不存在");
        }

        // 获取候选人信息
        SysUser candidate = userService.getById(interview.getCandidateId());
        if (candidate == null) {
            throw new RuntimeException("候选人不存在");
        }

        // 发送邮件
        return emailService.sendInterviewResult(
                candidate.getEmail(),
                candidate.getUsername(),
                interview.getPosition(),
                result,
                feedback,
                nextSteps
        );
    }

    @Override
    public List<Object> getCandidateInterviewProcess(Long candidateId) {
        // 获取候选人的所有申请
        List<Application> applications = applicationService.getApplicationsByCandidateId(candidateId);
        
        // 构建流程状态列表
        List<Object> processList = new ArrayList<>();
        for (Application application : applications) {
            Map<String, Object> processItem = new HashMap<>();
            processItem.put("applicationId", application.getId());
            processItem.put("applicationNo", application.getApplicationNo());
            processItem.put("positionId", application.getPositionId());
            processItem.put("status", application.getStatus());
            processItem.put("applyTime", application.getApplyTime());
            
            // 如果申请状态为INTERVIEW_SCHEDULED，获取面试信息
            if ("INTERVIEW_SCHEDULED".equals(application.getStatus()) && application.getInterviewId() != null) {
                Interview interview = interviewService.getById(application.getInterviewId());
                if (interview != null) {
                    processItem.put("interviewId", interview.getId());
                    processItem.put("interviewStatus", interview.getStatus());
                    processItem.put("scheduledTime", interview.getScheduledTime());
                    processItem.put("roomCode", interview.getRoomCode());
                }
            }
            
            processList.add(processItem);
        }
        
        return processList;
    }

    @Override
    public Object getPositionStatistics(Long positionId) {
        // 获取职位信息
        Position position = positionService.getById(positionId);
        if (position == null) {
            throw new RuntimeException("职位不存在");
        }
        
        // 获取该职位的所有申请
        List<Application> applications = applicationService.getApplicationsByPositionId(positionId);
        
        // 统计各状态的申请数量
        Map<String, Long> statusCounts = new HashMap<>();
        for (Application application : applications) {
            String status = application.getStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
        }
        
        // 构建统计结果
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("positionId", positionId);
        statistics.put("positionName", position.getTitle());
        statistics.put("totalApplications", applications.size());
        statistics.put("statusCounts", statusCounts);
        
        return statistics;
    }

    /**
     * 生成面试编号
     */
    private String generateInterviewNo() {
        // 生成格式：INT + 年月日 + 6位随机数
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 生成6位随机数
        Random random = new Random();
        int randomNum = random.nextInt(1000000);
        String randomStr = String.format("%06d", randomNum);
        
        return "INT" + dateStr + randomStr;
    }
    
    /**
     * 生成申请编号
     */
    private String generateApplicationNo() {
        // 生成格式：APP + 年月日 + 4位随机数
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 生成4位随机数
        Random random = new Random();
        int randomNum = random.nextInt(10000);
        String randomStr = String.format("%04d", randomNum);
        
        return "APP" + dateStr + randomStr;
    }
}