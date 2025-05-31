-- 创建数据库
CREATE DATABASE ai_interviewer DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE ai_interviewer;

-- 1. 用户表
CREATE TABLE `sys_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(255) NOT NULL COMMENT '密码',
    `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
    `role` varchar(20) NOT NULL DEFAULT 'CANDIDATE' COMMENT '角色：INTERVIEWER/HR/CANDIDATE',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_phone` (`phone`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色表
CREATE TABLE `sys_role` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name` varchar(50) NOT NULL COMMENT '角色名称',
    `role_code` varchar(50) NOT NULL COMMENT '角色编码',
    `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 用户角色关联表
CREATE TABLE `sys_user_role` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_user_role_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 4. 题目分类表
CREATE TABLE `t_question_category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `category_name` varchar(50) NOT NULL COMMENT '分类名称',
    `category_code` varchar(50) NOT NULL COMMENT '分类编码',
    `description` varchar(255) DEFAULT NULL COMMENT '分类描述',
    `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类表';

-- 5. 题目标签表
CREATE TABLE `t_question_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `tag_name` varchar(50) NOT NULL COMMENT '标签名称',
    `category_id` bigint DEFAULT NULL COMMENT '所属分类ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    CONSTRAINT `fk_tag_category_id` FOREIGN KEY (`category_id`) REFERENCES `t_question_category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目标签表';

