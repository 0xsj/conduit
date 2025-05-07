import Fastify from 'fastify';
import { createLogger } from '@traces-platform/logger';

// Initialize the logger
const logger = createLogger({
  serviceName: 'auth-service',
  level: 'debug',
  pretty: true
});

// Create Fastify instance
const server = Fastify({
  logger: false // We'll use our custom logger instead
});

// Define a route
server.get('/', async (request, reply) => {
  logger.info('Received request to root endpoint');
  return { service: 'auth-service', status: 'running' };
});

// Health check endpoint
server.get('/health', async (request, reply) => {
  logger.debug('Health check requested');
  return { status: 'ok' };
});

// Mock login route
server.post('/login', async (request, reply) => {
  logger.info('Login attempt received');
  return { token: 'mock-jwt-token' };
});

// Start the server
const start = async () => {
  try {
    await server.listen({ port: 3002, host: '0.0.0.0' });
    logger.info(`Server is running on http://localhost:3002`);
  } catch (err) {
    logger.error(err, 'Error starting server');
    process.exit(1);
  }
};

start();