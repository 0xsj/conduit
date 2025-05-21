use axum::{
    extract::{Extension, Json},
    http::StatusCode
};

use serde::{Deserialize, Serialize};
use std::sync::Arc;
use crate::domain::auth::{AuthToken, Credentials};
use crate::ports::primary::auth_service::AuthService;

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

impl From<AuthToken> for TokenResponse {
    fn from(token: AuthToken) -> Self {
        Self {
            token: token.token,
            expires_at: token.expires_at,
        }
    }
}

pub async fn login(
    Extension(service): Extension<Arc<dyn AuthService + Send + Sync>>,
    Json(payload): Json<LoginRequest>,
) -> Result<Json<TokenResponse>, StatusCode> {
    let credentials = Credentials {
        username: payload.username,
        password: payload.password
    };

    match service.login(credentials).await {
        Ok(token) => Ok(Json(TokenResponse::from(token))),
        Err(_) => Err(StatusCode::UNAUTHORIZED)
    }
}

pub async fn validate_token(
    Extension(service): Extension<Arc<dyn AuthService + Send + Sync>>,
    Json(token): Json<String>,
) -> Result<StatusCode, StatusCode> {
    match service.validate_token(&token).await {
        Ok(true) => Ok(StatusCode::OK),
        Ok(false) => Err(StatusCode::UNAUTHORIZED),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR)
    }
}