pub struct AppConfig {
    pub host: String,
    pub port: u16,
}

impl Default for AppConfig {
    fn default() -> Self {
        Self {
            host: "127.0.0.1".to_string(),
            port: 3000,
        }
    }
}

impl AppConfig {
    pub fn create(host: String, port: u16) -> Self {
        Self { host, port }
    }
}