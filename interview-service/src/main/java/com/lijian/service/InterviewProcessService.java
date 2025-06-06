package com.lijian.service;

import com.lijian.entity.Application;
import com.lijian.entity.Interview;
import com.lijian.entity.Position;
import com.lijian.entity.Resume;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试流程服务接口
 * 整合职位管理、简历管理、申请管理、面试管理和邮件通知等功能
 */
public interface InterviewProcessService {

    /**
     * HR发布职位
     * @param position 职位信息
     * @return 创建后的职位
     */
    Position publishPosition(Position position);

    /**
     * 候选人投递简历
     * @param candidateId 候选人ID
     * @param positionId 职位ID
     * @param resumeId 简历ID
     * @return 创建后的申请
     */
    Application applyPosition(Long candidateId, Long positionId, Long resumeId);

    /**
     * HR初筛简历
     * @param applicationId 申请ID
     * @param status 状态：PASSED/REJECTED
     * @param remark 备注
     * @return 是否成功
     */
    boolean screenResume(Long applicationId, String status, String remark);

    /**
     * 安排面试
     * @param applicationId 申请ID
     * @param interviewerId 面试官ID
     * @param scheduledTime 预约时间
     * @param duration 预计时长（分钟）
     * @return 创建后的面试
     */
    Interview scheduleInterview(Long applicationId, Long interviewerId, LocalDateTime scheduledTime, Integer duration);

    /**
     * 发送面试邀请邮件
     * @param interviewId 面试ID
     * @return 邮件日志ID
     */
    Long sendInterviewInvitation(Long interviewId);

    /**
     * 候选人确认面试
     * @param interviewId 面试ID
     * @param confirmed 是否确认
     * @param reason 拒绝原因（如果不确认）
     * @return 是否成功
     */
    boolean confirmInterview(Long interviewId, boolean confirmed, String reason);

    /**
     * 开始面试
     * @param interviewId 面试ID
     * @return 是否成功
     */
    boolean startInterview(Long interviewId);

    /**
     * 结束面试
     * @param interviewId 面试ID
     * @return 是否成功
     */
    boolean endInterview(Long interviewId);

    /**
     * 生成面试报告
     * @param interviewId 面试ID
     * @return 报告ID
     */
    Long generateInterviewReport(Long interviewId);

    /**
     * 发送面试结果通知
     * @param interviewId 面试ID
     * @param result 结果：PASS/FAIL
     * @param feedback 反馈
     * @param nextSteps 下一步安排
     * @return 邮件日志ID
     */
    Long sendInterviewResult(Long interviewId, String result, String feedback, String nextSteps);

    /**
     * 获取候选人的面试流程状态
     * @param candidateId 候选人ID
     * @return 面试流程状态列表
     */
    List<Object> getCandidateInterviewProcess(Long candidateId);

    /**
     * 获取职位的申请和面试统计
     * @param positionId 职位ID
     * @return 统计信息
     */
    Object getPositionStatistics(Long positionId);
}