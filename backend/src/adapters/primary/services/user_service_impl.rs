use::async_trait::async_trait;
use std::sync::Arc;

use crate::domain::user::User;
use crate::ports::primary::user_service::UserService;
use crate::ports::secondary::user_repository::UserRepository;

pub struct UserServiceImpl {
    user_repository: Arc<dyn UserRepository + Send + Sync>,
}

#[async_trait]
impl UserService for UserServiceImpl {
    async fn get_user(&self, id: &str) -> Result<Option<User>, String> {
        self.user_repository.find_by_id(id).await
    }

    async fn create_user(&self, username: String, email: String) -> Result<User, String> {
        let user = User::new(username, email);

        self.user_repository.create(user.clone()).await?;

        Ok(user)
    }

    async fn list_users(&self) -> Result<Vec<User>, String> {
        self.user_repository.find_all().await
    }
}