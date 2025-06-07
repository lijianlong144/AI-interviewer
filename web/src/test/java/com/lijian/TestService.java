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

    }

}
