/*
 Navicat Premium Dump SQL

 Source Server         : MySQL 5.7
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44)
 Source Host           : localhost:3306
 Source Schema         : exam_db

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44)
 File Encoding         : 65001

 Date: 16/01/2026 21:48:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for exam_record_answers
-- ----------------------------
DROP TABLE IF EXISTS `exam_record_answers`;
CREATE TABLE `exam_record_answers`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `exam_record_id` int(11) NOT NULL COMMENT '考试记录ID',
  `question_id` int(11) NOT NULL COMMENT '题目ID',
  `user_answer` int(11) NULL DEFAULT NULL COMMENT '用户答案',
  `is_correct` tinyint(1) NULL DEFAULT NULL COMMENT '是否正确',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_exam_question_answer`(`exam_record_id`, `question_id`) USING BTREE,
  INDEX `idx_exam_record_id`(`exam_record_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  INDEX `idx_is_correct`(`is_correct`) USING BTREE,
  CONSTRAINT `exam_record_answers_ibfk_1` FOREIGN KEY (`exam_record_id`) REFERENCES `exam_records` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `exam_record_answers_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '考试记录-答案表（用于ER图和统计分析）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_record_answers
-- ----------------------------

-- ----------------------------
-- Table structure for exam_record_questions
-- ----------------------------
DROP TABLE IF EXISTS `exam_record_questions`;
CREATE TABLE `exam_record_questions`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `exam_record_id` int(11) NOT NULL COMMENT '考试记录ID',
  `question_id` int(11) NOT NULL COMMENT '题目ID',
  `order_index` int(11) NOT NULL DEFAULT 0 COMMENT '题目在考试中的顺序',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_exam_question`(`exam_record_id`, `question_id`) USING BTREE,
  INDEX `idx_exam_record_id`(`exam_record_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  CONSTRAINT `exam_record_questions_ibfk_1` FOREIGN KEY (`exam_record_id`) REFERENCES `exam_records` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `exam_record_questions_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '考试记录-题目关联表（用于ER图和统计分析）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_record_questions
-- ----------------------------

-- ----------------------------
-- Table structure for exam_records
-- ----------------------------
DROP TABLE IF EXISTS `exam_records`;
CREATE TABLE `exam_records`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '考试记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of exam_records
-- ----------------------------
INSERT INTO `exam_records` VALUES (1, 3, '[148, 97, 60, 100, 94, 81, 25, 107, 53, 9, 150, 14, 56, 32, 134, 1, 181, 49, 160, 28, 132, 72, 50, 174, 115, 84, 161, 90, 80, 43, 17, 175, 96, 106, 145, 111, 163, 59, 78, 42, 73, 8, 4, 39, 120, 30, 34, 170, 23, 44, 87, 33, 123, 127, 65, 131, 29, 105, 41, 114, 58, 122, 102, 54, 153, 178, 31, 152, 5, 155, 109, 135, 138, 144, 51, 112, 164, 89, 166, 75, 142, 83, 125, 46, 124, 48, 104, 157, 85, 147, 69, 130, 35, 98, 99, 57, 176, 151, 113, 13]', '{\"0\": 0, \"1\": 2, \"2\": 1, \"3\": 1}', 0, 100, 0, NULL, '2026-01-11 12:54:25', '2026-01-11 12:54:25');

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of favorites
-- ----------------------------
INSERT INTO `favorites` VALUES (1, 3, 122, '2026-01-11 13:56:48');

-- ----------------------------
-- Table structure for practice_record_answers
-- ----------------------------
DROP TABLE IF EXISTS `practice_record_answers`;
CREATE TABLE `practice_record_answers`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `practice_record_id` int(11) NOT NULL COMMENT '练习记录ID',
  `question_id` int(11) NOT NULL COMMENT '题目ID',
  `user_answer` int(11) NULL DEFAULT NULL COMMENT '用户答案',
  `is_correct` tinyint(1) NULL DEFAULT NULL COMMENT '是否正确',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_practice_question_answer`(`practice_record_id`, `question_id`) USING BTREE,
  INDEX `idx_practice_record_id`(`practice_record_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  INDEX `idx_is_correct`(`is_correct`) USING BTREE,
  CONSTRAINT `practice_record_answers_ibfk_1` FOREIGN KEY (`practice_record_id`) REFERENCES `practice_records` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `practice_record_answers_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '练习记录-答案表（用于ER图和统计分析）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of practice_record_answers
-- ----------------------------

-- ----------------------------
-- Table structure for practice_record_questions
-- ----------------------------
DROP TABLE IF EXISTS `practice_record_questions`;
CREATE TABLE `practice_record_questions`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `practice_record_id` int(11) NOT NULL COMMENT '练习记录ID',
  `question_id` int(11) NOT NULL COMMENT '题目ID',
  `order_index` int(11) NOT NULL DEFAULT 0 COMMENT '题目在练习中的顺序',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_practice_question`(`practice_record_id`, `question_id`) USING BTREE,
  INDEX `idx_practice_record_id`(`practice_record_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  CONSTRAINT `practice_record_questions_ibfk_1` FOREIGN KEY (`practice_record_id`) REFERENCES `practice_records` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `practice_record_questions_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '练习记录-题目关联表（用于ER图和统计分析）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of practice_record_questions
-- ----------------------------

-- ----------------------------
-- Table structure for practice_records
-- ----------------------------
DROP TABLE IF EXISTS `practice_records`;
CREATE TABLE `practice_records`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '练习记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of practice_records
-- ----------------------------

-- ----------------------------
-- Table structure for questions
-- ----------------------------
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 182 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of questions
-- ----------------------------
INSERT INTO `questions` VALUES (1, 'single', 'easy', 'test1', '/api/images/cde6177f-e6ad-4118-b742-50ed90697c6f.jpg', '[\"A\", \"B\", \"C\", \"D\"]', 0, '[0]', 'test', '2025-12-09 08:20:16');
INSERT INTO `questions` VALUES (2, 'single', 'medium', 'test2', NULL, '[\"A\", \"B\", \"C\", \"D\"]', 1, '[1]', 'test2', '2025-12-09 08:44:58');
INSERT INTO `questions` VALUES (3, 'single', 'easy', 'test3', NULL, '[\"A\", \"B\", \"C\", \"D\"]', 0, '[0]', 'test', '2025-12-09 09:42:10');
INSERT INTO `questions` VALUES (4, 'single', 'easy', '机动车在道路上发生故障，难以移动时，首先应当持续开启危险报警闪光灯，并在车后多少米处设置警告标志？', NULL, '[\"50米至100米\", \"150米以外\", \"100米至150米\", \"50米以内\"]', 0, '[0]', '根据《道路交通安全法实施条例》规定...', '2025-12-09 11:02:31');
INSERT INTO `questions` VALUES (5, 'judge', 'medium', '驾驶机动车在道路上行驶时，应当悬挂机动车号牌。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '这是法律规定的要求。', '2025-12-09 11:02:31');
INSERT INTO `questions` VALUES (8, 'single', 'easy', '机动车在道路上发生故障，难以移动时，首先应当持续开启危险报警闪光灯，并在车后多少米处设置警告标志？', NULL, '[\"50米至100米\", \"150米以外\", \"100米至150米\", \"50米以内\"]', 0, '[0]', '根据《道路交通安全法实施条例》规定...', '2025-12-09 11:10:10');
INSERT INTO `questions` VALUES (9, 'judge', 'medium', '驾驶机动车在道路上行驶时，应当悬挂机动车号牌。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '这是法律规定的要求。', '2025-12-09 11:10:10');
INSERT INTO `questions` VALUES (10, 'multiple', 'hard', '答案是BC', NULL, '[\"A\", \"B\", \"C\", \"D\"]', 1, '[1, 2]', 'BC', '2025-12-09 11:44:56');
INSERT INTO `questions` VALUES (11, 'single', 'easy', '机动车在道路上发生故障，难以移动时，首先应当持续开启危险报警闪光灯，并在车后多少米处设置警告标志？', NULL, '[\"50米至100米\", \"150米以外\", \"100米至150米\", \"50米以内\"]', 0, '[0]', '根据《道路交通安全法实施条例》规定...', '2025-12-09 12:30:01');
INSERT INTO `questions` VALUES (12, 'judge', 'medium', '驾驶机动车在道路上行驶时，应当悬挂机动车号牌。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '这是法律规定的要求。', '2025-12-09 12:30:01');
INSERT INTO `questions` VALUES (13, 'single', 'easy', '机动车在道路上发生故障，难以移动时，首先应当持续开启危险报警闪光灯，并在车后多少米处设置警告标志？', NULL, '[\"50米至100米\", \"150米以外\", \"100米至150米\", \"50米以内\"]', 0, '[0]', '根据《道路交通安全法实施条例》规定...', '2025-12-09 12:30:13');
INSERT INTO `questions` VALUES (14, 'judge', 'medium', '驾驶机动车在道路上行驶时，应当悬挂机动车号牌。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '这是法律规定的要求。', '2025-12-09 12:30:13');
INSERT INTO `questions` VALUES (15, 'single', 'medium', '夜间驾驶机动车会车时，应该注意哪些事项？', NULL, '[\"使用近光灯\", \"保持安全距离\", \"开启远光灯\", \"注意观察路况\"]', 0, '[0]', '夜间会车时应使用近光灯，保持安全距离，注意观察路况。', '2025-12-09 12:32:12');
INSERT INTO `questions` VALUES (16, 'judge', 'medium', '驾驶机动车在道路上行驶时，应当悬挂机动车号牌。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '这是法律规定的要求。', '2025-12-09 12:32:12');
INSERT INTO `questions` VALUES (17, 'single', 'easy', '机动车在道路上发生故障，难以移动时，首先应当持续开启危险报警闪光灯，并在车后多少米处设置警告标志？', NULL, '[\"50米至100米\", \"150米以外\", \"100米至150米\", \"50米以内\"]', 0, '[0]', '根据《道路交通安全法实施条例》第60条规定，机动车在道路上发生故障难以移动时，应在车后50米至100米处设置警告标志。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (18, 'judge', 'medium', '驾驶机动车在道路上行驶时，应当悬挂机动车号牌。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '根据《道路交通安全法》第11条规定，驾驶机动车上道路行驶，应当悬挂机动车号牌。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (19, 'multiple', 'hard', '下列哪些情况属于醉酒驾驶机动车的判定标准？', NULL, '[\"血液酒精含量达到80mg/100ml以上\", \"血液酒精含量达到20mg/100ml以上\", \"在道路上驾驶机动车\", \"驾驶营运机动车\"]', 0, '[0, 2]', '根据相关法规，醉酒驾驶指血液酒精含量达到80mg/100ml以上并在道路上驾驶机动车的行为。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (20, 'single', 'easy', '驾驶机动车在路口直行遇到这种信号灯应该怎样行驶？', NULL, '[\"停车等待\", \"加速通过\", \"减速慢行\", \"进入路口等待\"]', 0, '[0]', '红灯亮时，禁止车辆通行，应在停止线外停车等待。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (21, 'judge', 'easy', '驾驶机动车违反道路交通安全法律法规发生交通事故属于交通违章行为。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '违反道路交通安全法律法规造成交通事故，属于交通违法行为。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (22, 'multiple', 'medium', '夜间驾驶机动车会车时，应该注意哪些事项？', NULL, '[\"使用近光灯\", \"保持安全距离\", \"开启远光灯\", \"注意观察路况\"]', 0, '[0, 1, 3]', '夜间会车时应使用近光灯，保持安全距离，注意观察路况。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (23, 'single', 'medium', '在高速公路驾驶机动车，最低车速不得低于每小时多少公里？', NULL, '[\"60公里\", \"80公里\", \"100公里\", \"120公里\"]', 0, '[0]', '高速公路最低车速为每小时60公里。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (24, 'judge', 'hard', '机动车驾驶证被吊销后，5年内不得重新申领驾驶证。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '根据《道路交通安全法》规定，驾驶证被吊销后，5年内不得重新申领。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (25, 'multiple', 'easy', '下列哪些行为属于危险驾驶行为？', NULL, '[\"酒后驾驶\", \"超速行驶\", \"疲劳驾驶\", \"系安全带驾驶\"]', 0, '[0, 1, 2]', '酒后驾驶、超速行驶、疲劳驾驶都属于危险驾驶行为。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (26, 'single', 'hard', '驾驶机动车在高速公路上行驶，遇有雾、雨、雪等低能见度气象条件时，能见度小于50米时，车速不得超过每小时多少公里？', NULL, '[\"20公里\", \"30公里\", \"40公里\", \"50公里\"]', 0, '[0]', '能见度小于50米时，车速不得超过每小时20公里。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (27, 'judge', 'medium', '机动车在道路上发生轻微交通事故，当事人对事实及成因无争议的，可以先撤离现场再协商处理。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '轻微交通事故无争议的，可以先行撤离现场协商处理。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (28, 'multiple', 'hard', '在哪些路段不得超车？', NULL, '[\"交叉路口\", \"窄桥\", \"弯道\", \"隧道\"]', 0, '[0, 1, 2, 3]', '交叉路口、窄桥、弯道、隧道等路段禁止超车。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (29, 'single', 'easy', '机动车驾驶人初次申领驾驶证后的实习期是多长时间？', NULL, '[\"12个月\", \"6个月\", \"3个月\", \"24个月\"]', 0, '[0]', '机动车驾驶人初次申领驾驶证后的实习期为12个月。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (30, 'judge', 'easy', '驾驶机动车在道路上掉头时，应当提前开启左转向灯。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '车辆掉头时应提前开启左转向灯。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (31, 'multiple', 'medium', '驾驶机动车通过交叉路口时，应该注意哪些事项？', NULL, '[\"减速慢行\", \"观察交通信号\", \"礼让行人\", \"加速通过\"]', 0, '[0, 1, 2]', '通过交叉路口时应减速慢行、观察交通信号、礼让行人。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (32, 'single', 'medium', '驾驶机动车在高速公路上行驶，车速超过每小时100公里时，应当与同车道前车保持多少米以上的距离？', NULL, '[\"100米\", \"150米\", \"200米\", \"50米\"]', 0, '[0]', '车速超过100km/h时，应与前车保持100米以上距离。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (33, 'judge', 'hard', '驾驶机动车发生财产损失交通事故，当事人对事实及成因无争议但移动车辆时，不需要报警。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '即使无争议，移动车辆前也需要报警或拍照固定证据。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (34, 'multiple', 'easy', '下列哪些情况需要办理机动车变更登记？', NULL, '[\"改变车身颜色\", \"更换发动机\", \"更换车身\", \"更换车架\"]', 0, '[0, 1, 2, 3]', '改变车身颜色、更换发动机、车身、车架都需要办理变更登记。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (35, 'single', 'hard', '醉酒驾驶营运机动车的，多少年内不得重新取得机动车驾驶证？', NULL, '[\"10年\", \"5年\", \"15年\", \"终身\"]', 0, '[0]', '醉酒驾驶营运机动车的，10年内不得重新取得驾驶证。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (36, 'judge', 'medium', '驾驶机动车在高速公路行驶，可以在匝道、加速车道减速车道上超车。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '匝道、加速车道、减速车道禁止超车。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (37, 'multiple', 'hard', '哪些交通违法行为一次记12分？', NULL, '[\"醉酒驾驶\", \"肇事逃逸不构成犯罪\", \"使用伪造驾驶证\", \"超速50%以上\"]', 0, '[0, 1, 2, 3]', '醉酒驾驶、肇事逃逸、使用伪造驾驶证、超速50%以上都记12分。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (38, 'single', 'easy', '机动车在夜间通过没有交通信号灯控制的交叉路口时，应当如何使用灯光？', NULL, '[\"交替使用远近光灯示意\", \"使用近光灯\", \"使用远光灯\", \"关闭灯光\"]', 0, '[0]', '夜间通过无信号灯路口应交替使用远近光灯示意。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (39, 'judge', 'easy', '驾驶机动车上路行驶应当随车携带机动车行驶证。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '驾驶机动车应随车携带行驶证。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (40, 'multiple', 'medium', '驾驶机动车在雨天临时停车时，应该开启什么灯？', NULL, '[\"危险报警闪光灯\", \"示廓灯\", \"后位灯\", \"前照灯\"]', 0, '[0, 1, 2]', '雨天临时停车应开启危险报警闪光灯、示廓灯和后位灯。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (41, 'single', 'medium', '驾驶机动车在高速公路上行驶，遇能见度小于200米时，车速不得超过每小时多少公里？', NULL, '[\"60公里\", \"80公里\", \"100公里\", \"120公里\"]', 0, '[0]', '能见度小于200米时，车速不得超过60km/h。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (42, 'judge', 'hard', '驾驶机动车在道路上发生交通事故，因抢救伤员变动现场时，应当标明位置。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '抢救伤员变动现场时应标明位置。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (43, 'multiple', 'easy', '机动车在高速公路上发生故障时，警告标志应当设置在故障车来车方向多少米以外？', NULL, '[\"150米\", \"100米\", \"50米\", \"200米\"]', 0, '[0]', '高速公路故障警告标志应设置在150米以外。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (44, 'single', 'hard', '饮酒后驾驶营运机动车的，处多少日拘留？', NULL, '[\"15日\", \"10日\", \"5日\", \"20日\"]', 0, '[0]', '饮酒后驾驶营运机动车处15日拘留。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (45, 'judge', 'medium', '驾驶机动车在道路上超车完毕驶回原车道时，应当开启右转向灯。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '超车完毕驶回原车道时应开启右转向灯。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (46, 'multiple', 'hard', '驾驶机动车不得有哪些行为？', NULL, '[\"拨打接听手持电话\", \"连续驾驶超过4小时未休息\", \"车门未关好时行车\", \"按规定使用安全带\"]', 0, '[0, 1, 2]', '驾驶时不得拨打接听手持电话、疲劳驾驶、车门未关好行车。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (47, 'single', 'easy', '驾驶机动车在道路上向左变更车道时，应当提前开启什么灯？', NULL, '[\"左转向灯\", \"右转向灯\", \"危险报警闪光灯\", \"示廓灯\"]', 0, '[0]', '向左变更车道应提前开启左转向灯。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (48, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯亮时，要在停止线前停车瞭望。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '红灯亮时应在停止线前停车。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (49, 'multiple', 'medium', '驾驶机动车在山区道路行驶时，应该注意什么？', NULL, '[\"保持车距\", \"注意落石\", \"控制车速\", \"频繁超车\"]', 0, '[0, 1, 2]', '山区道路行驶应保持车距、注意落石、控制车速。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (50, 'single', 'medium', '驾驶机动车在冰雪道路上行驶时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '冰雪道路行驶最高速度不超过30km/h。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (51, 'judge', 'hard', '驾驶机动车在道路上追逐竞驶，情节恶劣的，处拘役并处罚金。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '追逐竞驶情节恶劣的构成危险驾驶罪，处拘役并处罚金。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (52, 'multiple', 'easy', '机动车在哪些情形下不得超车？', NULL, '[\"前车正在左转弯\", \"前车正在掉头\", \"前车正在超车\", \"前车正常行驶\"]', 0, '[0, 1, 2]', '前车左转弯、掉头、超车时不得超车。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (53, 'single', 'hard', '造成交通事故后逃逸，尚不构成犯罪的，公安机关交通管理部门可以处多少元罚款？', NULL, '[\"2000元\", \"1000元\", \"500元\", \"200元\"]', 0, '[0]', '肇事逃逸不构成犯罪的处2000元罚款。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (54, 'judge', 'medium', '驾驶机动车在道路上发生交通事故，当事人不能自行移动车辆的，应当保护现场并立即报警。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '不能自行移动车辆时应保护现场并报警。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (55, 'multiple', 'hard', '哪些情况下不得申请机动车驾驶证？', NULL, '[\"醉酒驾驶被吊销未满5年\", \"肇事逃逸被吊销\", \"吸毒被注销未满3年\", \"心脏病患者\"]', 0, '[0, 1, 2]', '醉酒驾驶被吊销未满5年、肇事逃逸被吊销、吸毒被注销未满3年不得申请驾驶证。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (56, 'single', 'easy', '驾驶机动车在车道减少的路口，遇到前方车辆依次停车等候时，应当怎样做？', NULL, '[\"依次交替通行\", \"借道超车\", \"穿插等候的车辆\", \"鸣喇叭催促\"]', 0, '[0]', '车道减少时应依次交替通行。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (57, 'judge', 'easy', '驾驶机动车在路口遇到这种情况时，要加速通过。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '路口有行人时应减速让行。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (58, 'multiple', 'medium', '驾驶机动车在雾天行驶时，应当开启什么灯？', NULL, '[\"雾灯\", \"危险报警闪光灯\", \"示廓灯\", \"近光灯\"]', 0, '[0, 1, 2, 3]', '雾天行驶应开启雾灯、危险报警闪光灯、示廓灯、近光灯。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (59, 'single', 'medium', '驾驶机动车在没有中心线的城市道路上，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"70公里/小时\"]', 0, '[0]', '无中心线的城市道路最高速度30km/h。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (60, 'judge', 'hard', '驾驶机动车造成重大交通事故后逃逸，构成犯罪的，终身不得重新取得驾驶证。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '造成重大交通事故后逃逸构成犯罪的，终身禁驾。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (61, 'multiple', 'easy', '驾驶机动车前，应当对车辆进行哪些检查？', NULL, '[\"轮胎气压\", \"灯光信号\", \"燃油量\", \"发动机机油\"]', 0, '[0, 1, 2, 3]', '出车前应检查轮胎气压、灯光信号、燃油量、发动机机油等。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (62, 'single', 'hard', '饮酒后驾驶机动车被处罚，再次饮酒后驾驶机动车的，处多少日拘留？', NULL, '[\"10日以下\", \"15日以下\", \"5日以下\", \"20日以下\"]', 0, '[0]', '再次饮酒驾驶处10日以下拘留。', '2025-12-09 12:43:46');
INSERT INTO `questions` VALUES (63, 'judge', 'medium', '驾驶机动车在高速公路行驶，可以从匝道直接驶入行车道。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '应从加速车道充分加速后再驶入行车道。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (64, 'multiple', 'hard', '哪些行为属于交通肇事罪？', NULL, '[\"死亡1人负主要责任\", \"重伤3人负主要责任\", \"财产损失负主要责任\", \"酒后驾驶\"]', 0, '[0, 1]', '死亡1人或重伤3人以上负主要责任构成交通肇事罪。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (65, 'single', 'easy', '驾驶机动车在夜间通过急弯、坡路时，应当如何用灯？', NULL, '[\"交替使用远近光灯\", \"使用近光灯\", \"使用远光灯\", \"关闭所有灯光\"]', 0, '[0]', '夜间通过急弯、坡路应交替使用远近光灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (66, 'judge', 'easy', '驾驶机动车在路口右转弯时，应当提前开启右转向灯。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '右转弯时应提前开启右转向灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (67, 'multiple', 'medium', '驾驶机动车在高速公路上发生故障时，应当采取哪些措施？', NULL, '[\"开启危险报警闪光灯\", \"在来车方向设置警告标志\", \"车上人员转移到安全地带\", \"继续行驶到服务区\"]', 0, '[0, 1, 2]', '高速公路故障应开启双闪、设置警告标志、人员转移到安全地带。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (68, 'single', 'medium', '驾驶机动车在进出非机动车道时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '进出非机动车道最高速度30km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (69, 'judge', 'hard', '驾驶机动车违反交通管制规定强行通行，不听劝阻的，处2000元罚款。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '违反交通管制强行通行处2000元罚款。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (70, 'multiple', 'easy', '驾驶机动车在道路上行驶，驾驶人应当按规定使用什么？', NULL, '[\"安全带\", \"安全头盔\", \"灭火器\", \"三角警告牌\"]', 0, '[0]', '驾驶时应按规定使用安全带。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (71, 'single', 'hard', '伪造、变造机动车驾驶证构成犯罪的，依法追究什么责任？', NULL, '[\"刑事责任\", \"民事责任\", \"行政责任\", \"经济责任\"]', 0, '[0]', '伪造、变造驾驶证构成犯罪的追究刑事责任。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (72, 'judge', 'medium', '驾驶机动车在道路上掉头时，应当在允许掉头的地点提前驶入最左侧车道。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '掉头时应提前驶入最左侧车道。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (73, 'multiple', 'hard', '驾驶机动车在哪些情况下不得停车？', NULL, '[\"人行横道\", \"交叉路口\", \"公共汽车站\", \"单位门口\"]', 0, '[0, 1, 2]', '人行横道、交叉路口、公共汽车站等地点不得停车。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (74, 'single', 'easy', '驾驶机动车在道路上行驶时，机动车驾驶人应当随身携带哪种证件？', NULL, '[\"驾驶证\", \"身份证\", \"行驶证\", \"保险单\"]', 0, '[0]', '驾驶人应随身携带驾驶证。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (75, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯禁止通行。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '红灯亮时禁止通行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (76, 'multiple', 'medium', '驾驶机动车在雨天行驶时，应该注意什么？', NULL, '[\"降低车速\", \"保持车距\", \"使用雨刮器\", \"紧急制动\"]', 0, '[0, 1, 2]', '雨天行驶应降低车速、保持车距、使用雨刮器。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (77, 'single', 'medium', '驾驶机动车在泥泞道路行驶时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '泥泞道路最高速度30km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (78, 'judge', 'hard', '驾驶机动车在高速公路上倒车、逆行，一次记12分。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '高速公路倒车、逆行一次记12分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (79, 'multiple', 'easy', '机动车在道路上临时停车时，应当注意什么？', NULL, '[\"不得妨碍其他车辆通行\", \"不得妨碍行人通行\", \"开启危险报警闪光灯\", \"停在路口中央\"]', 0, '[0, 1, 2]', '临时停车不得妨碍其他车辆和行人通行，必要时开启双闪。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (80, 'single', 'hard', '未取得驾驶证驾驶机动车的，公安机关交通管理部门可以处多少元罚款？', NULL, '[\"2000元\", \"1000元\", \"500元\", \"200元\"]', 0, '[0]', '无证驾驶处2000元罚款。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (81, 'judge', 'medium', '驾驶机动车在道路上超车时，可以使用远光灯提示前车。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '超车时应使用远近光灯交替提示。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (82, 'multiple', 'hard', '驾驶机动车在哪些情况下需要降低车速？', NULL, '[\"通过人行横道\", \"通过学校区域\", \"通过居民小区\", \"通过无信号路口\"]', 0, '[0, 1, 2, 3]', '通过人行横道、学校、居民区、无信号路口都应降低车速。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (83, 'single', 'easy', '驾驶机动车在道路上向右变更车道时，应当提前开启什么灯？', NULL, '[\"右转向灯\", \"左转向灯\", \"危险报警闪光灯\", \"示廓灯\"]', 0, '[0]', '向右变更车道应提前开启右转向灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (84, 'judge', 'easy', '驾驶机动车在路口直行遇到前方路口堵塞时，可以进入路口等候。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '路口堵塞时不得进入路口。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (85, 'multiple', 'medium', '驾驶机动车在夜间行驶时，应该如何使用灯光？', NULL, '[\"开启前照灯\", \"使用近光灯\", \"会车时改用近光灯\", \"一直使用远光灯\"]', 0, '[0, 1, 2]', '夜间行驶应开启前照灯，使用近光灯，会车时改用近光灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (86, 'single', 'medium', '驾驶机动车在通过没有交通信号的交叉路口时，应当如何通行？', NULL, '[\"减速慢行并让行人和优先通行的车辆先行\", \"加速通过\", \"鸣喇叭示意\", \"抢行通过\"]', 0, '[0]', '无信号路口应减速慢行，让行人和优先车辆先行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (87, 'judge', 'hard', '驾驶机动车在道路上发生交通事故，当事人故意破坏现场的，承担全部责任。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '故意破坏现场者承担全部责任。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (88, 'multiple', 'easy', '驾驶机动车在哪些情况下需要鸣喇叭？', NULL, '[\"超车时\", \"通过弯道时\", \"通过无信号路口时\", \"市区夜间行驶时\"]', 0, '[0, 1, 2]', '超车、通过弯道、无信号路口时可以鸣喇叭。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (89, 'single', 'hard', '驾驶机动车在高速公路上行驶，低于规定最低车速的，一次记多少分？', NULL, '[\"3分\", \"6分\", \"12分\", \"1分\"]', 0, '[0]', '高速公路低于最低车速记3分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (90, 'judge', 'medium', '驾驶机动车在道路上发生故障停车后，不按规定使用灯光和设置警告标志的，一次记3分。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '故障停车不按规定使用灯光和设置警告标志记3分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (91, 'multiple', 'hard', '驾驶机动车在哪些路段需要特别注意安全？', NULL, '[\"铁路道口\", \"施工路段\", \"学校周边\", \"医院门口\"]', 0, '[0, 1, 2, 3]', '铁路道口、施工路段、学校周边、医院门口都需要特别注意安全。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (92, 'single', 'easy', '驾驶机动车在道路上发生故障，需要停车排除时，驾驶人应当立即开启什么灯？', NULL, '[\"危险报警闪光灯\", \"示廓灯\", \"后位灯\", \"前照灯\"]', 0, '[0]', '车辆故障需要停车时应立即开启危险报警闪光灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (93, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯时，可以右转弯。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '红灯亮时在不妨碍被放行车辆和行人通行的情况下可以右转弯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (94, 'multiple', 'medium', '驾驶机动车在冰雪路面行驶时，应该注意什么？', NULL, '[\"降低车速\", \"保持车距\", \"避免急刹车\", \"紧急转向\"]', 0, '[0, 1, 2]', '冰雪路面应降低车速、保持车距、避免急刹车。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (95, 'single', 'medium', '驾驶机动车在通过铁路道口时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '通过铁路道口最高速度30km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (96, 'judge', 'hard', '驾驶机动车在高速公路上行驶，非紧急情况在应急车道停车的，一次记6分。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '非紧急情况占用应急车道记6分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (97, 'multiple', 'easy', '驾驶机动车在哪些情况下需要减速让行？', NULL, '[\"人行横道有行人\", \"路口有车辆\", \"学校区域\", \"高速公路\"]', 0, '[0, 1, 2]', '人行横道有行人、路口有车辆、学校区域都需要减速让行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (98, 'single', 'hard', '驾驶机动车在高速公路上行驶，超过规定时速50%以上的，一次记多少分？', NULL, '[\"12分\", \"6分\", \"3分\", \"1分\"]', 0, '[0]', '超速50%以上记12分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (99, 'judge', 'medium', '驾驶机动车在道路上行驶，遇有前方车辆停车排队等候时，可以借道超车。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '车辆排队等候时不得借道超车。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (100, 'multiple', 'hard', '驾驶机动车在哪些情况下不得使用远光灯？', NULL, '[\"会车时\", \"跟车时\", \"市区有路灯照明时\", \"通过无信号路口时\"]', 0, '[0, 1, 2]', '会车时、跟车时、市区有路灯照明时不得使用远光灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (101, 'single', 'easy', '驾驶机动车在道路上临时停车，车身距道路边缘不得超过多少厘米？', NULL, '[\"30厘米\", \"50厘米\", \"80厘米\", \"100厘米\"]', 0, '[0]', '临时停车车身距道路边缘不超过30厘米。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (102, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯时，禁止左转弯。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '红灯亮时禁止左转弯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (103, 'multiple', 'medium', '驾驶机动车在山区道路行驶时，应该注意哪些危险？', NULL, '[\"落石\", \"急弯\", \"陡坡\", \"雾天\"]', 0, '[0, 1, 2, 3]', '山区道路应注意落石、急弯、陡坡、雾天等危险。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (104, 'single', 'medium', '驾驶机动车在通过窄路、窄桥时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '窄路、窄桥最高速度30km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (105, 'judge', 'hard', '驾驶机动车在高速公路上行驶，错过出口时，可以倒车回到出口。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '高速公路错过出口应继续行驶到下一出口，不得倒车。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (106, 'multiple', 'easy', '驾驶机动车在哪些情况下需要开启转向灯？', NULL, '[\"变更车道\", \"超车\", \"掉头\", \"靠边停车\"]', 0, '[0, 1, 2, 3]', '变更车道、超车、掉头、靠边停车都需要开启转向灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (107, 'single', 'hard', '驾驶机动车在高速公路上行驶，违反禁令标志的，一次记多少分？', NULL, '[\"3分\", \"6分\", \"12分\", \"1分\"]', 0, '[0]', '违反禁令标志记3分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (108, 'judge', 'medium', '驾驶机动车在道路上行驶，可以随意变更车道。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '变更车道应提前开启转向灯，不得随意变更。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (109, 'multiple', 'hard', '驾驶机动车在哪些情况下需要特别注意观察？', NULL, '[\"通过人行横道\", \"通过学校区域\", \"通过居民小区\", \"通过无信号路口\"]', 0, '[0, 1, 2, 3]', '通过人行横道、学校区域、居民小区、无信号路口都需要特别注意观察。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (110, 'single', 'easy', '驾驶机动车在道路上行驶时，驾驶人应当按照规定使用什么？', NULL, '[\"安全带\", \"安全头盔\", \"灭火器\", \"三角警告牌\"]', 0, '[0]', '驾驶人应按规定使用安全带。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (111, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯时，可以直行通过。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '绿灯亮时可以直行通过。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (112, 'multiple', 'medium', '驾驶机动车在雨天行驶时，应该采取哪些措施保证安全？', NULL, '[\"降低车速\", \"保持安全距离\", \"使用雨刮器\", \"开启雾灯\"]', 0, '[0, 1, 2, 3]', '雨天应降低车速、保持安全距离、使用雨刮器、必要时开启雾灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (113, 'single', 'medium', '驾驶机动车在没有中心线的公路上，最高速度不能超过多少？', NULL, '[\"40公里/小时\", \"50公里/小时\", \"60公里/小时\", \"70公里/小时\"]', 0, '[0]', '无中心线的公路最高速度40km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (114, 'judge', 'hard', '驾驶机动车在高速公路上行驶，可以随意停车上下客。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '高速公路不得随意停车上下客。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (115, 'multiple', 'easy', '驾驶机动车在哪些情况下需要让行？', NULL, '[\"右转弯让左转弯\", \"转弯让直行\", \"相对方向右转让左转\", \"支路让干路\"]', 1, '[1, 2, 3]', '转弯让直行、相对方向右转让左转、支路让干路。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (116, 'single', 'hard', '驾驶机动车在高速公路上行驶，违反规定占用应急车道的，一次记多少分？', NULL, '[\"6分\", \"3分\", \"12分\", \"1分\"]', 0, '[0]', '占用应急车道记6分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (117, 'judge', 'medium', '驾驶机动车在道路上行驶，遇到执行紧急任务的警车、消防车、救护车、工程救险车时，应当及时让行。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '遇到执行紧急任务的特种车辆应当及时让行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (118, 'multiple', 'hard', '驾驶机动车在哪些情况下需要特别注意儿童安全？', NULL, '[\"学校周边\", \"居民小区\", \"公园附近\", \"商场周边\"]', 0, '[0, 1, 2, 3]', '学校周边、居民小区、公园附近、商场周边都需要特别注意儿童安全。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (119, 'single', 'easy', '驾驶机动车在道路上行驶时，驾驶人应当随身携带哪种证件？', NULL, '[\"驾驶证\", \"身份证\", \"行驶证\", \"保险单\"]', 0, '[0]', '驾驶人应随身携带驾驶证。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (120, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯时，禁止直行。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '红灯亮时禁止直行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (121, 'multiple', 'medium', '驾驶机动车在雾天行驶时，应该注意什么？', NULL, '[\"开启雾灯\", \"降低车速\", \"保持安全距离\", \"使用远光灯\"]', 0, '[0, 1, 2]', '雾天行驶应开启雾灯、降低车速、保持安全距离。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (122, 'single', 'medium', '驾驶机动车在通过急弯时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '通过急弯最高速度30km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (123, 'judge', 'hard', '驾驶机动车在高速公路上行驶，可以穿越中央分隔带掉头。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '高速公路禁止穿越中央分隔带掉头。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (124, 'multiple', 'easy', '驾驶机动车在哪些情况下需要减速慢行？', NULL, '[\"通过人行横道\", \"通过学校区域\", \"通过居民小区\", \"高速公路\"]', 0, '[0, 1, 2]', '通过人行横道、学校区域、居民小区都需要减速慢行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (125, 'single', 'hard', '驾驶机动车在高速公路上行驶，违反禁令标志指示的，一次记多少分？', NULL, '[\"3分\", \"6分\", \"12分\", \"1分\"]', 0, '[0]', '违反禁令标志指示记3分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (126, 'judge', 'medium', '驾驶机动车在道路上行驶，遇到前方车辆缓慢行驶时，可以借道超车。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '车辆缓慢行驶时不得借道超车。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (127, 'multiple', 'hard', '驾驶机动车在哪些情况下需要特别注意行人？', NULL, '[\"人行横道\", \"学校区域\", \"居民小区\", \"商业区\"]', 0, '[0, 1, 2, 3]', '人行横道、学校区域、居民小区、商业区都需要特别注意行人。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (128, 'single', 'easy', '驾驶机动车在道路上临时停车时，应当如何操作？', NULL, '[\"开启危险报警闪光灯\", \"开启远光灯\", \"开启雾灯\", \"关闭所有灯光\"]', 0, '[0]', '临时停车应开启危险报警闪光灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (129, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯时，可以左转弯。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '绿灯亮时可以左转弯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (130, 'multiple', 'medium', '驾驶机动车在冰雪路面行驶时，应该采取哪些措施？', NULL, '[\"降低车速\", \"保持安全距离\", \"避免急刹车\", \"使用雪地轮胎\"]', 0, '[0, 1, 2, 3]', '冰雪路面应降低车速、保持安全距离、避免急刹车、使用雪地轮胎。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (131, 'single', 'medium', '驾驶机动车在通过陡坡时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '通过陡坡最高速度30km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (132, 'judge', 'hard', '驾驶机动车在高速公路上行驶，可以在匝道上停车休息。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '高速公路匝道禁止停车。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (133, 'multiple', 'easy', '驾驶机动车在哪些情况下需要开启危险报警闪光灯？', NULL, '[\"车辆故障\", \"临时停车\", \"牵引故障车\", \"正常行驶\"]', 0, '[0, 1, 2]', '车辆故障、临时停车、牵引故障车时需要开启危险报警闪光灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (134, 'single', 'hard', '驾驶机动车在高速公路上行驶，违反规定拖曳故障车的，一次记多少分？', NULL, '[\"3分\", \"6分\", \"12分\", \"1分\"]', 0, '[0]', '违反规定拖曳故障车记3分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (135, 'judge', 'medium', '驾驶机动车在道路上行驶，遇到非机动车时，应当减速让行。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '遇到非机动车应当减速让行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (136, 'multiple', 'hard', '驾驶机动车在哪些情况下需要特别注意老年人？', NULL, '[\"公园周边\", \"医院附近\", \"居民小区\", \"早市附近\"]', 0, '[0, 1, 2, 3]', '公园周边、医院附近、居民小区、早市附近都需要特别注意老年人。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (137, 'single', 'easy', '驾驶机动车在道路上行驶时，应当按照规定使用什么？', NULL, '[\"安全带\", \"安全头盔\", \"灭火器\", \"三角警告牌\"]', 0, '[0]', '应按规定使用安全带。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (138, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯时，可以右转弯。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '红灯亮时在不妨碍被放行车辆和行人通行的情况下可以右转弯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (139, 'multiple', 'medium', '驾驶机动车在雨天行驶时，应该注意哪些事项？', NULL, '[\"降低车速\", \"保持安全距离\", \"使用雨刮器\", \"开启雾灯\"]', 0, '[0, 1, 2, 3]', '雨天应降低车速、保持安全距离、使用雨刮器、开启雾灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (140, 'single', 'medium', '驾驶机动车在通过桥梁时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '通过桥梁最高速度30km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (141, 'judge', 'hard', '驾驶机动车在高速公路上行驶，可以骑轧车行道分界线。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '高速公路禁止骑轧车行道分界线。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (142, 'multiple', 'easy', '驾驶机动车在哪些情况下需要让行？', NULL, '[\"让右方来车先行\", \"转弯让直行\", \"相对方向左转让直行\", \"支路让干路\"]', 0, '[0, 1, 2, 3]', '让右方来车先行、转弯让直行、相对方向左转让直行、支路让干路。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (143, 'single', 'hard', '驾驶机动车在高速公路上行驶，违反规定使用专用车道的，一次记多少分？', NULL, '[\"3分\", \"6分\", \"12分\", \"1分\"]', 0, '[0]', '违反规定使用专用车道记3分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (144, 'judge', 'medium', '驾驶机动车在道路上行驶，遇到行人横过道路时，应当减速避让。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '遇到行人横过道路应当减速避让。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (145, 'multiple', 'hard', '驾驶机动车在哪些情况下需要特别注意残疾人？', NULL, '[\"残疾人专用通道\", \"康复中心附近\", \"医院附近\", \"公园入口\"]', 0, '[0, 1, 2, 3]', '残疾人专用通道、康复中心附近、医院附近、公园入口都需要特别注意残疾人。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (146, 'single', 'easy', '驾驶机动车在道路上临时停车时，车身距路缘石不得超过多少厘米？', NULL, '[\"30厘米\", \"50厘米\", \"80厘米\", \"100厘米\"]', 0, '[0]', '临时停车车身距路缘石不超过30厘米。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (147, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯时，禁止右转弯。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '红灯亮时在不妨碍被放行车辆和行人通行的情况下可以右转弯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (148, 'multiple', 'medium', '驾驶机动车在雾天行驶时，应该采取哪些安全措施？', NULL, '[\"开启雾灯\", \"降低车速\", \"保持安全距离\", \"使用远光灯\"]', 0, '[0, 1, 2]', '雾天行驶应开启雾灯、降低车速、保持安全距离。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (149, 'single', 'medium', '驾驶机动车在通过隧道时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '通过隧道最高速度30km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (150, 'judge', 'hard', '驾驶机动车在高速公路上行驶，可以倒车、逆行。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '高速公路禁止倒车、逆行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (151, 'multiple', 'easy', '驾驶机动车在哪些情况下需要开启转向灯？', NULL, '[\"起步\", \"变更车道\", \"超车\", \"靠边停车\"]', 0, '[0, 1, 2, 3]', '起步、变更车道、超车、靠边停车都需要开启转向灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (152, 'single', 'hard', '驾驶机动车在高速公路上行驶，违反规定停车上下客的，一次记多少分？', NULL, '[\"6分\", \"3分\", \"12分\", \"1分\"]', 0, '[0]', '违反规定停车上下客记6分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (153, 'judge', 'medium', '驾驶机动车在道路上行驶，遇到前方车辆停车排队等候时，可以穿插等候的车辆。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '不得穿插等候的车辆。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (154, 'multiple', 'hard', '驾驶机动车在哪些情况下需要特别注意儿童？', NULL, '[\"学校周边\", \"游乐场附近\", \"居民小区\", \"公园\"]', 0, '[0, 1, 2, 3]', '学校周边、游乐场附近、居民小区、公园都需要特别注意儿童。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (155, 'single', 'easy', '驾驶机动车在道路上行驶时，应当按照规定使用什么？', NULL, '[\"安全带\", \"安全头盔\", \"灭火器\", \"三角警告牌\"]', 0, '[0]', '应按规定使用安全带。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (156, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯时，可以直行通过。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '绿灯亮时可以直行通过。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (157, 'multiple', 'medium', '驾驶机动车在雨天行驶时，应该注意什么？', NULL, '[\"降低车速\", \"保持安全距离\", \"使用雨刮器\", \"开启雾灯\"]', 0, '[0, 1, 2, 3]', '雨天应降低车速、保持安全距离、使用雨刮器、开启雾灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (158, 'single', 'medium', '驾驶机动车在通过急弯、坡道顶端时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '通过急弯、坡道顶端最高速度30km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (159, 'judge', 'hard', '驾驶机动车在高速公路上行驶，可以在加速车道减速行驶。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '加速车道应加速至规定车速后驶入行车道，不得减速行驶。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (160, 'multiple', 'easy', '驾驶机动车在哪些情况下需要减速让行？', NULL, '[\"人行横道有行人\", \"路口有车辆\", \"学校区域\", \"高速公路\"]', 0, '[0, 1, 2]', '人行横道有行人、路口有车辆、学校区域都需要减速让行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (161, 'single', 'hard', '驾驶机动车在高速公路上行驶，违反规定使用危险报警闪光灯的，一次记多少分？', NULL, '[\"3分\", \"6分\", \"12分\", \"1分\"]', 0, '[0]', '违反规定使用危险报警闪光灯记3分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (162, 'judge', 'medium', '驾驶机动车在道路上行驶，遇到执行紧急任务的警车时，应当及时让行。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '遇到执行紧急任务的警车应当及时让行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (163, 'multiple', 'hard', '驾驶机动车在哪些情况下需要特别注意老年人？', NULL, '[\"公园\", \"医院\", \"居民小区\", \"早市\"]', 0, '[0, 1, 2, 3]', '公园、医院、居民小区、早市都需要特别注意老年人。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (164, 'single', 'easy', '驾驶机动车在道路上临时停车时，应当开启什么灯？', NULL, '[\"危险报警闪光灯\", \"示廓灯\", \"后位灯\", \"前照灯\"]', 0, '[0]', '临时停车应开启危险报警闪光灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (165, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯时，可以左转弯。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '绿灯亮时可以左转弯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (166, 'multiple', 'medium', '驾驶机动车在冰雪路面行驶时，应该注意什么？', NULL, '[\"降低车速\", \"保持安全距离\", \"避免急刹车\", \"使用雪地轮胎\"]', 0, '[0, 1, 2, 3]', '冰雪路面应降低车速、保持安全距离、避免急刹车、使用雪地轮胎。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (167, 'single', 'medium', '驾驶机动车在通过窄桥时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '通过窄桥最高速度30km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (168, 'judge', 'hard', '驾驶机动车在高速公路上行驶，可以在减速车道加速行驶。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '减速车道应减速驶离高速公路，不得加速行驶。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (169, 'multiple', 'easy', '驾驶机动车在哪些情况下需要开启危险报警闪光灯？', NULL, '[\"车辆故障\", \"临时停车\", \"牵引故障车\", \"正常行驶\"]', 0, '[0, 1, 2]', '车辆故障、临时停车、牵引故障车时需要开启危险报警闪光灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (170, 'single', 'hard', '驾驶机动车在高速公路上行驶，违反规定拖曳挂车的，一次记多少分？', NULL, '[\"3分\", \"6分\", \"12分\", \"1分\"]', 0, '[0]', '违反规定拖曳挂车记3分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (171, 'judge', 'medium', '驾驶机动车在道路上行驶，遇到非机动车时，应当减速让行。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '遇到非机动车应当减速让行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (172, 'multiple', 'hard', '驾驶机动车在哪些情况下需要特别注意残疾人？', NULL, '[\"残疾人专用通道\", \"康复中心附近\", \"医院附近\", \"公园入口\"]', 0, '[0, 1, 2, 3]', '残疾人专用通道、康复中心附近、医院附近、公园入口都需要特别注意残疾人。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (173, 'single', 'easy', '驾驶机动车在道路上行驶时，驾驶人应当随身携带哪种证件？', NULL, '[\"驾驶证\", \"身份证\", \"行驶证\", \"保险单\"]', 0, '[0]', '驾驶人应随身携带驾驶证。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (174, 'judge', 'easy', '驾驶机动车在路口遇到这种信号灯时，可以右转弯。', NULL, '[\"正确\", \"错误\"]', 0, '[0]', '红灯亮时在不妨碍被放行车辆和行人通行的情况下可以右转弯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (175, 'multiple', 'medium', '驾驶机动车在雨天行驶时，应该注意哪些事项？', NULL, '[\"降低车速\", \"保持安全距离\", \"使用雨刮器\", \"开启雾灯\"]', 0, '[0, 1, 2, 3]', '雨天应降低车速、保持安全距离、使用雨刮器、开启雾灯。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (176, 'single', 'medium', '驾驶机动车在通过急弯时，最高速度不能超过多少？', NULL, '[\"30公里/小时\", \"40公里/小时\", \"50公里/小时\", \"60公里/小时\"]', 0, '[0]', '通过急弯最高速度30km/h。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (177, 'judge', 'hard', '驾驶机动车在高速公路上行驶，可以穿越中央分隔带掉头。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '高速公路禁止穿越中央分隔带掉头。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (178, 'multiple', 'easy', '驾驶机动车在哪些情况下需要减速慢行？', NULL, '[\"通过人行横道\", \"通过学校区域\", \"通过居民小区\", \"高速公路\"]', 0, '[0, 1, 2]', '通过人行横道、学校区域、居民小区都需要减速慢行。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (179, 'single', 'hard', '驾驶机动车在高速公路上行驶，违反禁令标志指示的，一次记多少分？', NULL, '[\"3分\", \"6分\", \"12分\", \"1分\"]', 0, '[0]', '违反禁令标志指示记3分。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (180, 'judge', 'medium', '驾驶机动车在道路上行驶，遇到前方车辆缓慢行驶时，可以借道超车。', NULL, '[\"正确\", \"错误\"]', 1, '[1]', '车辆缓慢行驶时不得借道超车。', '2025-12-09 12:43:47');
INSERT INTO `questions` VALUES (181, 'multiple', 'hard', '驾驶机动车在哪些情况下需要特别注意行人？', NULL, '[\"人行横道\", \"学校区域\", \"居民小区\", \"商业区\"]', 0, '[0, 1, 2, 3]', '人行横道、学校区域、居民小区、商业区都需要特别注意行人。', '2025-12-09 12:43:47');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', 'admin@example.com', '25f43b1486ad95a1398e3eeb3d83bc4010015fcc9bedb35b432e00298d5021f7', 'admin', '2025-12-09 08:03:02');
INSERT INTO `users` VALUES (2, 'user', 'user@example.com', '0b14d501a594442a01c6859541bcb3e8164d183d32937b851835442f69d5c94e', 'user', '2025-12-09 08:03:02');
INSERT INTO `users` VALUES (3, 'Rinko24', 'uui451@gmail.com', '7d1010b263b8808ac1150c17a6b4b0bba4a5a28b97e8b98ed6842dc061d6ae2c', 'user', '2025-12-09 08:52:10');
INSERT INTO `users` VALUES (4, 'admin1', 'admin@example1.com', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'admin', '2025-12-11 09:43:39');

SET FOREIGN_KEY_CHECKS = 1;
