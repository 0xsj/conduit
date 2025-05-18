use axum::{
    extract::{Json, Path},
    http::StatusCode, Extension,
};
use serde::{Deserialize, Serialize};
use std::sync::Arc;

use crate::ports::primary::user_service::UserService;
use crate::domain::user::User;

#[derive(Deserialize)]
pub struct CreateUserRequest {
    username: String,
    email: String,
}

#[derive(Serialize)]
pub struct UserResponse {
    id: String,
    username: String,
    email: String,
}

impl From<User> for UserResponse {
    fn from(user: User) -> Self {
        Self {
            id: user.id.clone(),
            username: user.username.clone(),
            email: user.email.clone()
        }
    }
}

pub async fn create_user(
    Extension(service): Extension<Arc<dyn UserService + Send + Sync>>,
    Json(payload): Json<CreateUserRequest>,
) -> Result<Json<UserResponse>, StatusCode> {
    match service.create_user(payload.username, payload.email).await {
        Ok(user) => Ok(Json(UserResponse::from(user))),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

pub async fn get_user(
    Extension(service): Extension<Arc<dyn UserService + Send + Sync>>,
    Path(id): Path<String>,
) -> Result<Json<UserResponse>, StatusCode> {
    match service.get_user(&id).await {
        Ok(Some(user)) => Ok(Json(UserResponse::from(user))),
        Ok(None) => Err(StatusCode::NOT_FOUND),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR)
    }
}

pub async fn list_users(
    Extension(service): Extension<Arc<dyn UserService + Send + Sync>>,

) -> Result<Json<Vec<UserResponse>>, StatusCode> {
    match service.list_users().await {
        Ok(users) => {
            let user_responses = users.into_iter()
                .map(UserResponse::from)
                .collect();
            Ok(Json(user_responses))
        },
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR)
    }
}