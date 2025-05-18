use axum::{
    routing::{get, post},
    Router,
};
use std::net::SocketAddr;
use tower_http::cors::{Any, CorsLayer};

use crate::config::{app::AppConfig, di::AppServices};
use crate::adapters::primary::http::{user_handler, auth_handler};

pub async fn start_server(config: AppConfig) -> Result<(), Box<dyn std::error::Error>> {


    // Enable CORS
    let cors = CorsLayer::new()
        .allow_origin(Any)
        .allow_methods(Any)
        .allow_headers(Any);

    // Create router with routes
    let app = Router::new()
        // Health check
        .route("/health", get(health_check))
        
        // User routes
        .route("/api/users", get(user_handler::list_users))
        .route("/api/users", post(user_handler::create_user))
        .route("/api/users/{id}", get(user_handler::get_user)) // FIXED: changed :id to {id}
        
        // Auth routes
        .route("/api/auth/login", post(auth_handler::login))
        
        // Apply CORS middleware
        .layer(cors);

    // Get bind address from config
    let addr = SocketAddr::from(([127, 0, 0, 1], config.port));
    
    println!("Server listening on {}", addr);
    
    // Start server
    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;
    
    Ok(())
}

// Health check handler
async fn health_check() -> &'static str {
    "OK"
}