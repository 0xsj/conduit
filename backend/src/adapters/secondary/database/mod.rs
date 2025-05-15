use sqlx::{MySqlPool, mysql::MySqlPoolOptions};

pub async fn create_pool(database_url: &str) -> Result<MySqlPool, sqlx::Error> {
    MySqlPoolOptions::new()
        .max_connections(10)        // Maximum number of connections in the pool
        .min_connections(1)         // Minimum idle connections to maintain
        .max_lifetime(std::time::Duration::from_secs(1800)) // Max connection lifetime (30 min)
        .idle_timeout(std::time::Duration::from_secs(600))  // Idle timeout (10 min)
        .connect(database_url)
        .await
}

pub async fn run_migrations(pool: &MySqlPool) -> Result<(), sqlx::Error> {
    sqlx::migrate!("./migrations")
        .run(pool)
        .await
}