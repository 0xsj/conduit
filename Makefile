.PHONY: dev dev-backend dev-load-balancer run-all

# Start backend with file watching for changes
dev-backend:
	cargo watch -x 'run -p backend'

# Start load balancer with file watching for changes
dev-load-balancer:
	cargo watch -x 'run -p load-balancer'

# Start both backend and load balancer in parallel using multiple terminals
dev:
	@echo "Starting both backend and load balancer..."
	@echo "Backend will run on port 3000"
	@echo "Load balancer will run on port 8080"
	@echo "Use Ctrl+C to stop"
	@echo ""
	@tmux new-session -d -s dev-session 'make dev-backend' \; \
		split-window -h 'make dev-load-balancer' \; \
		attach \; \
		set-option -g mouse on \;

# Run both services without tmux (requires multiple terminals)
run-all:
	@echo "Starting backend and load balancer (multiple terminals required)"
	@echo "In terminal 1: cargo run -p backend"
	@echo "In terminal 2: cargo run -p load-balancer"
	@echo ""
	@echo "Or use: make dev (requires tmux)"

# Start multiple backend instances for load balancing
run-cluster:
	@echo "Starting 3 backend instances on different ports"
	@echo "Load balancer will distribute traffic between them"
	@tmux new-session -d -s cluster-session 'PORT=3000 cargo run -p backend' \; \
		split-window -v 'PORT=3001 cargo run -p backend' \; \
		split-window -v 'PORT=3002 cargo run -p backend' \; \
		select-layout even-vertical \; \
		new-window 'cargo run -p load-balancer' \; \
		select-window -t 0 \; \
		attach \; \
		set-option -g mouse on \;

# Run using docker-compose for full environment
docker-dev:
	docker-compose up --build

# Clean up all build artifacts and docker resources
clean:
	cargo clean
	docker-compose down -v

docker-up:
	docker-compose up -d

# Stop all Docker services
docker-down:
	docker-compose down

# Start a specific service
docker-up-%:
	docker-compose up -d $*

# Restart a specific service
docker-restart-%:
	docker-compose restart $*

# View logs for a specific service
docker-logs-%:
	docker-compose logs -f $*

# Reset database (removes all data)
docker-reset-db:
	docker-compose down -v mysql
	docker-compose up -d mysql

# Run migrations using SQLx CLI
db-migrate:
	sqlx migrate run

# Create a new migration
db-new-migration:
	@read -p "Migration name: " name; \
	sqlx migrate add -r $$name