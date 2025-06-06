package com.lijian;


import com.lijian.entity.SysUser;
import com.lijian.service.EmailService;
import com.lijian.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class TestService {
    @Autowired
    EmailService emailService;
    @Autowired
    SysUserService userService;
    @Autowired
    private JavaMailSender mailSender;
    @Test
    public void register () {



        SysUser user = new SysUser();
        user.setUsername("lijianlong1314");
        user.setPassword("testPassword");
        user.setEmail("lijianlong1314@qq.com");

        SysUser register = userService.register(user, null);
        System.out.printf("注册用户: %s, ID: %d%n", register.getUsername(), register.getId());
    }

    @Test
    public void getByUsername(){

        SysUser user = userService.getByUsername("lijian");
        if (user != null) {
            System.out.printf("用户信息: %s, ID: %d%n", user.getUsername(), user.getId());
        } else {
            System.out.println("用户不存在");
        }



    }

    @Test
    public void sendSimpleMail() throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();
        // 配置发送者邮箱
        message.setFrom("lijianlong144@foxmail.com");
        // 配置接受者邮箱
        message.setTo("lijianlong1314@outlook.com");
        // 配置邮件主题
        message.setSubject("主题：你好强");
        // 配置邮件内容
        message.setText("哈哈哈哈好吧");

        System.out.println(message+"邮件发送成功");
        // 发送邮件
        mailSender.send(message);
    }
    @Test
    public void sendSimpleMail001() throws Exception {
        Map<String,Object> vars = new HashMap<>();
        vars.put("companyName", "linaus科技有限公司");
        vars.put("positionName", "Java开发工程师");
        vars.put("candidateRealName", "张三");
        vars.put("interviewTime", "2025-06-10 10:00");
        vars.put("interviewLocation", "线上面试，房间号 736747");
        vars.put("interviewerName", "李HR");
        vars.put("sendDate", "2025-06-06");
        vars.put("roomCode", "736747");
        vars.put("contactInfo", "hr@example.com");

        emailService.sendTemplateEmail("lijianlong1314@outlook.com", "INTERVIEW_INVITATION", vars);
    }

    @Test
    public void sendSimpleMail002() throws Exception {
        Map<String,Object> vars = new HashMap<>();
        vars.put("companyName", "linaus科技有限公司");
        vars.put("positionName", "Java开发工程师");
        vars.put("candidateRealName", "张三");
        vars.put("interviewTime", "2025-06-10 10:00");
        // 注意这里，您的模板中使用的是roomCode而不是interviewLocation
        vars.put("roomCode", "736747");  // 添加这个变量
        vars.put("interviewerName", "李HR");
        vars.put("contactInfo", "hr@example.com");
        // 添加sendDate变量，格式化为更友好的日期格式
        vars.put("sendDate", "2025年6月6日");

        emailService.sendTemplateEmail("lijianlong1314@outlook.com", "INTERVIEW_INVITATION", vars);
    }


}
