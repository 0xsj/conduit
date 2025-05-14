// src/error.rs

use std::fmt;
use std::error::Error as StdError;

/// Application error types
#[derive(Debug)]
pub enum AppError {
    /// Redis-related errors
    Redis(String),
    /// RabbitMQ-related errors
    RabbitMQ(String),
    /// Database-related errors
    Database(String),
    /// Configuration errors
    Config(String),
    /// I/O errors
    IO(std::io::Error),
    /// Other errors
    Other(String),
}

impl fmt::Display for AppError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Redis(msg) => write!(f, "Redis error: {}", msg),
            Self::RabbitMQ(msg) => write!(f, "RabbitMQ error: {}", msg),
            Self::Database(msg) => write!(f, "Database error: {}", msg),
            Self::Config(msg) => write!(f, "Configuration error: {}", msg),
            Self::IO(err) => write!(f, "I/O error: {}", err),
            Self::Other(msg) => write!(f, "{}", msg),
        }
    }
}

impl StdError for AppError {
    fn source(&self) -> Option<&(dyn StdError + 'static)> {
        match self {
            Self::IO(err) => Some(err),
            _ => None,
        }
    }
}

// Implement From conversions for common error types
impl From<std::io::Error> for AppError {
    fn from(err: std::io::Error) -> Self {
        Self::IO(err)
    }
}

impl From<redis::RedisError> for AppError {
    fn from(err: redis::RedisError) -> Self {
        Self::Redis(err.to_string())
    }
}

// Result type alias for convenience
pub type AppResult<T> = Result<T, AppError>;