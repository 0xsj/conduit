use async_trait::async_trait;
use crate::domain::auth::{AuthToken, Credentials};

#[async_trait]
pub trait AuthService {
    async fn login(&self, credentials: Credentials) -> Result<AuthToken, String>;
    async fn validate_token(&self, token: &str) -> Result<bool, String>;
}