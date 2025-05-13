// load-balancer/src/main.rs
use std::io::{Read, Write};
use std::net::{TcpListener, TcpStream};
use std::sync::{Arc, Mutex};
use std::thread;
use std::time::Duration;

#[derive(Clone)]
struct LoadBalancer {
    // List of backend servers
    backends: Vec<String>,
    // Current backend index for round-robin
    current: Arc<Mutex<usize>>,
    // Health check status
    health: Arc<Mutex<Vec<bool>>>,
}

impl LoadBalancer {
    fn new(backends: Vec<String>) -> Self {
        let health = vec![true; backends.len()];
        
        LoadBalancer {
            backends,
            current: Arc::new(Mutex::new(0)),
            health: Arc::new(Mutex::new(health)),
        }
    }

    fn start_health_checks(&self) {
        let backends = self.backends.clone();
        let health = Arc::clone(&self.health);
        
        thread::spawn(move || {
            loop {
                for (i, backend) in backends.iter().enumerate() {
                    let is_healthy = Self::check_health(backend);
                    let mut health = health.lock().unwrap();
                    health[i] = is_healthy;
                }
                thread::sleep(Duration::from_secs(5));
            }
        });
    }

    fn check_health(backend: &str) -> bool {
        match TcpStream::connect(backend) {
            Ok(mut stream) => {
                // Send a GET request to /health endpoint
                let request = "GET /health HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
                if stream.write(request.as_bytes()).is_err() {
                    return false;
                }
                
                // Check the response
                let mut buffer = [0; 1024];
                match stream.read(&mut buffer) {
                    Ok(size) => {
                        let response = String::from_utf8_lossy(&buffer[0..size]);
                        response.contains("OK") || response.contains("200 OK")
                    }
                    Err(_) => false,
                }
            }
            Err(_) => false,
        }
    }

    fn get_next_backend(&self) -> Option<String> {
        let health = self.health.lock().unwrap();
        let backends_count = self.backends.len();
        
        if backends_count == 0 {
            return None;
        }
        
        // Find the next healthy backend
        let mut current = self.current.lock().unwrap();
        let start_index = *current;
        
        for i in 0..backends_count {
            let index = (start_index + i) % backends_count;
            if health[index] {
                *current = (index + 1) % backends_count;
                return Some(self.backends[index].clone());
            }
        }
        
        None // No healthy backends available
    }

    fn handle_client(&self, mut client: TcpStream) {
        // Get the next backend server
        let backend_addr = match self.get_next_backend() {
            Some(addr) => addr,
            None => {
                let _ = client.write(b"HTTP/1.1 503 Service Unavailable\r\n\r\nNo backend servers available");
                return;
            }
        };
        
        // Connect to the backend server
        let mut backend = match TcpStream::connect(&backend_addr) {
            Ok(stream) => stream,
            Err(_) => {
                let _ = client.write(b"HTTP/1.1 502 Bad Gateway\r\n\r\nFailed to connect to backend server");
                return;
            }
        };
        
        // Forward client request to backend
        let mut buffer = [0; 4096];
        match client.read(&mut buffer) {
            Ok(n) => {
                if let Err(_) = backend.write(&buffer[0..n]) {
                    let _ = client.write(b"HTTP/1.1 502 Bad Gateway\r\n\r\nFailed to forward request");
                    return;
                }
            }
            Err(_) => return,
        }
        
        // Forward backend response to client
        loop {
            match backend.read(&mut buffer) {
                Ok(n) if n > 0 => {
                    if let Err(_) = client.write(&buffer[0..n]) {
                        break;
                    }
                }
                _ => break,
            }
        }
    }
}

fn main() -> std::io::Result<()> {
    // Configuration
    let listen_addr = "127.0.0.1:8080";
    let backends = vec![
        "127.0.0.1:3000".to_string(),
        "127.0.0.1:3001".to_string(),
        "127.0.0.1:3002".to_string(),
    ];
    
    println!("Load balancer starting on {}", listen_addr);
    println!("Backend servers: {:?}", backends);
    
    // Create load balancer
    let load_balancer = LoadBalancer::new(backends);
    
    // Start health checks
    load_balancer.start_health_checks();
    
    // Start TCP listener
    let listener = TcpListener::bind(listen_addr)?;
    
    // Accept connections
    for stream in listener.incoming() {
        match stream {
            Ok(stream) => {
                let lb = load_balancer.clone();
                thread::spawn(move || {
                    lb.handle_client(stream);
                });
            }
            Err(e) => {
                eprintln!("Error accepting connection: {}", e);
            }
        }
    }
    
    Ok(())
}