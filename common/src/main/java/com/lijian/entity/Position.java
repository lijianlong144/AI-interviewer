package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 职位实体类
 */
@Data
@TableName("t_position")
public class Position {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 职位编号
     */
    private String positionCode;

    /**
     * 职位名称
     */
    private String title;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 工作类型：FULL_TIME/PART_TIME/INTERN
     */
    private String jobType;

    /**
     * 工作地点
     */
    private String workLocation;

    /**
     * 招聘人数
     */
    private Integer headcount;

    /**
     * 最低薪资
     */
    private BigDecimal salaryMin;

    /**
     * 最高薪资
     */
    private BigDecimal salaryMax;

    /**
     * 经验要求
     */
    private String experienceRequired;

    /**
     * 学历要求
     */
    private String educationRequired;

    /**
     * 职位描述
     */
    private String description;

    /**
     * 任职要求
     */
    private String requirements;

    /**
     * 福利待遇
     */
    private String benefits;

    /**
     * 状态：1-招聘中 2-已关闭 3-已暂停
     */
    private Integer status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 关闭时间
     */
    private LocalDateTime closeTime;

    /**
     * 负责HR的ID
     */
    private Long hrId;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-否 1-是
     */
    private Integer isDeleted;
}