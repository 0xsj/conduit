mod domain;
mod ports;
mod adapters;
mod config;

use adapters::primary::http::routes;
use config::app::AppConfig;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Initialize logger
    tracing_subscriber::fmt::init();
    
    // Load configuration
    let config = AppConfig::load()?;
    
    // Start the HTTP server
    routes::start_server(config).await?;
    
    Ok(())
}
