package com.lijian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lijian.entity.EmailLog;
import com.lijian.entity.EmailTemplate;

import java.util.List;
import java.util.Map;

/**
 * 邮件服务接口
 */
public interface EmailService extends IService<EmailLog> {

    /**
     * 发送简单文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @return 邮件日志ID
     */
    Long sendSimpleEmail(String to, String subject, String content);

    /**
     * 发送HTML邮件
     * @param to 收件人
     * @param subject 主题
     * @param htmlContent HTML内容
     * @return 邮件日志ID
     */
    Long sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * 使用数据库模板发送邮件
     * @param to 收件人
     * @param templateName 模板名字
     * @param variables 模板变量
     * @return 邮件日志ID
     */
    Long sendTemplateEmail(String to, String templateName, Map<String, Object> variables);

    /**
     * 使用模板文件发送邮件
     * @param to 收件人
     * @param templateName 模板名称（不包含后缀）
     * @param subject 邮件主题
     * @param variables 模板变量
     * @return 邮件日志ID
     */
    Long sendTemplateFileEmail(String to, String templateName, String subject, Map<String, Object> variables);

    /**
     * 批量发送邮件
     * @param toList 收件人列表
     * @param subject 主题
     * @param content 内容
     * @return 邮件日志ID列表
     */
    List<Long> batchSendEmail(List<String> toList, String subject, String content);

    /**
     * 创建邮件模板
     * @param template 模板对象
     * @return 创建的模板
     */
    EmailTemplate createTemplate(EmailTemplate template);

    /**
     * 更新邮件模板
     * @param template 模板对象
     * @return 是否更新成功
     */
    boolean updateTemplate(EmailTemplate template);

    /**
     * 根据模板代码获取模板
     * @param templateCode 模板代码
     * @return 模板对象
     */
    EmailTemplate getTemplateByCode(String templateCode);

    /**
     * 获取邮件日志
     * @param id 日志ID
     * @return 日志对象
     */
    EmailLog getEmailLog(Long id);

    /**
     * 发送面试邀请邮件
     * @param to 收件人
     * @param candidateRealName 候选人姓名
     * @param positionName 职位名称
     * @param interviewTime 面试时间
     * @param interviewLocation 面试地点
     * @param interviewerName 面试官姓名
     * @param contactInfo 联系信息
     * @return 邮件日志ID
     */
    Long sendInterviewInvitation(String to, String candidateRealName, String positionName, String interviewTime,
                               String interviewLocation, String interviewerName, String contactInfo);

    /**
     * 发送面试结果邮件
     * @param to 收件人
     * @param candidateName 候选人姓名
     * @param positionName 职位名称
     * @param result 面试结果
     * @param feedback 面试反馈
     * @param nextSteps 后续步骤
     * @return 邮件日志ID
     */
    Long sendInterviewResult(String to, String candidateName, String positionName, String result,
                           String feedback, String nextSteps);
}