package com.lijian.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.nio.charset.StandardCharsets;

@Configuration
public class ThymeleafConfig {

    /**
     * 邮件模板引擎
     * 用于处理HTML邮件模板
     */
    @Bean
    @Primary
    public SpringTemplateEngine emailTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        // 添加字符串模板解析器（用于处理数据库中的模板）
        templateEngine.addTemplateResolver(stringTemplateResolver());
        // 添加文件模板解析器（用于处理模板文件）
        templateEngine.addTemplateResolver(emailTemplateResolver());
        return templateEngine;
    }

    /**
     * 字符串模板解析器
     * 用于处理字符串形式的模板内容（如数据库中存储的模板）
     */
    @Bean
    public StringTemplateResolver stringTemplateResolver() {
        StringTemplateResolver templateResolver = new StringTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);
        templateResolver.setOrder(2); // 优先级低于文件模板
        return templateResolver;
    }

    /**
     * 邮件模板解析器
     * 用于解析classpath:/templates/mail/目录下的HTML模板文件
     */
    @Bean
    public ITemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/mail/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCacheable(false);
        templateResolver.setOrder(1); // 优先级高于字符串模板
        templateResolver.setCheckExistence(true);
        
        // 打印调试信息
        System.out.println("模板前缀: " + templateResolver.getPrefix());
        System.out.println("模板后缀: " + templateResolver.getSuffix());
        System.out.println("模板模式: " + templateResolver.getTemplateMode());
        System.out.println("模板编码: " + templateResolver.getCharacterEncoding());
        
        return templateResolver;
    }
}