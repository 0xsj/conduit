-- Create Users Table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255)
);

-- Create Auth Tokens Table
CREATE TABLE IF NOT EXISTS auth_tokens (
    token VARCHAR(255) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    expires_at VARCHAR(50) NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username)
);