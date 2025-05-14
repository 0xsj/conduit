use std::sync::Arc;
use crate::error::{AppError, AppResult};

use crate::ports::primary::{UserService, AuthService};
use crate::ports::secondary::{UserRepository, AuthRepository};

use crate::adapters::primary::services::{UserServiceImpl, AuthServiceImpl};

use crate::adapters::secondary::repositories::{

};

use crate::adapters::secondary::cache::redis_client::{self, RedisClient};
use crate::adapters::secondary::messaging::rabbitmq_client::{self, RabbitMQClient};

pub struct AppServices {
    pub user_service: Arc<dyn UserService + Send + Sync>,
    pub auth_service: Arc<dyn AuthService + Send + Sync>,

    pub redis_client: Option<RedisClient>,
    pub rabbitmq_client: Option<RabbitMQClient>
}


pub struct DI {
    pub services: AppServices
}

impl DI {
    pub async fn new_in_memory() -> Result<Self, AppError> {
        unimplemented!("In-memory DI not yet implemented")
    }

    pub async fn new_with_mysql(db_url: &str) -> Result<Self, AppError> {
        unimplemented!("MySQL DI not yet implemented")
    } 

    pub async fn with_redis(mut self, redis_url: &str) -> Result<Self, AppError> {
        let redis_client = RedisClient::new(redis_url)?;
        self.services.redis_client = Some(redis_client);
        Ok(self)
    }

      pub async fn with_rabbitmq(mut self, rabbitmq_url: &str) -> Result<Self, AppError> {
        let rabbitmq_client = RabbitMQClient::new(rabbitmq_url).await
            .map_err(|e| AppError::RabbitMQ(e.to_string()))?;
        self.services.rabbitmq_client = Some(rabbitmq_client);
        Ok(self)
    }
}


pub async fn get_services() -> Result<AppServices, AppError> {
    let db_url = std::env::var("DATABASE_URL").unwrap_or_else(|_| "mysql://user:pass@localhost/db".to_string());
    let redis_url = std::env::var("REDIS_URL").ok();
    let rabbitmq_url = std::env::var("RABBITMQ_URL").ok();
    
    let mut di = DI::new_in_memory().await?;
    
    if let Some(redis_url) = redis_url {
        di = di.with_redis(&redis_url).await?;
    }
    
    if let Some(rabbitmq_url) = rabbitmq_url {
        di = di.with_rabbitmq(&rabbitmq_url).await?;
    }
    
    Ok(di.services)
}