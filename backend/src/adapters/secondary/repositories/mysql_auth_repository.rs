// // backend/src/adapters/secondary/repositories/mysql_auth_repository.rs
// use async_trait::async_trait;
// use sqlx::MySqlPool;
// use chrono::{Utc, Duration};

// use crate::domain::auth::{AuthToken, Credentials};
// use crate::ports::secondary::auth_repository::AuthRepository;

// pub struct MySqlAuthRepository {
//     pool: MySqlPool,
// }

// impl MySqlAuthRepository {
//     pub fn new(pool: MySqlPool) -> Self {
//         Self { pool }
//     }
// }

// #[async_trait]
// impl AuthRepository for MySqlAuthRepository {
//     async fn validate_credentials(&self, credentials: &Credentials) -> Result<bool, String> {
//         // In a real application, you would hash the password before comparison
//         let result = sqlx::query!(
//             r#"
//             SELECT COUNT(*) as count
//             FROM users
//             WHERE username = ? AND password = ?
//             "#,
//             credentials.username,
//             credentials.password
//         )
//         .fetch_one(&self.pool)
//         .await
//         .map_err(|e| e.to_string())?;

//         Ok(result.count > 0)
//     }

//     async fn create_token(&self, username: &str) -> Result<AuthToken, String> {
//         let now = Utc::now();
//         let expires_at = now + Duration::hours(1);
//         let token = format!("token-{}-{}", username, now.timestamp());
//         let expires_at_str = expires_at.to_rfc3339();

//         // Store token in database
//         sqlx::query!(
//             r#"
//             INSERT INTO auth_tokens (token, username, expires_at)
//             VALUES (?, ?, ?)
//             "#,
//             token,
//             username,
//             expires_at_str
//         )
//         .execute(&self.pool)
//         .await
//         .map_err(|e| e.to_string())?;

//         Ok(AuthToken {
//             token,
//             expires_at: expires_at_str,
//         })
//     }

//     async fn validate_token(&self, token: &str) -> Result<bool, String> {
//         let now = Utc::now().to_rfc3339();

//         let result = sqlx::query!(
//             r#"
//             SELECT COUNT(*) as count
//             FROM auth_tokens
//             WHERE token = ? AND expires_at > ?
//             "#,
//             token,
//             now
//         )
//         .fetch_one(&self.pool)
//         .await
//         .map_err(|e| e.to_string())?;

//         Ok(result.count > 0)
//     }
// }