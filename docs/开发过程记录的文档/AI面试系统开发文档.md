# AI面试系统开发文档

## 一、项目概述

### 1.1 项目背景
AI面试系统是一个基于人工智能技术的在线面试平台，旨在帮助企业提高面试效率，为候选人提供更加公平、标准化的面试体验。本系统作为毕业设计项目，旨在探索AI技术在人力资源领域的应用。

### 1.2 项目目标
- 实现AI驱动的智能面试功能
- 提供题库管理和面试评估功能
- 生成详细的面试报告和分析
- 支持多角色（面试官、HR、候选人）使用
- 提供友好的用户界面和良好的用户体验

### 1.3 技术架构
- **后端**：Spring Boot 3 + Spring AI Alibaba + MyBatis Plus
- **前端**：Vue 3 + Element Plus + TypeScript
- **数据库**：MySQL 8.0 + Redis
- **AI服务**：阿里云通义千问（DashScope）
- **其他**：JWT认证、WebSocket（实时通信）
- **项目结构**：采用父子模块结构，便于功能分离和维护

## 二、功能需求分析

### 2.1 用户管理模块
- **用户注册/登录**
    - 支持候选人自主注册
    - HR账号由面试官创建
    - 支持邮箱验证（可选）
    - 密码加密存储

- **角色权限**
    - 面试官：系统全部功能
    - HR：面试管理、报告查看、题库管理
    - 候选人：参加面试、查看个人报告
    - 基于RBAC模型实现权限控制

### 2.2 题库管理模块
- **题目分类**
    - 技术题（TECHNICAL）：编程、算法、专业知识等
    - 行为题（BEHAVIORAL）：团队协作、压力处理、职业规划等
    - 情景题（SITUATIONAL）：模拟工作场景的问题解决

- **题目管理**
    - CRUD操作
    - 难度等级设置（1-5）
    - 标签管理（技术栈、职位类别等）
    - 参考答案和关键词设置
    - 批量导入导出功能

### 2.3 面试管理模块
- **面试创建**
    - HR创建面试安排
    - 生成面试房间号和访问链接
    - 设置面试时间、职位和要求
    - 邮件通知候选人（可选）

- **面试流程**
    - 候选人进入面试房间
    - AI面试官自动提问
    - 语音识别和文本记录
    - 实时答案分析
    - 面试过程录制（可选）

### 2.4 AI面试功能
- **智能提问**
    - 基于职位和候选人背景智能生成问题
    - 结合题库和AI生成混合出题
    - 追问和深入探讨
    - 难度自适应调整

- **实时分析**
    - 关键词匹配
    - 回答质量评分
    - 语义理解和分析
    - 情感分析（可选）
    - 说话速度和清晰度评估（可选）

### 2.5 报告生成模块
- **评分维度**
    - 技术能力
    - 沟通能力
    - 逻辑思维
    - 专业素养
    - 综合表现

- **报告内容**
    - 整体评分
    - 优劣势分析
    - 改进建议
    - AI推荐决策
    - 面试过程重点回顾

## 三、开发进度规划

### 第一阶段：基础架构搭建（3天）
1. **Day 1：环境配置**
    - ✅ 数据库创建和表结构
    - 完善Maven父子模块配置
    - 配置Spring AI Alibaba
    - 搭建基础项目结构

2. **Day 2：基础框架**
    - 实体类生成（Entity）
    - Mapper接口创建
    - 基础Service层
    - 通用工具类开发

3. **Day 3：认证体系**
    - JWT工具类
    - 登录注册接口
    - 权限拦截器
    - 统一响应处理

### 第二阶段：核心功能开发（7天）
4. **Day 4-5：用户管理**
    - 用户CRUD接口
    - 角色权限控制
    - 个人信息管理
    - 密码重置功能

5. **Day 6-7：题库管理**
    - 题目CRUD接口
    - 题目分类和标签
    - 批量导入功能
    - 题目搜索和筛选

