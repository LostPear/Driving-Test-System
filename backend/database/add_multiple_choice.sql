-- 添加多选题类型支持
-- 执行此脚本前请先备份数据库

USE exam_db;

-- 修改题目表，添加multiple类型，并将correct_answer改为JSON格式以支持多个正确答案
ALTER TABLE questions MODIFY COLUMN type ENUM('single', 'judge', 'multiple') NOT NULL;

-- 如果correct_answer字段是INT类型，需要先添加新字段，迁移数据，然后删除旧字段
-- 这里我们使用JSON字段来存储正确答案（单个答案存储为[0]，多个答案存储为[0,1,2]）
ALTER TABLE questions ADD COLUMN correct_answers JSON AFTER correct_answer;

-- 将现有的correct_answer转换为JSON格式
UPDATE questions SET correct_answers = JSON_ARRAY(correct_answer);

-- 删除旧的correct_answer字段（可选，为了兼容性可以保留）
-- ALTER TABLE questions DROP COLUMN correct_answer;

-- 注意：为了向后兼容，我们暂时保留correct_answer字段
-- 在实际使用中，优先使用correct_answers字段

