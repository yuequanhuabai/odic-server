-- ====================================================
-- OIDC Server Database Schema
-- 创建时间: 2025-11-27
-- 数据库: oauth2
-- ====================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS oauth2 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE oauth2;

-- ====================================================
-- 1. 用户表 (users)
-- 存储OIDC用户的基本信息
-- ====================================================
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    email VARCHAR(100) COMMENT '邮箱',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ====================================================
-- 2. OAuth客户端表 (oauth2_clients)
-- 存储OAuth2客户端应用信息
-- ====================================================
DROP TABLE IF EXISTS oauth2_clients;

CREATE TABLE oauth2_clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客户端ID',
    client_id VARCHAR(100) NOT NULL UNIQUE COMMENT '客户端标识',
    client_secret VARCHAR(255) NOT NULL COMMENT '客户端密钥',
    redirect_uris VARCHAR(500) NOT NULL COMMENT '重定向URI列表(逗号分隔)',
    client_name VARCHAR(100) COMMENT '客户端名称',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_client_id (client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OAuth2客户端表';

-- ====================================================
-- 3. 授权码表 (authorization_codes)
-- 存储OAuth2授权码流程中的授权码
-- ====================================================
DROP TABLE IF EXISTS authorization_codes;

CREATE TABLE authorization_codes (
    code VARCHAR(255) PRIMARY KEY COMMENT '授权码',
    client_id VARCHAR(100) NOT NULL COMMENT '客户端标识',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    redirect_uri VARCHAR(255) NOT NULL COMMENT '重定向URI',
    scope VARCHAR(255) COMMENT '授权范围',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_client_id (client_id),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='授权码表';

-- ====================================================
-- 4. 访问令牌表 (access_tokens)
-- 存储OAuth2访问令牌
-- ====================================================
DROP TABLE IF EXISTS access_tokens;

CREATE TABLE access_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '令牌ID',
    token VARCHAR(500) NOT NULL COMMENT 'JWT令牌',
    client_id VARCHAR(100) COMMENT '客户端标识',
    user_id BIGINT COMMENT '用户ID',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_token (token(255)),
    INDEX idx_client_id (client_id),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访问令牌表';

-- ====================================================
-- 外键约束（可选，根据需要启用）
-- ====================================================
-- ALTER TABLE authorization_codes ADD CONSTRAINT fk_auth_code_user
--     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- ALTER TABLE access_tokens ADD CONSTRAINT fk_access_token_user
--     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
