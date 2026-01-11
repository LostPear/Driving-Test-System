/*
 优化后的数据库结构（性能优化版本）
 目标：使ER图关系更清晰，同时保证查询性能
 
 优化策略：
 1. 在主表中保留JSON字段（questions, answers）用于快速查询
 2. 创建关联表用于ER图展示和复杂统计分析
 3. 查询时优先使用JSON字段，关联表作为冗余数据（可选维护）
 
 Date: 2026-01-08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for users（用户表保持不变）
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `role` enum('user','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'user',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE,
  UNIQUE INDEX `email`(`email`) USING BTREE,
  INDEX `idx_username`(`username`) USING BTREE,
  INDEX `idx_email`(`email`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for questions（题目表保持不变）
-- ----------------------------
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` enum('single','judge','multiple') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `difficulty` enum('easy','medium','hard') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `options` json NOT NULL,
  `correct_answer` int(11) NOT NULL,
  `correct_answers` json NULL,
  `explanation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_difficulty`(`difficulty`) USING BTREE,
  INDEX `idx_image`(`image`(255)) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for exam_records（考试记录表）
-- 保留JSON字段用于快速查询，同时创建关联表用于ER图展示
-- ----------------------------
DROP TABLE IF EXISTS `exam_records`;
CREATE TABLE `exam_records` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `questions` json NOT NULL COMMENT '题目ID列表（JSON数组，用于快速查询）',
  `answers` json NULL COMMENT '用户答案（JSON对象，用于快速查询）',
  `score` int(11) NULL DEFAULT NULL COMMENT '得分',
  `total_questions` int(11) NOT NULL DEFAULT 0 COMMENT '题目总数',
  `passed` tinyint(1) NULL DEFAULT 0 COMMENT '是否通过（90分及格）',
  `duration_minutes` int(11) NULL DEFAULT NULL COMMENT '考试时长（分钟）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `submitted_at` datetime NULL DEFAULT NULL COMMENT '提交时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_submitted_at`(`submitted_at`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE,
  CONSTRAINT `exam_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '考试记录表';

-- ----------------------------
-- Table structure for exam_record_questions（考试记录-题目关联表）
-- 用于ER图展示和复杂统计分析（如：某道题被答错的次数）
-- ----------------------------
DROP TABLE IF EXISTS `exam_record_questions`;
CREATE TABLE `exam_record_questions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `exam_record_id` int(11) NOT NULL COMMENT '考试记录ID',
  `question_id` int(11) NOT NULL COMMENT '题目ID',
  `order_index` int(11) NOT NULL DEFAULT 0 COMMENT '题目在考试中的顺序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_record_id`(`exam_record_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  UNIQUE INDEX `unique_exam_question`(`exam_record_id`, `question_id`) USING BTREE,
  CONSTRAINT `exam_record_questions_ibfk_1` FOREIGN KEY (`exam_record_id`) REFERENCES `exam_records` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `exam_record_questions_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '考试记录-题目关联表（用于ER图和统计分析）';

-- ----------------------------
-- Table structure for exam_record_answers（考试记录-答案表）
-- 用于ER图展示和复杂统计分析
-- ----------------------------
DROP TABLE IF EXISTS `exam_record_answers`;
CREATE TABLE `exam_record_answers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `exam_record_id` int(11) NOT NULL COMMENT '考试记录ID',
  `question_id` int(11) NOT NULL COMMENT '题目ID',
  `user_answer` int(11) NULL DEFAULT NULL COMMENT '用户答案',
  `is_correct` tinyint(1) NULL DEFAULT NULL COMMENT '是否正确',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_record_id`(`exam_record_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  INDEX `idx_is_correct`(`is_correct`) USING BTREE,
  UNIQUE INDEX `unique_exam_question_answer`(`exam_record_id`, `question_id`) USING BTREE,
  CONSTRAINT `exam_record_answers_ibfk_1` FOREIGN KEY (`exam_record_id`) REFERENCES `exam_records` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `exam_record_answers_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '考试记录-答案表（用于ER图和统计分析）';

-- ----------------------------
-- Table structure for practice_records（练习记录表）
-- 保留JSON字段用于快速查询，同时创建关联表用于ER图展示
-- ----------------------------
DROP TABLE IF EXISTS `practice_records`;
CREATE TABLE `practice_records` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `questions` json NOT NULL COMMENT '题目ID列表（JSON数组，用于快速查询）',
  `answers` json NULL COMMENT '用户答案（JSON对象，用于快速查询）',
  `practice_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'random' COMMENT '练习类型（random/sequential/category）',
  `total_questions` int(11) NOT NULL DEFAULT 0 COMMENT '题目总数',
  `correct_count` int(11) NULL DEFAULT 0 COMMENT '答对数量',
  `wrong_count` int(11) NULL DEFAULT 0 COMMENT '答错数量',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `submitted_at` datetime NULL DEFAULT NULL COMMENT '提交时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_submitted_at`(`submitted_at`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE,
  CONSTRAINT `practice_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '练习记录表';

-- ----------------------------
-- Table structure for practice_record_questions（练习记录-题目关联表）
-- 用于ER图展示和复杂统计分析
-- ----------------------------
DROP TABLE IF EXISTS `practice_record_questions`;
CREATE TABLE `practice_record_questions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `practice_record_id` int(11) NOT NULL COMMENT '练习记录ID',
  `question_id` int(11) NOT NULL COMMENT '题目ID',
  `order_index` int(11) NOT NULL DEFAULT 0 COMMENT '题目在练习中的顺序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_practice_record_id`(`practice_record_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  UNIQUE INDEX `unique_practice_question`(`practice_record_id`, `question_id`) USING BTREE,
  CONSTRAINT `practice_record_questions_ibfk_1` FOREIGN KEY (`practice_record_id`) REFERENCES `practice_records` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `practice_record_questions_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '练习记录-题目关联表（用于ER图和统计分析）';

-- ----------------------------
-- Table structure for practice_record_answers（练习记录-答案表）
-- 用于ER图展示和复杂统计分析
-- ----------------------------
DROP TABLE IF EXISTS `practice_record_answers`;
CREATE TABLE `practice_record_answers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `practice_record_id` int(11) NOT NULL COMMENT '练习记录ID',
  `question_id` int(11) NOT NULL COMMENT '题目ID',
  `user_answer` int(11) NULL DEFAULT NULL COMMENT '用户答案',
  `is_correct` tinyint(1) NULL DEFAULT NULL COMMENT '是否正确',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_practice_record_id`(`practice_record_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  INDEX `idx_is_correct`(`is_correct`) USING BTREE,
  UNIQUE INDEX `unique_practice_question_answer`(`practice_record_id`, `question_id`) USING BTREE,
  CONSTRAINT `practice_record_answers_ibfk_1` FOREIGN KEY (`practice_record_id`) REFERENCES `practice_records` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `practice_record_answers_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT = '练习记录-答案表（用于ER图和统计分析）';

-- ----------------------------
-- Table structure for favorites（收藏表保持不变）
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `question_id` int(11) NOT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_user_question`(`user_id`, `question_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE,
  CONSTRAINT `favorites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `favorites_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