-- 6. 题库表
CREATE TABLE `t_question` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `title` varchar(500) NOT NULL COMMENT '题目标题',
    `content` text COMMENT '题目详细内容',
    `category_id` bigint NOT NULL COMMENT '分类ID',
    `difficulty` tinyint NOT NULL DEFAULT '3' COMMENT '难度1-5',
    `position` varchar(100) DEFAULT NULL COMMENT '适用职位',
    `standard_answer` text COMMENT '参考答案',
    `keywords` varchar(500) DEFAULT NULL COMMENT '关键词，逗号分隔',
    `creator_id` bigint NOT NULL COMMENT '创建人ID',
    `use_count` int DEFAULT '0' COMMENT '使用次数',
    `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_difficulty` (`difficulty`),
    KEY `idx_position` (`position`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_question_category_id` FOREIGN KEY (`category_id`) REFERENCES `t_question_category` (`id`),
    CONSTRAINT `fk_question_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- 7. 题目标签关联表
CREATE TABLE `t_question_tag_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `question_id` bigint NOT NULL COMMENT '题目ID',
    `tag_id` bigint NOT NULL COMMENT '标签ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_tag` (`question_id`,`tag_id`),
    KEY `idx_tag_id` (`tag_id`),
    CONSTRAINT `fk_relation_question_id` FOREIGN KEY (`question_id`) REFERENCES `t_question` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_relation_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `t_question_tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目标签关联表';

-- 8. 面试表
CREATE TABLE `t_interview` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '面试ID',
    `interview_no` varchar(50) NOT NULL COMMENT '面试编号',
    `title` varchar(100) NOT NULL COMMENT '面试标题',
    `position` varchar(100) NOT NULL COMMENT '应聘职位',
    `description` varchar(500) DEFAULT NULL COMMENT '面试描述',
    `candidate_id` bigint NOT NULL COMMENT '候选人ID',
    `creator_id` bigint NOT NULL COMMENT '创建人ID',
    `scheduled_time` datetime DEFAULT NULL COMMENT '预约时间',
    `start_time` datetime DEFAULT NULL COMMENT '开始时间',
    `end_time` datetime DEFAULT NULL COMMENT '结束时间',
    `duration` int DEFAULT NULL COMMENT '面试时长(分钟)',
    `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/ONGOING/COMPLETED/CANCELLED',
    `room_id` varchar(50) NOT NULL COMMENT '面试房间号',
    `total_score` decimal(5,2) DEFAULT NULL COMMENT '总分',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_interview_no` (`interview_no`),
    UNIQUE KEY `uk_room_id` (`room_id`),
    KEY `idx_candidate_id` (`candidate_id`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_status` (`status`),
    KEY `idx_scheduled_time` (`scheduled_time`),
    CONSTRAINT `fk_interview_candidate_id` FOREIGN KEY (`candidate_id`) REFERENCES `sys_user` (`id`),
    CONSTRAINT `fk_interview_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试表';

-- 9. 面试问题表
CREATE TABLE `t_interview_question` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '问题ID',
    `interview_id` bigint NOT NULL COMMENT '面试ID',
    `question_id` bigint DEFAULT NULL COMMENT '题目ID（来自题库）',
    `question_content` text NOT NULL COMMENT '问题内容',
    `question_type` varchar(20) NOT NULL COMMENT '问题类型：TECHNICAL/BEHAVIORAL/SITUATIONAL',
    `ai_generated` tinyint NOT NULL DEFAULT '0' COMMENT '是否AI生成：0-否 1-是',
    `sequence` int NOT NULL COMMENT '问题顺序',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    KEY `idx_interview_id` (`interview_id`),
    KEY `idx_question_id` (`question_id`),
    KEY `idx_question_type` (`question_type`),
    CONSTRAINT `fk_interview_question_interview_id` FOREIGN KEY (`interview_id`) REFERENCES `t_interview` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_interview_question_question_id` FOREIGN KEY (`question_id`) REFERENCES `t_question` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试问题表';

-- 10. 面试回答表
CREATE TABLE `t_interview_answer` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回答ID',
    `interview_id` bigint NOT NULL COMMENT '面试ID',
    `question_id` bigint NOT NULL COMMENT '问题ID',
    `answer_content` text NOT NULL COMMENT '回答内容',
    `answer_audio_url` varchar(500) DEFAULT NULL COMMENT '回答音频URL',
    `think_duration` int DEFAULT NULL COMMENT '思考时长(秒)',
    `answer_duration` int DEFAULT NULL COMMENT '回答时长(秒)',
    `answer_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '回答时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_id` (`question_id`),
    KEY `idx_interview_id` (`interview_id`),
    CONSTRAINT `fk_answer_interview_id` FOREIGN KEY (`interview_id`) REFERENCES `t_interview` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_answer_question_id` FOREIGN KEY (`question_id`) REFERENCES `t_interview_question` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试回答表';

-- 11. 面试报告表
CREATE TABLE `t_interview_report` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报告ID',
    `interview_id` bigint NOT NULL COMMENT '面试ID',
    `candidate_id` bigint NOT NULL COMMENT '候选人ID',
    `overall_score` decimal(5,2) DEFAULT NULL COMMENT '总体评分',
    `technical_score` decimal(5,2) DEFAULT NULL COMMENT '技术能力分',
    `communication_score` decimal(5,2) DEFAULT NULL COMMENT '沟通能力分',
    `logical_score` decimal(5,2) DEFAULT NULL COMMENT '逻辑能力分',
    `professional_score` decimal(5,2) DEFAULT NULL COMMENT '专业能力分',
    `strengths` text COMMENT '优势分析',
    `weaknesses` text COMMENT '不足分析',
    `suggestions` text COMMENT '改进建议',
    `ai_summary` text COMMENT 'AI综合评价',
    `hr_comment` text COMMENT 'HR评语',
    `recommendation` varchar(20) DEFAULT NULL COMMENT '推荐决策：STRONGLY_RECOMMEND/RECOMMEND/NEUTRAL/NOT_RECOMMEND',
    `report_status` tinyint NOT NULL DEFAULT '0' COMMENT '报告状态：0-生成中 1-已完成',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_interview_id` (`interview_id`),
    KEY `idx_candidate_id` (`candidate_id`),
    KEY `idx_report_status` (`report_status`),
    CONSTRAINT `fk_report_interview_id` FOREIGN KEY (`interview_id`) REFERENCES `t_interview` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_report_candidate_id` FOREIGN KEY (`candidate_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试报告表';

-- 12. 问题评分详情表
CREATE TABLE `t_question_score_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `report_id` bigint NOT NULL COMMENT '报告ID',
    `question_id` bigint NOT NULL COMMENT '问题ID',
    `score` decimal(5,2) NOT NULL DEFAULT '0.00' COMMENT '得分',
    `keyword_match_rate` decimal(5,2) DEFAULT NULL COMMENT '关键词匹配率',
    `semantic_similarity` decimal(5,2) DEFAULT NULL COMMENT '语义相似度',
    `analysis` text COMMENT '回答分析',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_report_question` (`report_id`,`question_id`),
    KEY `idx_question_id` (`question_id`),
    CONSTRAINT `fk_score_report_id` FOREIGN KEY (`report_id`) REFERENCES `t_interview_report` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_score_question_id` FOREIGN KEY (`question_id`) REFERENCES `t_interview_question` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问题评分详情表';

-- 13. Prompt模板表
CREATE TABLE `t_prompt_template` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `template_code` varchar(50) NOT NULL COMMENT '模板编码',
    `template_name` varchar(100) NOT NULL COMMENT '模板名称',
    `template_content` text NOT NULL COMMENT '模板内容',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除：0-否 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt模板表';

-- 插入初始数据

-- 角色数据
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES 
('面试官', 'INTERVIEWER', '系统管理员，拥有所有权限'),
('HR', 'HR', '人力资源，负责面试管理和报告查看'),
('候选人', 'CANDIDATE', '面试候选人');

-- 用户数据
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ZfQ5gZf0xXrPWI4Qj6ddi', '系统管理员', 'INTERVIEWER'),
('hr001', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ZfQ5gZf0xXrPWI4Qj6ddi', '张HR', 'HR'),
('test001', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ZfQ5gZf0xXrPWI4Qj6ddi', '测试候选人', 'CANDIDATE');
-- 密码都是 123456

-- 题目分类数据
INSERT INTO `t_question_category` (`category_name`, `category_code`, `description`, `sort`) VALUES 
('技术题', 'TECHNICAL', '技术相关问题，包括编程、算法、专业知识等', 1),
('行为题', 'BEHAVIORAL', '行为相关问题，包括团队协作、压力处理、职业规划等', 2),
('情景题', 'SITUATIONAL', '情景模拟问题，模拟工作场景的问题解决', 3);

-- 题目标签数据
INSERT INTO `t_question_tag` (`tag_name`, `category_id`) VALUES 
('Java', 1),
('Spring Boot', 1),
('MySQL', 1),
('团队协作', 2),
('压力管理', 2),
('问题解决', 3);

-- Prompt模板初始数据
INSERT INTO `t_prompt_template` (`template_code`, `template_name`, `template_content`, `description`) VALUES
('INTERVIEW_INIT', '面试初始化', '你现在是一位专业的面试官，将要对一位应聘{position}职位的候选人进行面试。\n\n面试背景信息：\n- 职位名称：{position}\n- 职位要求：{requirements}\n- 候选人姓名：{candidateName}\n- 候选人背景：{candidateBackground}\n\n你的任务是通过提问来评估候选人是否适合这个职位。面试将分为以下几个环节：\n1. 开场白和自我介绍\n2. 技术能力评估\n3. 行为问题评估\n4. 情景问题评估\n5. 结束语和后续安排\n\n请以专业、友好的语气进行面试，评估候选人的技术能力、沟通能力、逻辑思维和专业素养。\n\n请首先进行开场白，向候选人介绍自己，并邀请候选人进行自我介绍。', '面试初始化Prompt，设置面试场景和AI面试官角色'),
('TECHNICAL_QUESTION', '技术问题生成', '你是一位专业的技术面试官，正在面试一位{position}职位的候选人。\n\n请根据以下信息生成一个技术相关的面试问题：\n- 职位：{position}\n- 技术方向：{techStack}\n- 难度级别：{difficulty}/5（1最简单，5最困难）\n- 已问过的问题：{askedQuestions}\n- 候选人之前的回答：{previousAnswers}\n\n要求：\n1. 问题应该是开放性的，能够评估候选人的技术深度和广度\n2. 问题应该与职位相关，针对核心技术能力\n3. 问题难度应该符合指定级别\n4. 避免与已问过的问题重复或过于相似\n5. 可以基于候选人之前的回答进行深入探讨\n\n请直接给出问题内容，不要包含其他解释或前缀。', '技术问题生成Prompt，用于生成技术相关的面试问题'),
('BEHAVIORAL_QUESTION', '行为问题生成', '你是一位专业的面试官，正在评估候选人的软技能和行为特质。\n\n请根据以下信息生成一个行为问题：\n- 职位：{position}\n- 目标评估特质：{trait}（如团队协作、压力处理、问题解决等）\n- 面试阶段：{stage}\n- 已问过的问题：{askedQuestions}\n\n要求：\n1. 问题应该是情境化的，要求候选人分享过去的经历\n2. 问题应该能够评估指定的行为特质\n3. 使用STAR法则（情境、任务、行动、结果）引导候选人回答\n4. 问题应该与职位相关性强\n5. 避免与已问过的问题重复\n\n请直接给出问题内容，不要包含其他解释或前缀。', '行为问题生成Prompt，用于生成行为相关的面试问题'); 