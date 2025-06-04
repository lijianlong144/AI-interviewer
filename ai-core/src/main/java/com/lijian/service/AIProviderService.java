package com.lijian.service;

import com.lijian.dto.AIRequest;
import com.lijian.dto.AIResponse;
import reactor.core.publisher.Flux;

/**
 * AI服务提供商接口
 */
public interface AIProviderService {

    /**
     * 发送消息到AI
     */
    AIResponse sendMessage(AIRequest request);

    /**
     * 流式发送消息
     */
    Flux<String> streamMessage(AIRequest request);

    /**
     * 获取提供商名称
     */
    String getProviderName();

    /**
     * 检查服务是否可用
     */
    boolean isAvailable();
}