use axum::{
    routing::{get, post},
    Router,
    Extension
};
use redis::io::tcp::socket2::Socket;

use std::net::SocketAddr;
use std::sync::Arc;
use tower_http::cors::{Any, CorsLayer};

use crate::config::app::AppConfig;
use crate::config::di::AppServices;
use crate::adapters::primary::http::{user_handler, auth_handler};

pub async fn start_server(
    config: AppConfig,
    services: AppServices,
) -> Result<(), Box<dyn std::error::Error>> {
    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(Any)
        .allow_headers(Any);

    let app = Router::new()
        // Health check
        .route("/health", get(health_check))
        
        // User routes
        .route("/api/users", get(user_handler::list_users))
        .route("/api/users", post(user_handler::create_user))
        .route("/api/users/{id}", get(user_handler::get_user))
        
        // Auth routes
        .route("/api/auth/login", post(auth_handler::login))
        .route("/api/auth/validate", post(auth_handler::validate_token))
        
        // Add service dependencies
        .layer(Extension(services.user_service))
        .layer(Extension(services.auth_service))
        
        // Apply CORS middleware
        .layer(cors);

    let addr = SocketAddr::from(([127, 0, 0, 1], config.port));
    println!("Server listening on {}", addr);

    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;

    Ok(())
}

async fn health_check() -> &'static str {
    "OK"
}