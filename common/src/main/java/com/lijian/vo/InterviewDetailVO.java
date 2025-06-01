package com.lijian.vo;

import com.lijian.entity.Interview;
import com.lijian.entity.InterviewRecord;
import com.lijian.entity.InterviewReport;
import com.lijian.entity.SysUser;
import lombok.Data;

import java.util.List;

/**
 * 面试详情VO
 *
 * @author lijian
 * @since 1.0.0
 */
@Data
public class InterviewDetailVO {

    /**
     * 面试基本信息
     */
    private Interview interview;

    /**
     * 候选人信息
     */
    private SysUser candidate;

    /**
     * 面试官信息
     */
    private SysUser interviewer;

    /**
     * 面试记录列表
     */
    private List<InterviewRecord> records;

    /**
     * 面试报告
     */
    private InterviewReport report;

    /**
     * 面试状态描述
     */
    private String statusDesc;

    /**
     * 是否可以开始面试
     */
    private Boolean canStart;

    /**
     * 是否可以结束面试
     */
    private Boolean canEnd;

    /**
     * 是否可以取消面试
     */
    private Boolean canCancel;
}