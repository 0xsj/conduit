use async_trait::async_trait;
use crate::domain::user::User;

#[async_trait]
pub trait UserService {
    async fn get_user(&self, id: &str) -> Result<Option<User>, String>;
    async fn create_user(&self, username: String, email: String) -> Result<User, String>;
    async fn list_users(&self) -> Result<Vec<User>, String>;
}