package com.lijian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.Interview;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试服务接口
 */
public interface InterviewService extends IService<Interview> {

    /**
     * 创建面试
     * @param interview 面试信息
     * @return 创建后的面试
     */
    Interview createInterview(Interview interview);

    /**
     * 根据候选人ID查询面试列表
     * @param candidateId 候选人ID
     * @return 面试列表
     */
    List<Interview> getByCandidateId(Long candidateId);

    /**
     * 根据面试官ID查询面试列表
     * @param interviewerId 面试官ID
     * @return 面试列表
     */
    List<Interview> getByInterviewerId(Long interviewerId);

    /**
     * 根据状态查询面试列表
     * @param status 状态
     * @return 面试列表
     */
    List<Interview> getByStatus(String status);

    /**
     * 分页查询面试列表
     * @param page 分页参数
     * @param status 状态
     * @param position 职位
     * @param candidateId 候选人ID
     * @param interviewerId 面试官ID
     * @return 面试分页列表
     */
    Page<Interview> pageInterviews(Page<Interview> page, String status, String position, Long candidateId, Long interviewerId);

    /**
     * 开始面试
     * @param id 面试ID
     * @return 是否成功
     */
    boolean startInterview(Long id);

    /**
     * 结束面试
     * @param id 面试ID
     * @return 是否成功
     */
    boolean endInterview(Long id);

    /**
     * 取消面试
     * @param id 面试ID
     * @param reason 取消原因
     * @return 是否成功
     */
    boolean cancelInterview(Long id, String reason);

    /**
     * 根据房间号获取面试
     * @param roomCode 房间号
     * @return 面试信息
     */
    Interview getByRoomCode(String roomCode);

    /**
     * 检查面试状态
     * @param id 面试ID
     * @return 面试状态
     */
    String checkInterviewStatus(Long id);

    /**
     * 获取今日面试列表
     * @param interviewerId 面试官ID（可选）
     * @return 今日面试列表
     */
    List<Interview> getTodayInterviews(Long interviewerId);

    /**
     * 获取即将开始的面试
     * @param userId 用户ID（候选人或面试官）
     * @param hours 未来多少小时内
     * @return 即将开始的面试列表
     */
    List<Interview> getUpcomingInterviews(Long userId, Integer hours);

    /**
     * 重新安排面试时间
     * @param id 面试ID
     * @param newTime 新的面试时间
     * @param reason 重新安排原因
     * @return 是否成功
     */
    boolean rescheduleInterview(Long id, LocalDateTime newTime, String reason);

    /**
     * 获取面试统计信息
     * @param interviewerId 面试官ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    Object getInterviewStatistics(Long interviewerId, String startDate, String endDate);

    /**
     * 批量更新面试状态
     * @param interviewIds 面试ID列表
     * @param status 状态
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<Long> interviewIds, String status);

    /**
     * 生成面试房间号
     * @return 房间号
     */
    String generateRoomCode();

    /**
     * 检查面试时间冲突
     * @param candidateId 候选人ID
     * @param interviewerId 面试官ID
     * @param scheduledTime 预约时间
     * @param duration 预计时长（分钟）
     * @return 是否有冲突
     */
    boolean checkTimeConflict(Long candidateId, Long interviewerId, LocalDateTime scheduledTime, Integer duration);

    /**
     * 获取面试时长统计
     * @param interviewId 面试ID
     * @return 面试时长（分钟）
     */
    Integer calculateInterviewDuration(Long interviewId);

    /**
     * 自动取消超时未开始的面试
     * @param timeoutMinutes 超时分钟数
     * @return 取消的面试数量
     */
    int autoCancelTimeoutInterviews(Integer timeoutMinutes);
}