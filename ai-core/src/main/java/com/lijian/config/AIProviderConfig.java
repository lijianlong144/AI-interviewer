package com.lijian.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * AI服务提供商配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIProviderConfig {

    /**
     * AI提供商配置映射
     * key: 提供商名称（dashscope, openai, claude等）
     * value: 提供商配置
     */
    private Map<String, ProviderConfig> providers;

    /**
     * 默认提供商
     */
    private String defaultProvider = "dashscope";

    @Data
    public static class ProviderConfig {
        /**
         * API密钥
         */
        private String apiKey;

        /**
         * API基础URL（用于兼容API）
         */
        private String baseUrl;

        /**
         * 模型名称
         */
        private String model;

        /**
         * 温度参数
         */
        private Double temperature = 0.7;

        /**
         * 最大token数
         */
        private Integer maxTokens = 2000;

        /**
         * 是否启用
         */
        private Boolean enabled = true;
    }
}