package com.lijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lijian.entity.EmailLog;
import com.lijian.entity.EmailTemplate;
import com.lijian.mapper.EmailLogMapper;
import com.lijian.mapper.EmailTemplateMapper;
import com.lijian.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl extends ServiceImpl<EmailLogMapper, EmailLog> implements EmailService {
    @Autowired
    @Qualifier("emailTemplateEngine")
    private SpringTemplateEngine emailTemplateEngine;
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private EmailTemplateMapper emailTemplateMapper;

    @Value("${spring.mail.username}")
    private String sender;

    @Value("${spring.mail.nickname:AI面试系统}")
    private String nickname;

    @Override
    @Transactional
    public Long sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(nickname + "<" + sender + ">");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);

            // 记录邮件日志
            EmailLog log = new EmailLog();
            log.setToEmail(to);
            log.setSubject(subject);
            log.setContent(content);
            log.setStatus("SUCCESS");
            log.setSendTime(LocalDateTime.now());
            log.setCreateTime(LocalDateTime.now());
            save(log);

            return log.getId();
        } catch (Exception e) {
            // 记录失败日志
            EmailLog log = new EmailLog();
            log.setToEmail(to);
            log.setSubject(subject);
            log.setContent(content);
            log.setStatus("FAILED");
            log.setErrorMsg(e.getMessage());
            log.setSendTime(LocalDateTime.now());
            log.setCreateTime(LocalDateTime.now());
            save(log);

            throw new RuntimeException("发送邮件失败", e);
        }
    }

    @Override
    @Transactional
    public Long sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(nickname + "<" + sender + ">");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);

            // 记录邮件日志
            EmailLog log = new EmailLog();
            log.setToEmail(to);
            log.setSubject(subject);
            log.setContent(htmlContent);
            log.setStatus("SUCCESS");
            log.setSendTime(LocalDateTime.now());
            log.setCreateTime(LocalDateTime.now());
            save(log);

            return log.getId();
        } catch (Exception e) {
            // 记录失败日志
            EmailLog log = new EmailLog();
            log.setToEmail(to);
            log.setSubject(subject);
            log.setContent(htmlContent);
            log.setStatus("FAILED");
            log.setErrorMsg(e.getMessage());
            log.setSendTime(LocalDateTime.now());
            log.setCreateTime(LocalDateTime.now());
            save(log);

            throw new RuntimeException("发送HTML邮件失败", e);
        }
    }

    @Override
    @Transactional
    public Long sendTemplateEmail(String to, String templateCode, Map<String, Object> variables) {
        // 获取模板
        EmailTemplate template = getTemplateByCode(templateCode);
        if (template == null) {
            throw new RuntimeException("邮件模板不存在: " + templateCode);
        }

        try {
            // 确保sendDate变量存在
            if (!variables.containsKey("sendDate")) {
                // 格式化当前日期为易读格式
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
                variables.put("sendDate", LocalDateTime.now().format(formatter));
            }

            // 渲染模板内容
            Context context = new Context();
            context.setVariables(variables);
            String content = emailTemplateEngine.process(template.getContent(), context);

            // 渲染邮件主题
            Context subjectContext = new Context();
            subjectContext.setVariables(variables);
            String subject = emailTemplateEngine.process(template.getSubject(), subjectContext);

            // 发送邮件
            return sendHtmlEmail(to, subject, content);
        } catch (Exception e) {
            throw new RuntimeException("处理邮件模板失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 使用模板文件发送邮件
     * @param to 收件人
     * @param templateName 模板名称（不包含后缀）
     * @param subject 邮件主题
     * @param variables 模板变量
     * @return 邮件日志ID
     */
    @Override
    @Transactional
    public Long sendTemplateFileEmail(String to, String templateName, String subject, Map<String, Object> variables) {
        try {
            // 确保sendDate变量存在
            if (!variables.containsKey("sendDate")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
                variables.put("sendDate", LocalDateTime.now().format(formatter));
            }

            // 渲染模板内容
            Context context = new Context();
            context.setVariables(variables);

            // 打印调试信息
            System.out.println("=== 邮件模板调试信息 ===");
            System.out.println("模板名称: " + templateName);
            System.out.println("变量: " + variables);

            // 只使用emailTemplateEngine
            String content = emailTemplateEngine.process(templateName, context);

            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("无法渲染模板: " + templateName);
            }

            System.out.println("渲染后内容前200字符: " + content.substring(0, Math.min(200, content.length())));
            System.out.println("========================");

            // 发送邮件
            return sendHtmlEmail(to, subject, content);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("处理邮件模板文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public List<Long> batchSendEmail(List<String> toList, String subject, String content) {
        List<Long> logIds = new ArrayList<>();
        for (String to : toList) {
            Long logId = sendSimpleEmail(to, subject, content);
            logIds.add(logId);
        }
        return logIds;
    }

    @Override
    @Transactional
    public EmailTemplate createTemplate(EmailTemplate template) {
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        emailTemplateMapper.insert(template);
        return template;
    }

    @Override
    public boolean updateTemplate(EmailTemplate template) {
        template.setUpdateTime(LocalDateTime.now());
        return emailTemplateMapper.updateById(template) > 0;
    }

    @Override
    public EmailTemplate getTemplateByCode(String templateCode) {
        LambdaQueryWrapper<EmailTemplate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmailTemplate::getTemplateCode, templateCode);
        return emailTemplateMapper.selectOne(queryWrapper);
    }

    @Override
    public EmailLog getEmailLog(Long id) {
        return getById(id);
    }

    @Override
    @Transactional
    public Long sendInterviewInvitation(String to, String candidateRealName, String positionName, String interviewTime,
                                      String interviewLocation, String interviewerName, String contactInfo) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("candidateRealName", candidateRealName);
        variables.put("positionName", positionName);
        variables.put("interviewTime", interviewTime);
        
        // 从interviewLocation中提取房间号，或者使用默认值
        String roomCode = interviewLocation;
        if (interviewLocation != null && interviewLocation.contains("房间号")) {
            String[] parts = interviewLocation.split("房间号");
            if (parts.length > 1) {
                roomCode = parts[1].trim();
            }
        }
        variables.put("roomCode", roomCode);
        
        // 格式化当前日期为易读格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
        variables.put("sendDate", LocalDateTime.now().format(formatter));
        
        variables.put("interviewLocation", interviewLocation);
        variables.put("companyName", "利安思科技");
        variables.put("interviewerName", interviewerName);
        variables.put("contactInfo", contactInfo);

        // 打印调试信息
        System.out.println("发送面试邀请邮件:");
        System.out.println("收件人: " + to);
        System.out.println("变量: " + variables);

        // 使用模板文件发送邮件
        String subject = "面试邀请 - " + positionName;
        return sendTemplateFileEmail(to, "interview-invitation", subject, variables);
    }

    @Override
    @Transactional
    public Long sendInterviewResult(String to, String candidateName, String positionName, String result,
                                  String feedback, String nextSteps) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("candidateName", candidateName);
        variables.put("positionName", positionName);
        variables.put("result", result);
        variables.put("feedback", feedback);
        variables.put("nextSteps", nextSteps);
        variables.put("companyName", "利安思科技");

        // 使用模板文件发送邮件
        String subject = "【利安思科技】面试结果通知 - " + positionName;
        return sendTemplateFileEmail(to, "interview-result", subject, variables);
    }
}