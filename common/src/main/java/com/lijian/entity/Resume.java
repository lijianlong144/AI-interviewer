package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 简历实体类
 */
@Data
@TableName("t_resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 候选人ID
     */
    private Long candidateId;

    /**
     * 简历名称
     */
    private String resumeName;

    /**
     * 简历文件URL
     */
    private String resumeUrl;

    /**
     * 简历类型：UPLOAD/ONLINE
     */
    private String resumeType;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 当前所在地
     */
    private String currentLocation;

    /**
     * 期望工作地
     */
    private String expectedLocation;

    /**
     * 最高学历
     */
    private String highestEducation;

    /**
     * 毕业院校
     */
    private String graduationSchool;

    /**
     * 专业
     */
    private String major;

    /**
     * 毕业年份
     */
    private Integer graduationYear;

    /**
     * 工作年限
     */
    private Integer workYears;

    /**
     * 当前公司
     */
    private String currentCompany;

    /**
     * 当前职位
     */
    private String currentPosition;

    /**
     * 工作经历详情
     */
    private String workExperience;

    /**
     * 技能特长
     */
    private String skills;

    /**
     * 自我评价
     */
    private String selfEvaluation;

    /**
     * 期望最低薪资
     */
    private BigDecimal expectedSalaryMin;

    /**
     * 期望最高薪资
     */
    private BigDecimal expectedSalaryMax;

    /**
     * 可到岗日期
     */
    private LocalDate availableDate;

    /**
     * 附件URL
     */
    private String attachmentUrl;

    /**
     * 状态：1-有效 0-无效
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}