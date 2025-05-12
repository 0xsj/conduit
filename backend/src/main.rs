mod adapters;
mod config;
mod domain;
mod ports;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Initialize tracing for logs
    tracing_subscriber::fmt::init();
    
    // Create application config
    let config = config::app::AppConfig::default();
    
    // Start HTTP server
    adapters::primary::http::routes::start_server(config).await?;
    
    Ok(())
}