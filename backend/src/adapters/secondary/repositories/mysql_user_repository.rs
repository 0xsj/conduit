use async_trait::async_trait;
use sqlx::MySqlPool;

use crate::domain::user::User;
use crate::ports::secondary::user_repository::UserRepository;

pub struct MySqlUserRepository {
    pool: MySqlPool,
}

impl MySqlUserRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool}
    }
}

#[async_trait]
impl UserRepository for MySqlUserRepository {
    async fn find_by_id(&self, id: &str) -> Result<Option<User>, String> {
        let result = sqlx::query_as!(
            User,
            r#"
            SELECT id, username, email
            FROM users
            WHERE id = ?
            "#,
            id
        )
        .fetch_optional(&self.pool)
        .await
        .map_err(|e| e.to_string());

        result
    }

    async fn create(&self, user: User) -> Result<User, String> {
        sqlx::query!(
            r#"
            INSERT INTO users (id, username, email)
            VALUES (?, ?, ?)
            "#,
            user.id,
            user.username,
            user.email
        )
        .execute(&self.pool)
        .await
        .map_err(|e| e.to_string())?;

        Ok(user)
    }

    async fn find_all(&self) -> Result<Vec<User>, String> {
        let result = sqlx::query_as!(
            User,
            r#"
            SELECT id, username, email
            FROM users
            "#
        )
        .fetch_all(&self.pool)
        .await
        .map_err(|e| e.to_string());

        result
    }
}