6. **Day 8-10：面试管理**
    - 面试创建和调度
    - 面试状态管理
    - 面试房间功能
    - 面试记录存储

### 第三阶段：AI功能集成（5天）
7. **Day 11-12：AI接入**
    - DashScope API集成
    - Prompt工程设计
    - 上下文管理
    - 异常处理和降级策略

8. **Day 13-15：智能面试**
    - AI提问逻辑
    - 答案实时分析
    - 评分算法实现
    - 面试过程优化

### 第四阶段：前端开发（7天）
9. **Day 16-17：前端架构**
    - Vue3项目搭建
    - 路由和状态管理
    - UI组件库集成
    - 响应式布局设计

10. **Day 18-20：页面开发**
    - 登录注册页
    - 面试官后台页面
    - 面试房间页面
    - 报告展示页面

11. **Day 21-22：前后端联调**
    - 接口对接
    - 数据交互优化
    - Bug修复
    - 用户体验优化

### 第五阶段：优化部署（3天）
12. **Day 23-24：功能优化**
    - 性能优化
    - 用户体验改进
    - 安全加固
    - 兼容性测试

13. **Day 25：部署上线**
    - 服务器配置
    - 应用部署
    - 测试验证
    - 文档完善

## 四、详细开发顺序

### 4.1 后端开发顺序
1. **基础设施层**
   ```
   common模块
   ├── 统一响应Result
   ├── 全局异常处理
   ├── JWT工具类
   ├── Redis配置
   ├── 通用工具类
   └── 常量定义
   ```

2. **用户模块**
   ```
   user模块
   ├── Entity实体类
   │   ├── User
   │   ├── Role
   │   └── UserRole
   ├── Mapper接口
   ├── Service层
   │   ├── UserService
   │   ├── RoleService
   │   └── AuthService
   ├── Controller层
   │   ├── UserController
   │   ├── RoleController
   │   └── AuthController
   └── DTO/VO对象
   ```

3. **面试模块**
   ```
   interview模块
   ├── 题库管理
   │   ├── Question
   │   ├── QuestionCategory
   │   └── QuestionTag
   ├── 面试管理
   │   ├── Interview
   │   ├── InterviewRoom
   │   └── InterviewSchedule
   ├── 面试记录
   │   ├── InterviewRecord
   │   ├── InterviewQuestion
   │   └── InterviewAnswer
   └── 报告生成
       ├── InterviewReport
       ├── ScoreDetail
       └── RecommendationDetail
   ```

4. **AI核心模块**
   ```
   ai-core模块
   ├── AI服务接口
   │   ├── DashScopeService
   │   └── AIInterviewService
   ├── Prompt模板
   │   ├── QuestionPrompt
   │   ├── AnalysisPrompt
   │   └── ReportPrompt
   ├── 上下文管理
   │   ├── ContextManager
   │   └── MessageHistory
   └── 评分算法
       ├── ScoreCalculator
       ├── KeywordMatcher
       └── SemanticAnalyzer
   ```

### 4.2 前端开发顺序
1. **基础架构**
    - 项目初始化
    - 路由配置
    - Axios封装
    - 权限控制
    - 全局状态管理

2. **页面开发**
    - 登录注册
    - 用户中心
    - 题库管理
    - 面试管理
    - 面试房间
    - 报告查看
    - 数据统计（可选）

## 五、需要补充的内容

### 5.1 技术细节补充
1. **语音功能**
    - 集成Web Speech API进行语音识别
    - 语音转文字实时显示
    - 支持语音回放
    - 多语言支持（可选）

2. **视频功能（可选）**
    - WebRTC视频通话
    - 视频录制存储
    - 表情识别分析
    - 视频流加密

3. **向量数据库（建议）**
    - 使用Milvus或Elasticsearch
    - 存储问题和答案的向量
    - 实现语义相似度匹配
    - 快速检索相关问题

