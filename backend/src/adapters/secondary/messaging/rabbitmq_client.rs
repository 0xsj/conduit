use lapin::{
    Connection, ConnectionProperties,
    options::{BasicPublishOptions, BasicConsumeOptions, QueueDeclareOptions},
    types::FieldTable,
    protocol::basic::AMQPProperties,
};
use std::sync::Arc;
use futures_lite::stream::StreamExt;

pub struct RabbitMQClient {
    connection: Connection,
}

impl RabbitMQClient {
    pub async fn new(amqp_url: &str) -> Result<Self, lapin::Error> {
        let connection = Connection::connect(amqp_url, ConnectionProperties::default()).await?;
        Ok(Self { connection })
    }

    pub async fn publish(&self, queue_name: &str, payload: &[u8]) -> Result<(), String> {
        let channel = self.connection.create_channel().await.map_err(|e| e.to_string())?;
        
        // Declare queue
        channel.queue_declare(
            queue_name,
            QueueDeclareOptions {
                durable: true,
                ..Default::default()
            },
            FieldTable::default(),
        )
        .await
        .map_err(|e| e.to_string())?;
        
        // Publish message
        channel.basic_publish(
            "",
            queue_name,
            BasicPublishOptions::default(),
            payload,
            AMQPProperties::default(),
        )
        .await
        .map_err(|e| e.to_string())?;
        
        Ok(())
    }

    pub async fn subscribe<F>(&self, queue_name: &str, callback: F) -> Result<(), String>
    where
        F: Fn(&[u8]) -> Result<(), String> + Send + Sync + 'static,
    {
        let channel = self.connection.create_channel().await.map_err(|e| e.to_string())?;
        
        // Declare queue
        channel.queue_declare(
            queue_name,
            QueueDeclareOptions {
                durable: true,
                ..Default::default()
            },
            FieldTable::default(),
        )
        .await
        .map_err(|e| e.to_string())?;
        
        // Consume messages
        let mut consumer = channel.basic_consume(
            queue_name,
            "consumer",
            BasicConsumeOptions::default(),
            FieldTable::default(),
        )
        .await
        .map_err(|e| e.to_string())?;
        
        let callback = Arc::new(callback);
        tokio::spawn(async move {
            while let Some(delivery) = consumer.next().await {
                if let Ok(delivery) = delivery {
                    let callback = Arc::clone(&callback);
                    let data = delivery.data.clone();
                    if let Err(e) = callback(&data) {
                        eprintln!("Error processing message: {}", e);
                    }
                    if let Err(e) = delivery.ack(Default::default()).await {
                        eprintln!("Error acknowledging message: {}", e);
                    }
                }
            }
        });
        
        Ok(())
    }
}