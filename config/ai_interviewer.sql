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
                            `role` varchar(20) NOT NULL DEFAULT 'CANDIDATE' COMMENT '角色：ADMIN/HR/CANDIDATE',
                            `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_username` (`username`),
                            KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 题库表
CREATE TABLE `t_question` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
                              `title` varchar(500) NOT NULL COMMENT '题目标题',
                              `content` text COMMENT '题目详细内容',
                              `type` varchar(20) NOT NULL DEFAULT 'BEHAVIORAL' COMMENT '题型：TECHNICAL/BEHAVIORAL/SITUATIONAL',
                              `difficulty` tinyint NOT NULL DEFAULT '3' COMMENT '难度1-5',
                              `position` varchar(100) DEFAULT NULL COMMENT '适用职位',
                              `tags` varchar(500) DEFAULT NULL COMMENT '标签，逗号分隔',
                              `reference_answer` text COMMENT '参考答案',
                              `keywords` varchar(500) DEFAULT NULL COMMENT '关键词，逗号分隔',
                              `creator_id` bigint NOT NULL COMMENT '创建人ID',
                              `use_count` int DEFAULT '0' COMMENT '使用次数',
                              `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              PRIMARY KEY (`id`),
                              KEY `idx_type` (`type`),
                              KEY `idx_position` (`position`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库表';

-- 3. 面试表
CREATE TABLE `t_interview` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '面试ID',
                               `interview_no` varchar(50) NOT NULL COMMENT '面试编号',
                               `candidate_id` bigint NOT NULL COMMENT '候选人ID',
                               `candidate_name` varchar(50) DEFAULT NULL COMMENT '候选人姓名',
                               `position` varchar(100) DEFAULT NULL COMMENT '应聘职位',
                               `interviewer_id` bigint DEFAULT NULL COMMENT '面试官ID',
                               `scheduled_time` datetime DEFAULT NULL COMMENT '预约时间',
                               `start_time` datetime DEFAULT NULL COMMENT '开始时间',
                               `end_time` datetime DEFAULT NULL COMMENT '结束时间',
                               `duration` int DEFAULT NULL COMMENT '面试时长(分钟)',
                               `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/ONGOING/COMPLETED/CANCELLED',
                               `room_code` varchar(50) DEFAULT NULL COMMENT '面试房间号',
                               `total_score` decimal(5,2) DEFAULT NULL COMMENT '总分',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_interview_no` (`interview_no`),
                               KEY `idx_candidate_id` (`candidate_id`),
                               KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试表';

-- 4. 面试记录表
CREATE TABLE `t_interview_record` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                                      `interview_id` bigint NOT NULL COMMENT '面试ID',
                                      `question_id` bigint DEFAULT NULL COMMENT '题目ID（如果来自题库）',
                                      `question_content` text NOT NULL COMMENT '实际问题内容',
                                      `question_type` varchar(20) DEFAULT 'AI' COMMENT '问题来源：AI/BANK（题库）',
                                      `answer_text` text COMMENT '回答文本',
                                      `answer_audio_url` varchar(500) DEFAULT NULL COMMENT '回答音频URL',
                                      `think_duration` int DEFAULT NULL COMMENT '思考时长(秒)',
                                      `answer_duration` int DEFAULT NULL COMMENT '回答时长(秒)',
                                      `ai_score` decimal(5,2) DEFAULT NULL COMMENT 'AI评分(0-100)',
                                      `ai_analysis` text COMMENT 'AI分析',
                                      `keywords_matched` varchar(500) DEFAULT NULL COMMENT '匹配到的关键词',
                                      `sequence` int NOT NULL COMMENT '问题顺序',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`id`),
                                      KEY `idx_interview_id` (`interview_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试记录表';

-- 5. 面试报告表
CREATE TABLE `t_interview_report` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报告ID',
                                      `interview_id` bigint NOT NULL COMMENT '面试ID',
                                      `overall_score` decimal(5,2) DEFAULT NULL COMMENT '总体评分',
                                      `technical_score` decimal(5,2) DEFAULT NULL COMMENT '技术能力分',
                                      `communication_score` decimal(5,2) DEFAULT NULL COMMENT '沟通能力分',
                                      `logic_score` decimal(5,2) DEFAULT NULL COMMENT '逻辑能力分',
                                      `professional_score` decimal(5,2) DEFAULT NULL COMMENT '专业能力分',
                                      `strengths` text COMMENT '优势分析',
                                      `weaknesses` text COMMENT '不足分析',
                                      `suggestions` text COMMENT '改进建议',
                                      `ai_summary` text COMMENT 'AI综合评价',
                                      `hr_comment` text COMMENT 'HR评语',
                                      `recommendation` varchar(20) DEFAULT NULL COMMENT '推荐决策：STRONGLY_RECOMMEND/RECOMMEND/NEUTRAL/NOT_RECOMMEND',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                      PRIMARY KEY (`id`),
                                      UNIQUE KEY `uk_interview_id` (`interview_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试报告表';

-- 插入测试数据
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`) VALUES
                                                                         ('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ZfQ5gZf0xXrPWI4Qj6ddi', '系统管理员', 'ADMIN'),
                                                                         ('hr001', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ZfQ5gZf0xXrPWI4Qj6ddi', '张HR', 'HR'),
                                                                         ('test001', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36ZfQ5gZf0xXrPWI4Qj6ddi', '测试候选人', 'CANDIDATE');
-- 密码都是 123456