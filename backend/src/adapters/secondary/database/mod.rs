// backend/src/adapters/secondary/database/mod.rs
use sqlx::{MySqlPool, mysql::MySqlPoolOptions, migrate::MigrateError};

pub mod errors;

pub async fn create_pool(database_url: &str) -> Result<MySqlPool, sqlx::Error> {
    MySqlPoolOptions::new()
        .max_connections(10)
        .min_connections(1)
        .max_lifetime(std::time::Duration::from_secs(1800)) // 30 minutes
        .idle_timeout(std::time::Duration::from_secs(600))  // 10 minutes
        .connect(database_url)
        .await
}

pub async fn run_migrations(pool: &MySqlPool) -> Result<(), MigrateError> {
    sqlx::migrate!("./migrations")
        .run(pool)
        .await
}