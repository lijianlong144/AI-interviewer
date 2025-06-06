package com.lijian.controller;

import com.lijian.entity.EmailLog;
import com.lijian.entity.EmailTemplate;
import com.lijian.result.Result;
import com.lijian.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 邮件控制器
 */
@RestController
@RequestMapping("/api/email")
@Validated
public class EmailController {

    @Autowired
    private EmailService emailService;

    /**
     * 发送简单邮件
     */
    @PostMapping("/simple")
    public Result<Long> sendSimpleEmail(@RequestParam String to,
                                       @RequestParam String subject,
                                       @RequestParam String content) {
        Long logId = emailService.sendSimpleEmail(to, subject, content);
        return Result.success(logId);
    }

    /**
     * 发送HTML邮件
     */
    @PostMapping("/html")
    public Result<Long> sendHtmlEmail(@RequestParam String to,
                                     @RequestParam String subject,
                                     @RequestParam String htmlContent) {
        Long logId = emailService.sendHtmlEmail(to, subject, htmlContent);
        return Result.success(logId);
    }

    /**
     * 使用模板发送邮件
     */
    @PostMapping("/template")
    public Result<Long> sendTemplateEmail(@RequestParam String to,
                                         @RequestParam String templateCode,
                                         @RequestBody Map<String, Object> variables) {
        Long logId = emailService.sendTemplateEmail(to, templateCode, variables);
        return Result.success(logId);
    }

    /**
     * 批量发送邮件
     */
    @PostMapping("/batch")
    public Result<List<Long>> batchSendEmail(@RequestBody List<String> toList,
                                           @RequestParam String subject,
                                           @RequestParam String content) {
        List<Long> logIds = emailService.batchSendEmail(toList, subject, content);
        return Result.success(logIds);
    }

    /**
     * 创建邮件模板
     */
    @PostMapping("/template/create")
    public Result<EmailTemplate> createTemplate(@Valid @RequestBody EmailTemplate template) {
        EmailTemplate createdTemplate = emailService.createTemplate(template);
        return Result.success(createdTemplate);
    }

    /**
     * 更新邮件模板
     */
    @PutMapping("/template")
    public Result<Boolean> updateTemplate(@Valid @RequestBody EmailTemplate template) {
        boolean result = emailService.updateTemplate(template);
        return Result.success(result);
    }

    /**
     * 获取邮件模板
     */
    @GetMapping("/template/{templateCode}")
    public Result<EmailTemplate> getTemplateByCode(@PathVariable("templateCode") String templateCode) {
        EmailTemplate template = emailService.getTemplateByCode(templateCode);
        return Result.success(template);
    }

    /**
     * 获取邮件日志
     */
    @GetMapping("/log/{id}")
    public Result<EmailLog> getEmailLog(@PathVariable("id") Long id) {
        EmailLog log = emailService.getEmailLog(id);
        return Result.success(log);
    }

    /**
     * 发送面试邀请邮件
     */
    @PostMapping("/interview-invitation")
    public Result<Long> sendInterviewInvitation(@RequestParam String to,
                                              @RequestParam String candidateName,
                                              @RequestParam String positionName,
                                              @RequestParam String interviewTime,
                                              @RequestParam String interviewLocation,
                                              @RequestParam String interviewerName,
                                              @RequestParam String contactInfo) {
        Long logId = emailService.sendInterviewInvitation(to, candidateName, positionName, interviewTime,
                interviewLocation, interviewerName, contactInfo);
        return Result.success(logId);
    }

    /**
     * 发送面试结果邮件
     */
    @PostMapping("/interview-result")
    public Result<Long> sendInterviewResult(@RequestParam String to,
                                          @RequestParam String candidateName,
                                          @RequestParam String positionName,
                                          @RequestParam String result,
                                          @RequestParam String feedback,
                                          @RequestParam(required = false) String nextSteps) {
        Long logId = emailService.sendInterviewResult(to, candidateName, positionName, result, feedback, nextSteps);
        return Result.success(logId);
    }
}