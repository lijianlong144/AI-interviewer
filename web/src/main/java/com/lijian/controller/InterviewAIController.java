package com.lijian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lijian.entity.Interview;
import com.lijian.entity.InterviewQuestionEntity;
import com.lijian.entity.Position;
import com.lijian.entity.Resume;
import com.lijian.entity.InterviewAnswerEntity;
import com.lijian.result.Result;
import com.lijian.service.InterviewQuestionService;
import com.lijian.service.InterviewService;
import com.lijian.service.PositionService;
import com.lijian.service.ResumeService;
import com.lijian.service.InterviewAnswerService;
import com.lijian.entity.Application;
import com.lijian.service.ApplicationService;
import com.lijian.dto.SaveAnswerDTO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
//import org.springframework.http.codec.ServerSentEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.alibaba.cloud.ai.memory.redis.RedisChatMemoryRepository;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * 面试管理控制器
 *
 * @author lijian
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/ai/interview")
@Validated
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:8080"}, 
             allowCredentials = "true", 
             allowedHeaders = "*", 
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class InterviewAIController {
    private static final Logger log = LoggerFactory.getLogger(InterviewAIController.class);
    private final ChatClient chatClient;
    private final BeanOutputConverter<InterviewQuestionEntity> converter;
    private final String format;
    private final MessageWindowChatMemory messageWindowChatMemory;
    private final int MAX_MESSAGES = 100;

    @Resource
    private PositionService positionService;

    @Resource
    private ResumeService resumeService;

    @Resource
    private InterviewService interviewService;

    @Resource
    private InterviewQuestionService interviewQuestionService;
    
    @Resource
    private InterviewAnswerService interviewAnswerService;

    @Resource
    private ApplicationService applicationService;

    public InterviewAIController(ChatClient.Builder builder, RedisChatMemoryRepository redisChatMemoryRepository) {
        this.converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<InterviewQuestionEntity>() {}
        );
        this.format = converter.getFormat();
        
        this.messageWindowChatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(redisChatMemoryRepository)
                .maxMessages(MAX_MESSAGES)
                .build();
                
        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(messageWindowChatMemory)
                                .build()
                )
                .build();
                
        log.info("InterviewAIController initialized with format: {}", format);
    }


    /**
     * 流式生成AI面试问题
     * @param interviewId 面试ID
     * @param questionType 问题类型
     * @param interviewQuestionId 面试问题ID
     * @param hasContext 是否已有上下文
     * @return SSE响应
     */
    @GetMapping(value = "/generate-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateAIQuestionStream(
            @RequestParam(value = "interviewId") Long interviewId,
            @RequestParam(value = "questionType") String questionType,
            @RequestParam(value = "interviewQuestionId") Long interviewQuestionId,
            @RequestParam(value = "hasContext", defaultValue = "false") boolean hasContext) {
        
        log.info("生成AI面试问题: interviewId={}, questionType={}, interviewQuestionId={}, hasContext={}", 
                interviewId, questionType, interviewQuestionId, hasContext);
        
        // 创建收集器，用于收集完整响应
        AtomicReference<StringBuilder> collector = new AtomicReference<>(new StringBuilder());
        // 使用传入的问题ID，而不是生成新的ID
        AtomicReference<Long> generatedQuestionId = new AtomicReference<>(interviewQuestionId);
        
        return Flux.defer(() -> {
            try {
                // 1. 获取面试信息和相关数据
                Interview interview = getInterviewById(interviewId);
                if (interview == null) {
                    return Flux.error(new RuntimeException("面试不存在"));
                }
                
                // 使用面试编号作为会话ID
                final String conversationId;
                if (interview.getInterviewNo() == null || interview.getInterviewNo().isEmpty()) {
                    conversationId = "interview_" + interviewId;
                    log.warn("面试编号为空，使用默认会话ID: {}", conversationId);
                } else {
                    conversationId = interview.getInterviewNo();
                }
                
                Position position = getPositionById(interview.getPositionId());
                Resume resume = getResumeFromApplication(interview.getApplicationId());
                
                // 2. 构建提示词
                String systemPrompt = buildSystemPrompt();
                String userPrompt = buildUserPrompt(position, resume, questionType, hasContext);
                
                // 3. 流式调用AI模型，使用会话记忆
                return chatClient.prompt()
                        .system(systemPrompt)
                        .user(userPrompt)
                        .advisors(a -> a.param(CONVERSATION_ID, conversationId))
                        .stream()
                        .content()
                        .doOnNext(part -> collector.get().append(part))
                        .map(content -> ServerSentEvent.<String>builder().data(content).build())
                        .doOnComplete(() -> {
                            try {
                                // 清理和格式化JSON
                                String fullResponse = collector.get().toString();
                                String finalJson = cleanupJsonResponse(fullResponse);
                                log.info("AI生成的面试问题JSON: {}", finalJson);
                                
                                // 转换为实体并保存
                                InterviewQuestionEntity question = converter.convert(finalJson);
                                
                                // 设置面试ID和其他必要字段
                                question.setInterviewId(interviewId);
                                question.setCreateTime(new Date());
                                question.setUpdateTime(new Date());
                                question.setIsDeleted(0);
                                
                                // 使用传入的interviewQuestionId，而不是生成新的ID
                                question.setInterviewQuestionId(interviewQuestionId);
                                
                                // 保存到数据库
                                interviewQuestionService.saveQuestion(question);
                                log.info("成功保存面试问题: interviewId={}, interviewQuestionId={}", interviewId, interviewQuestionId);
                            } catch (Exception e) {
                                log.error("处理AI生成问题失败", e);
                            }
                        })
                        .onErrorResume(e -> handleError(e))
                        .concatWith(sendCompletionEvent(collector, generatedQuestionId));
            } catch (Exception e) {
                log.error("生成AI面试问题失败", e);
                return Flux.just(ServerSentEvent.<String>builder()
                        .event("error")
                        .data("处理失败: " + e.getMessage())
                        .build());
            }
        });
    }
    
    /**
     * 根据ID获取面试信息
     */
    private Interview getInterviewById(Long interviewId) {
        Interview interview = interviewService.getById(interviewId);
        if (interview == null) {
            log.error("面试不存在: {}", interviewId);
            return null;
        }
        
        Long positionId = interview.getPositionId();
        Long applicationId = interview.getApplicationId();
        
        if (positionId == null) {
            log.error("面试记录中没有职位ID: {}", interviewId);
            return null;
        }
        
        if (applicationId == null) {
            log.error("面试记录中没有申请ID: {}", interviewId);
            return null;
        }
        
        return interview;
    }
    
    /**
     * 根据ID获取职位信息
     */
    private Position getPositionById(Long positionId) {
        Position position = positionService.getById(positionId);
        if (position == null) {
            log.error("职位不存在: {}", positionId);
            throw new RuntimeException("职位不存在");
        }
        return position;
    }
    
    /**
     * 从申请中获取简历信息
     */
    private Resume getResumeFromApplication(Long applicationId) {
        Application application = applicationService.getById(applicationId);
        if (application == null) {
            log.error("申请不存在: {}", applicationId);
            throw new RuntimeException("申请不存在");
        }
        
        Long resumeId = application.getResumeId();
        if (resumeId == null) {
            log.error("申请中没有简历ID: {}", applicationId);
            throw new RuntimeException("申请中没有简历ID");
        }
        
        return getResumeById(resumeId);
    }
    
    /**
     * 根据ID获取简历信息
     */
    private Resume getResumeById(Long resumeId) {
        Resume resume = resumeService.getById(resumeId);
        if (resume == null) {
            log.error("简历不存在: {}", resumeId);
            throw new RuntimeException("简历不存在");
        }
        return resume;
    }
    
    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        return """
        You are an experienced professional interviewer with expertise in conducting technical interviews.
        Your role is to generate thoughtful, challenging, and relevant interview questions based on the job requirements and candidate's resume.
        
        Guidelines:
        1. Generate only ONE question per request
        2. Ensure the question is appropriate for the specified question type
        3. Tailor the question to match both the job requirements and candidate's background
        4. Avoid generating multiple questions disguised as a single question
        5. Structure your response as a valid JSON object according to the specified format
        6. Do not include any explanations or additional text outside the JSON structure
        
        Remember that your questions will be used in a real interview scenario, so they should be clear, concise, and professionally phrased.
        """;
    }
    
    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(Position position, Resume resume, String questionType, boolean hasContext) {
        String jobRequirement = position.getRequirements() != null ? 
            position.getRequirements() : position.getDescription();
            
        if (!hasContext) {
            // 第一次对话，包含简历和职位信息
            StringBuilder resumeBuilder = new StringBuilder();
            resumeBuilder.append("姓名: ").append(resume.getRealName()).append("\n");
            resumeBuilder.append("性别: ").append(resume.getGender()).append("\n");
            resumeBuilder.append("学历: ").append(resume.getHighestEducation()).append("\n");
            resumeBuilder.append("毕业院校: ").append(resume.getGraduationSchool()).append("\n");
            resumeBuilder.append("专业: ").append(resume.getMajor()).append("\n");
            resumeBuilder.append("工作年限: ").append(resume.getWorkYears()).append("\n");
            resumeBuilder.append("技能: ").append(resume.getSkills()).append("\n");
            resumeBuilder.append("工作经历: ").append(resume.getWorkExperience()).append("\n");
            
            String resumeText = resumeBuilder.toString();
            
            return String.format("""
            请根据以下内容生成一道 %s 类型的面试题：
            
            岗位要求：
            %s
            
            候选人简历：
            %s
            
            format: 以纯文本输出 json，请不要包含任何多余的文字;
            outputExample: {
              "questionContent": "这里是面试问题内容",
              "questionType": "%s",
              "aiGenerated": 1,
              "sequence": 1
            }
            """, questionType, jobRequirement, resumeText, questionType.toUpperCase());
        } else {
            // 已有上下文，只需要问题类型
            return String.format("""
            请生成一道新的 %s 类型的面试题，注意不要与之前的问题重复。
            
            format: 以纯文本输出 json，请不要包含任何多余的文字;
            outputExample: {
              "questionContent": "这里是面试问题内容",
              "questionType": "%s",
              "aiGenerated": 1,
              "sequence": 1
            }
            """, questionType, questionType.toUpperCase());
        }
    }
    
    /**
     * 清理和格式化JSON响应
     */
    private String cleanupJsonResponse(String response) {
        String finalJson = response
                .replaceAll("\\n", "")
                .replaceAll("\\s+", " ")
                .replaceAll("\"\\s*:", "\":")
                .replaceAll(":\\s*\"", ":\"");
        
        // 去除可能的```json前缀和```后缀
        if (finalJson.contains("```json")) {
            finalJson = finalJson.replaceAll("```json", "");
        }
        if (finalJson.contains("```")) {
            finalJson = finalJson.replaceAll("```", "");
        }
        
        return finalJson;
    }
    
    /**
     * 生成唯一ID
     */
    private long generateUniqueId() {
        long timestamp = System.currentTimeMillis();
        int randomNum = (int)(Math.random() * 90000) + 10000; // 生成10000-99999之间的随机数
        return Long.parseLong(timestamp + "" + randomNum);
    }
    
    /**
     * 处理错误
     */
    private Flux<ServerSentEvent<String>> handleError(Throwable e) {
        log.error("生成AI面试问题失败", e);
        return Flux.just(ServerSentEvent.<String>builder()
                .event("error")
                .data("处理失败: " + e.getMessage())
                .build());
    }
    
    /**
     * 发送完成事件
     */
    private Flux<ServerSentEvent<String>> sendCompletionEvent(AtomicReference<StringBuilder> collector, AtomicReference<Long> generatedQuestionId) {
        return Flux.defer(() -> {
            try {
                // 获取问题ID
                Long uniqueId = generatedQuestionId.get();
                if (uniqueId == null) {
                    log.error("在sendCompletionEvent中未找到问题ID");
                    return Flux.just(ServerSentEvent.<String>builder()
                            .event("error")
                            .data("处理失败: 未找到问题ID")
                            .build());
                }
                
                log.info("发送完成事件，使用问题ID: {}", uniqueId);
                
                // 返回包含interviewQuestionId的成功消息
                String successResponse = String.format("{\"status\":\"success\",\"message\":\"保存成功\",\"interview_question_id\":%d}", uniqueId);
                
                return Flux.just(ServerSentEvent.<String>builder()
                        .event("done")
                        .data(successResponse)
                        .build());
            } catch (Exception e) {
                log.error("发送完成事件失败", e);
                return Flux.just(ServerSentEvent.<String>builder()
                        .event("error")
                        .data("处理失败: " + e.getMessage())
                        .build());
            }
        });
    }

    /**
     * 保存面试回答
     * @param saveAnswerDTO 保存面试回答的数据传输对象
     * @return 结果
     */
    @PostMapping("/save-answer")
    public Result<Void> saveAnswer(@RequestBody @Valid SaveAnswerDTO saveAnswerDTO) {
        
        log.info("保存面试回答: interviewId={}, interviewQuestionId={}, thinkDuration={}, answerDuration={}", 
                saveAnswerDTO.getInterviewId(), saveAnswerDTO.getInterviewQuestionId(), 
                saveAnswerDTO.getThinkDuration(), saveAnswerDTO.getAnswerDuration());
        
        try {
            // 1. 检查面试是否存在
            Interview interview = interviewService.getById(saveAnswerDTO.getInterviewId());
            if (interview == null) {
                return Result.error("面试不存在");
            }
            
            // 2. 检查问题是否存在
            InterviewQuestionEntity question = interviewQuestionService.lambdaQuery()
                .eq(InterviewQuestionEntity::getInterviewQuestionId, saveAnswerDTO.getInterviewQuestionId())
                .eq(InterviewQuestionEntity::getInterviewId, saveAnswerDTO.getInterviewId())
                .one();
                
            if (question == null) {
                return Result.error("面试问题不存在");
            }
            
            // 3. 创建面试回答实体
            InterviewAnswerEntity answer = new InterviewAnswerEntity();
            answer.setInterviewId(saveAnswerDTO.getInterviewId());
            answer.setInterviewQuestionId(saveAnswerDTO.getInterviewQuestionId()); 
            answer.setAnswerContent(saveAnswerDTO.getAnswerContent());
            answer.setThinkDuration(saveAnswerDTO.getThinkDuration());  // 设置思考时长
            answer.setAnswerDuration(saveAnswerDTO.getAnswerDuration());  // 设置回答时长
            answer.setAnswerTime(new Date());
            answer.setCreateTime(new Date());
            answer.setUpdateTime(new Date());
            answer.setIsDeleted(0);
            
            // 4. 保存面试回答
            interviewAnswerService.saveAnswer(answer);
            
            return Result.success();
        } catch (Exception e) {
            log.error("保存面试回答失败: {}", e.getMessage(), e);
            return Result.error("保存回答失败: " + e.getMessage());
        }
    }
}