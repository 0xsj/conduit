// load-balancer/src/main.rs
use std::io::{Read, Write};
use std::net::{TcpListener, TcpStream};
use std::sync::{Arc, Mutex};
use std::thread;
use std::time::{Duration, Instant};

/// LoadBalancer distributes incoming connections across multiple backend servers
/// using a round-robin algorithm with health checks.
#[derive(Clone)]
struct LoadBalancer {
    // List of backend server addresses
    backends: Vec<String>,
    // Current backend index for round-robin selection
    current: Arc<Mutex<usize>>,
    // Health status of backend servers
    health: Arc<Mutex<Vec<bool>>>,
}

impl LoadBalancer {
    /// Create a new LoadBalancer with the given backend servers
    fn new(backends: Vec<String>) -> Self {
        let health = vec![true; backends.len()];
        
        LoadBalancer {
            backends,
            current: Arc::new(Mutex::new(0)),
            health: Arc::new(Mutex::new(health)),
        }
    }

    /// Start health check thread that periodically checks backend servers
    fn start_health_checks(&self) {
        let backends = self.backends.clone();
        let health = Arc::clone(&self.health);
        
        thread::spawn(move || {
            loop {
                for (i, backend) in backends.iter().enumerate() {
                    let is_healthy = Self::check_health(backend);
                    
                    // Update health status with minimal lock time
                    let mut health = health.lock().unwrap();
                    health[i] = is_healthy;
                    
                    // Drop the lock immediately
                    drop(health);
                    
                    // Small delay between checking each backend
                    thread::sleep(Duration::from_millis(100));
                }
                
                // Wait before next health check cycle
                thread::sleep(Duration::from_secs(5));
            }
        });
    }

    /// Check if a backend server is healthy by connecting to its health endpoint
    fn check_health(backend: &str) -> bool {
        // Set a timeout for the connection attempt
        match TcpStream::connect(backend) {
            Ok(mut stream) => {
                // Set read/write timeouts
                let _ = stream.set_read_timeout(Some(Duration::from_secs(2)));
                let _ = stream.set_write_timeout(Some(Duration::from_secs(2)));
                
                // Send a GET request to /health endpoint
                let request = "GET /health HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
                if stream.write_all(request.as_bytes()).is_err() {
                    return false;
                }
                
                // Check the response with timeout
                let start = Instant::now();
                let mut buffer = [0; 1024];
                let mut response = String::new();
                
                while start.elapsed() < Duration::from_secs(5) {
                    match stream.read(&mut buffer) {
                        Ok(0) => break, // Connection closed
                        Ok(size) => {
                            response.push_str(&String::from_utf8_lossy(&buffer[0..size]));
                            if response.contains("OK") || response.contains("200 OK") {
                                return true;
                            }
                        }
                        Err(_) => return false,
                    }
                }
                
                false // Timeout or invalid response
            }
            Err(_) => false, // Connection failed
        }
    }

    /// Get the next healthy backend server using round-robin selection
    fn get_next_backend(&self) -> Option<String> {
        // Get a snapshot of health status with minimal lock time
        let health = self.health.lock().unwrap();
        let health_snapshot = health.clone();
        let backends_count = self.backends.len();
        
        // Drop the lock immediately after cloning
        drop(health);
        
        if backends_count == 0 {
            return None;
        }
        
        // Get and update the current index with minimal lock time
        let mut current = self.current.lock().unwrap();
        let start_index = *current;
        
        // Find the next healthy backend using the snapshot
        for i in 0..backends_count {
            let index = (start_index + i) % backends_count;
            if health_snapshot[index] {
                // Update the index for next time
                *current = (index + 1) % backends_count;
                return Some(self.backends[index].clone());
            }
        }
        
        None // No healthy backends available
    }

    /// Handle a client connection by forwarding it to a backend server
    fn handle_client(&self, mut client: TcpStream) {
        // Set client timeouts
        let _ = client.set_read_timeout(Some(Duration::from_secs(30)));
        let _ = client.set_write_timeout(Some(Duration::from_secs(30)));
        
        // Get the next backend server
        let backend_addr = match self.get_next_backend() {
            Some(addr) => addr,
            None => {
                let response = "HTTP/1.1 503 Service Unavailable\r\n\r\nNo backend servers available";
                let _ = client.write_all(response.as_bytes());
                return;
            }
        };
        
        // Connect to the backend server with timeout
        let backend = match TcpStream::connect(&backend_addr) {
            Ok(stream) => stream,
            Err(e) => {
                let response = format!("HTTP/1.1 502 Bad Gateway\r\n\r\nFailed to connect to backend server: {}", e);
                let _ = client.write_all(response.as_bytes());
                return;
            }
        };
        
        // Set backend timeouts
        let _ = backend.set_read_timeout(Some(Duration::from_secs(30)));
        let _ = backend.set_write_timeout(Some(Duration::from_secs(30)));
        
        // Create two threads to handle bidirectional forwarding
        self.forward_bidirectional(client, backend);
    }
    
    /// Forward data bidirectionally between client and backend
    fn forward_bidirectional(&self, mut client: TcpStream, mut backend: TcpStream) {
        // Clone streams for the second thread
        let mut client_clone = match client.try_clone() {
            Ok(stream) => stream,
            Err(_) => return,
        };
        
        let mut backend_clone = match backend.try_clone() {
            Ok(stream) => stream,
            Err(_) => return,
        };
        
        // Thread 1: client -> backend
        let client_to_backend = thread::spawn(move || {
            let mut buffer = [0; 8192];
            
            loop {
                match client.read(&mut buffer) {
                    Ok(0) => break, // Connection closed
                    Ok(n) => {
                        if backend.write_all(&buffer[0..n]).is_err() {
                            break;
                        }
                    }
                    Err(_) => break,
                }
            }
        });
        
        // Thread 2: backend -> client
        let backend_to_client = thread::spawn(move || {
            let mut buffer = [0; 8192];
            
            loop {
                match backend_clone.read(&mut buffer) {
                    Ok(0) => break, // Connection closed
                    Ok(n) => {
                        if client_clone.write_all(&buffer[0..n]).is_err() {
                            break;
                        }
                    }
                    Err(_) => break,
                }
            }
        });
        
        // Wait for both forwarding directions to complete
        let _ = client_to_backend.join();
        let _ = backend_to_client.join();
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