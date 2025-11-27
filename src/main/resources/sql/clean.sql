-- ====================================================
-- OIDC Server 数据清理脚本
-- 用于清空所有表数据，保留表结构
-- 执行后重启服务，DataInitializer会重新初始化数据
-- ====================================================

USE oauth2;

-- 关闭外键检查（如果有外键约束）
SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有表数据
TRUNCATE TABLE access_tokens;
TRUNCATE TABLE authorization_codes;
TRUNCATE TABLE oauth2_clients;
TRUNCATE TABLE users;

-- 开启外键检查
SET FOREIGN_KEY_CHECKS = 1;

SELECT '数据清理完成！重启服务后DataInitializer会自动初始化数据。' AS '';
