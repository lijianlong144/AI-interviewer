package com.lijian.dto;

import com.lijian.entity.Interview;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 面试数据传输对象，包含候选人姓名
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InterviewDTO extends Interview {
    
    /**
     * 候选人姓名
     */
    private String candidateName;
} 