package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.Interview;
import com.lijian.mapper.InterviewMapper;
import com.lijian.service.InterviewService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 面试服务实现类
 */
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview> implements InterviewService {
    
    @Override
    public List<Interview> getByCandidateId(Long candidateId) {
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Interview::getCandidateId, candidateId);
        return list(queryWrapper);
    }
    
    @Override
    public List<Interview> getByInterviewerId(Long interviewerId) {
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Interview::getInterviewerId, interviewerId);
        return list(queryWrapper);
    }
    
    @Override
    public List<Interview> getByStatus(String status) {
        LambdaQueryWrapper<Interview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Interview::getStatus, status);
        return list(queryWrapper);
    }
    
    @Override
    public boolean startInterview(Long id) {
        Interview interview = getById(id);
        if (interview == null) {
            return false;
        }
        
        interview.setStatus("ONGOING");
        interview.setStartTime(LocalDateTime.now());
        return updateById(interview);
    }
    
    @Override
    public boolean endInterview(Long id) {
        Interview interview = getById(id);
        if (interview == null) {
            return false;
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        interview.setStatus("COMPLETED");
        interview.setEndTime(endTime);
        
        // 计算面试时长（分钟）
        if (interview.getStartTime() != null) {
            long minutes = ChronoUnit.MINUTES.between(interview.getStartTime(), endTime);
            interview.setDuration((int) minutes);
        }
        
        return updateById(interview);
    }
} 