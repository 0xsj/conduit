use axum::{
    extract::Json,
    http::StatusCode,
};
use serde::{Deserialize, Serialize};
use chrono::{Utc, Duration};

use crate::domain::auth::{Credentials, AuthToken};

#[derive(Deserialize)]
pub struct LoginRequest {
    username: String,
    password: String,
}

#[derive(Serialize)]
pub struct TokenResponse {
    token: String,
    expires_at: String,
}

// Hardcoded credentials for demo
const DEMO_USERNAME: &str = "demo";
const DEMO_PASSWORD: &str = "password";

pub async fn login(
    Json(payload): Json<LoginRequest>,
) -> Result<Json<TokenResponse>, StatusCode> {
    // Check if credentials are valid
    if payload.username == DEMO_USERNAME && payload.password == DEMO_PASSWORD {
        // Generate a simple token (in a real app, use JWT)
        let now = Utc::now();
        let expires_at = now + Duration::hours(1);
        
        let token = AuthToken {
            token: format!("demo-token-{}", now.timestamp()),
            expires_at: expires_at.to_rfc3339(),
        };
        
        Ok(Json(TokenResponse {
            token: token.token,
            expires_at: token.expires_at,
        }))
    } else {
        Err(StatusCode::UNAUTHORIZED)
    }
}