use axum::{
    extract::{Path, Json},
    http::StatusCode,
};
use serde::{Deserialize, Serialize};
use std::sync::Mutex;
use once_cell::sync::Lazy;

use crate::domain::user::User;

// Thread-safe storage using once_cell and Mutex
static USERS: Lazy<Mutex<Vec<User>>> = Lazy::new(|| Mutex::new(Vec::new()));

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
            email: user.email.clone(),
        }
    }
}

pub async fn create_user(
    Json(payload): Json<CreateUserRequest>,
) -> Result<Json<UserResponse>, StatusCode> {
    // Create a new user
    let user = User::new(payload.username, payload.email);
    
    // Store user
    let mut users = USERS.lock().map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;
    users.push(user.clone());
    
    // Return user data
    Ok(Json(UserResponse::from(user)))
}

pub async fn get_user(
    Path(id): Path<String>,
) -> Result<Json<UserResponse>, StatusCode> {
    // Find user by ID
    let users = USERS.lock().map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;
    let user = users.iter()
        .find(|u| u.id == id)
        .cloned();
    
    // Return user or 404
    match user {
        Some(user) => Ok(Json(UserResponse::from(user))),
        None => Err(StatusCode::NOT_FOUND),
    }
}

pub async fn list_users() -> Result<Json<Vec<UserResponse>>, StatusCode> {
    // Get all users
    let users = USERS.lock().map_err(|_| StatusCode::INTERNAL_SERVER_ERROR)?;
    let user_responses: Vec<UserResponse> = users.iter()
        .cloned()
        .map(UserResponse::from)
        .collect();
    
    Ok(Json(user_responses))
}