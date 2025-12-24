-- 为questions表添加image字段，用于存储图片路径
ALTER TABLE questions ADD COLUMN image VARCHAR(500) DEFAULT NULL AFTER question;

-- 创建索引以优化查询
CREATE INDEX idx_image ON questions(image(255));


