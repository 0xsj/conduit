// backend/src/adapters/secondary/cache/redis_client.rs
use std::time::Duration;
use std::sync::Arc;

#[derive(Clone)]
pub struct RedisClient {
    // Wrap in Arc to ensure it can be cloned
    client: Arc<redis::Client>,
}

impl RedisClient {
    pub fn new(redis_url: &str) -> Result<Self, redis::RedisError> {
        let client = redis::Client::open(redis_url)?;
        Ok(Self { client: Arc::new(client) })
    }
    
    pub async fn get<T: serde::de::DeserializeOwned>(&self, key: &str) -> Result<Option<T>, String> {
        // Use get_multiplexed_async_connection or get_async_connection
        let mut conn = self.client.get_multiplexed_async_connection().await
            .map_err(|e| e.to_string())?;
            
        // Let's use the cmd interface which is more stable across versions
        let result: redis::RedisResult<Option<String>> = redis::cmd("GET")
            .arg(key)
            .query_async(&mut conn)
            .await;
        
        match result {
            Ok(Some(data)) => {
                serde_json::from_str(&data)
                    .map_err(|e| e.to_string())
                    .map(Some)
            },
            Ok(None) => Ok(None),
            Err(_) => Ok(None),
        }
    }
    
    pub async fn set<T: serde::Serialize>(&self, key: &str, value: &T, ttl_seconds: Option<u64>) -> Result<(), String> {
        let serialized = serde_json::to_string(value).map_err(|e| e.to_string())?;
        let mut conn = self.client.get_multiplexed_async_connection().await
            .map_err(|e| e.to_string())?;
            
        if let Some(ttl) = ttl_seconds {
            redis::cmd("SETEX")
                .arg(key)
                .arg(ttl as usize)
                .arg(&serialized)
                .query_async(&mut conn)
                .await
                .map_err(|e| e.to_string())?;
        } else {
            redis::cmd("SET")
                .arg(key)
                .arg(&serialized)
                .query_async(&mut conn)
                .await
                .map_err(|e| e.to_string())?;
        }
        
        Ok(())
    }
    
    pub async fn delete(&self, key: &str) -> Result<(), String> {
        let mut conn = self.client.get_multiplexed_async_connection().await
            .map_err(|e| e.to_string())?;
            
        redis::cmd("DEL")
            .arg(key)
            .query_async(&mut conn)
            .await
            .map_err(|e| e.to_string())?;
            
        Ok(())
    }
}