### 5.2 功能扩展建议
1. **简历解析**
    - PDF/Word简历上传
    - AI简历分析
    - 职位匹配度计算
    - 关键技能提取

2. **面试准备**
    - 模拟面试功能
    - 面试题练习
    - 答案优化建议
    - 行业面试指南

3. **数据分析**
    - 面试数据统计
    - 候选人画像分析
    - 面试效果评估
    - 可视化报表

### 5.3 安全性考虑
1. **数据安全**
    - 敏感信息加密
    - SQL注入防护
    - XSS攻击防护
    - CSRF防护
    - 数据脱敏处理

2. **接口安全**
    - 接口限流
    - 参数校验
    - 权限验证
    - 日志审计
    - API版本控制

## 六、关键技术实现

### 6.1 Spring AI Alibaba配置
```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwen-turbo
          temperature: 0.7
          top_p: 0.9
          max_tokens: 2000
```

### 6.2 WebSocket配置（面试实时通信）
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(interviewWebSocketHandler(), "/interview-ws/**")
                .setAllowedOrigins("*");
    }
    
    @Bean
    public WebSocketHandler interviewWebSocketHandler() {
        return new InterviewWebSocketHandler();
    }
}
```

### 6.3 Redis缓存策略
- 用户Token缓存（TTL: 24小时）
- 面试房间信息缓存（TTL: 面试持续时间）
- 热门题目缓存（TTL: 1小时）
- AI对话上下文缓存（TTL: 面试持续时间）
- 分布式锁实现（防止并发问题）

### 6.4 MyBatis Plus配置
```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
```

### 6.5 AI评分算法实现思路
```java
public class ScoreCalculator {
    // 技术能力评分（基于关键词匹配和语义相似度）
    public double calculateTechnicalScore(String answer, List<String> keywords, String standardAnswer) {
        double keywordScore = calculateKeywordMatchScore(answer, keywords);
        double semanticScore = calculateSemanticSimilarity(answer, standardAnswer);
        return keywordScore * 0.4 + semanticScore * 0.6;
    }
    
    // 沟通能力评分（基于语言流畅度、表达清晰度）
    public double calculateCommunicationScore(String answer) {
        // 实现逻辑
    }
    
    // 逻辑思维评分（基于答案结构、因果关系）
    public double calculateLogicalScore(String answer) {
        // 实现逻辑
    }
}
```

## 七、测试计划

### 7.1 单元测试
- Service层测试
- 工具类测试
- AI接口测试
- 使用JUnit 5 + Mockito框架

### 7.2 集成测试
- 接口测试（使用RestAssured）
- 业务流程测试
- 性能测试（使用JMeter）
- 数据库交互测试

### 7.3 用户测试
- 功能可用性测试
- 用户体验测试
- 压力测试
- 浏览器兼容性测试

## 八、部署方案

### 8.1 开发环境
- 本地开发测试
- Docker容器化
- 开发环境配置文件
- 热部署配置

### 8.2 生产环境
- 云服务器部署
- Nginx反向代理
- HTTPS证书配置
- 数据库备份策略
- CI/CD流程（可选）

## 九、项目风险及应对

### 9.1 技术风险
- AI接口调用限制：实现缓存和降级方案
- 并发面试处理：使用消息队列异步处理
- 数据存储压力：分表分库设计
- 网络延迟问题：优化前端交互体验

### 9.2 进度风险
- 功能过多：优先实现核心功能
- 技术难点：提前技术预研
- 联调问题：预留充足联调时间
- 资源限制：合理规划开发资源

## 十、项目交付物

1. **源代码**
    - 后端完整代码
    - 前端完整代码
    - 数据库脚本
    - 配置文件模板

2. **文档**
    - 需求文档
    - 设计文档
    - API文档
    - 部署文档
    - 使用手册
    - 开发日志

3. **演示**
    - 功能演示视频
    - PPT展示文稿
    - 在线演示环境
    - 测试账号