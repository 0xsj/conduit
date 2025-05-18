// backend/src/main.rs
mod adapters;
mod config;
mod domain;
mod ports;
mod error;

use std::error::Error as StdError;

#[tokio::main]
async fn main() -> Result<(), Box<dyn StdError>> {
    // Set up tracing
    tracing_subscriber::fmt::init();
    
    // Load environment variables
    dotenv::dotenv().ok();
    
    // Get database URL from environment or use default
    let db_url = std::env::var("DATABASE_URL")
        .unwrap_or_else(|_| "mysql://apiuser:apipassword@localhost:3307/rust_api".to_string());
    
    // Create DI container with MySQL repositories
    let di = match config::di::DI::new_with_mysql(&db_url).await {
        Ok(di) => di,
        Err(e) => {
            eprintln!("Failed to initialize database: {}", e);
            return Err(Box::<dyn StdError>::from(std::io::Error::new(std::io::ErrorKind::Other, e)));
        }
    };
    
    // Check for optional Redis and RabbitMQ configuration
    let redis_url = std::env::var("REDIS_URL").ok();
    let rabbitmq_url = std::env::var("RABBITMQ_URL").ok();
    
    // Add Redis if configured
    // Using the builder pattern with cloning for error recovery
    let di = if let Some(url) = redis_url {
        let di_clone = di.clone(); // Clone before potential move
        match di.with_redis(&url).await {
            Ok(new_di) => {
                println!("Redis connection established");
                new_di
            },
            Err(e) => {
                eprintln!("Warning: Failed to connect to Redis: {}", e);
                di_clone // Use the clone if there was an error
            }
        }
    } else {
        di
    };
    
    // Add RabbitMQ if configured
    // Using the builder pattern with cloning for error recovery
    let di = if let Some(url) = rabbitmq_url {
        let di_clone = di.clone(); // Clone before potential move
        match di.with_rabbitmq(&url).await {
            Ok(new_di) => {
                println!("RabbitMQ connection established");
                new_di
            },
            Err(e) => {
                eprintln!("Warning: Failed to connect to RabbitMQ: {}", e);
                di_clone // Use the clone if there was an error
            }
        }
    } else {
        di
    };
    
    let port = std::env::var("PORT")
        .ok()
        .and_then(|p| p.parse::<u16>().ok())
        .unwrap_or(3000);
    
    let config = config::app::AppConfig::create("127.0.0.1".to_string(), port);
    
    println!("Starting server with configuration:");
    println!("  Port: {}", config.port);
    println!("  Database: Configured");
    println!("  Redis: {}", if di.services.redis_client.is_some() { "Configured" } else { "Not configured" });
    println!("  RabbitMQ: {}", if di.services.rabbitmq_client.is_some() { "Configured" } else { "Not configured" });
    
    if let Err(e) = adapters::primary::http::routes::start_server(config, di.services).await {
        eprintln!("Server error: {}", e);
        std::process::exit(1);
    }
    
    Ok(())
}