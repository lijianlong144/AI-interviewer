package com.lijian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.Interview;

import java.util.List;

/**
 * 面试服务接口
 */
public interface InterviewService extends IService<Interview> {
    
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
} 