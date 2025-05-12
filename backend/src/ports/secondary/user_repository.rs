use async_trait::async_trait;
use crate::domain::user::User;

#[async_trait]
pub trait UserRepository {
    async fn find_by_id(&self, id: &str) -> Result<Option<User>, String>;
    async fn create(&self, user: User) -> Result<User, String>;
    async fn find_all(&self) -> Result<Vec<User>, String>;
}