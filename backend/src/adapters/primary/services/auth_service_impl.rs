use async_trait::async_trait;
use std::sync::Arc;

use crate::domain::auth::{AuthToken, Credentials};
use crate::ports::primary::auth_service::AuthService;
use crate::ports::secondary::auth_repository::AuthRepository;

pub struct AuthServiceImpl {
    auth_repository: Arc<dyn AuthRepository + Send + Sync>,
}

impl AuthServiceImpl {
    pub fn new(auth_repository: Arc<dyn AuthRepository + Send + Sync>) -> Self {
        Self { auth_repository }
    }
}

#[async_trait]
impl AuthService for AuthServiceImpl {
    async fn login(&self, credentials: Credentials) -> Result<AuthToken, String> {
        let is_valid = self.auth_repository.validate_credentials(&credentials).await?;
        
        if !is_valid {
            return Err("Invalid credentials".to_string());
        }
        
        self.auth_repository.create_token(&credentials.username).await
    }

     async fn validate_token(&self, token: &str) -> Result<bool, String> {
        self.auth_repository.validate_token(token).await
    }
}