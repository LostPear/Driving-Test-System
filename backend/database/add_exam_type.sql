-- 为exams表添加type字段，用于区分练习和考试
ALTER TABLE exams ADD COLUMN type ENUM('practice', 'exam') DEFAULT 'exam' AFTER user_id;

-- 更新现有记录，默认为exam类型
UPDATE exams SET type = 'exam' WHERE type IS NULL;

