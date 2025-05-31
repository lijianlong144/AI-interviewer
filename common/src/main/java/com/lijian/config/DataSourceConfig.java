package com.lijian.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 数据源配置类
 * 
 * @author lijian
 * @since 1.0.0
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {
    
    // 数据源配置在application.yml中，这里不需要额外配置
    // Spring Boot会自动配置数据源
    
} 