use async_trait::async_trait;
use crate::domain::auth::{AuthToken, Credentials};

#[async_trait]
pub trait AuthRepository {
    async fn validate_credentials(&self, credentials: &Credentials) -> Result<bool, String>;
    async fn create_token(&self, username: &str) -> Result<AuthToken, String>;
    async fn validate_token(&self, token: &str) -> Result<bool, String>;
}