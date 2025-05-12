// src/adapters/primary/http/routes.rs
use axum::{
    routing::get,
    Router,
};
use std::net::SocketAddr;

use crate::config::app::AppConfig;

pub async fn start_server(config: AppConfig) -> Result<(), Box<dyn std::error::Error>> {
    // Simple router with a health check
    let app = Router::new()
        .route("/", get(|| async { "Hello, World!" }))
        .route("/health", get(|| async { "OK" }));

    // Get bind address from config
    let addr = SocketAddr::from(([127, 0, 0, 1], config.port));
    
    println!("Server listening on {}", addr);
    
    // Start server - Axum 0.8.x version
    let listener = tokio::net::TcpListener::bind(addr).await?;
    axum::serve(listener, app).await?;
    
    Ok(())
}