package com.lijian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮件日志实体类
 */
@Data
@TableName("t_email_log")
public class EmailLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 收件人邮箱
     */
    private String toEmail;

    /**
     * 抄送邮箱
     */
    private String ccEmail;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String content;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 状态
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}