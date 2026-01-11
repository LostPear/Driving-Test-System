-- ----------------------------
-- 优化 exam 表结构，增加 exam_questions 关联表
-- ----------------------------

-- 1. 创建 exam_questions 表
CREATE TABLE IF NOT EXISTS `exam_questions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `exam_id` int(11) NOT NULL,
  `question_id` int(11) NOT NULL,
  `user_answer` int(11) DEFAULT NULL COMMENT '用户的答案选项索引',
  `is_correct` tinyint(1) DEFAULT 0 COMMENT '是否回答正确',
  `item_score` int(11) DEFAULT 0 COMMENT '该题得分',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_id`(`exam_id`) USING BTREE,
  INDEX `idx_question_id`(`question_id`) USING BTREE,
  CONSTRAINT `fk_exam_questions_exam` FOREIGN KEY (`exam_id`) REFERENCES `exams` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_exam_questions_question` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- 说明：
-- 原 exams 表中的 questions 和 answers 字段为 JSON 格式，不利于 ER 图展示关系。
-- 新增 exam_questions 表后，exams 表与 questions 表通过 exam_questions 表建立多对多关系（带属性）。
-- 这种结构在 ER 图中更加清晰，符合第三范式。
