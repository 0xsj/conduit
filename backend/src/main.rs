mod adapters;
mod config;
mod domain;
mod ports;
mod error;

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    tracing_subscriber::fmt::init();
    
    let config = config::app::AppConfig::default();
    
    adapters::primary::http::routes::start_server(config).await?;
    
    Ok(())
}