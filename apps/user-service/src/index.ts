import Fastify from 'fastify'
import {createLogger} from '@traces-platform/logger'

const logger = createLogger({
    serviceName: "user-service",
    level: 'debug',
    pretty: true
})

const server = Fastify({
    logger: false
})

server.get('/', async (request, reply) => {
    logger.info('Received request to root endpoint');
    return { service: 'user-service', status: 'running' };
})

server.get('/health', async (request, reply) => {
    logger.debug('Health check requested');
    return { status: 'ok' };
  });

  const start = async () => {
    try {
      await server.listen({ port: 3001, host: '0.0.0.0' });
      logger.info(`Server is running on http://localhost:3001`);
    } catch (err) {
      logger.error(err, 'Error starting server');
      process.exit(1);
    }
  };
  
  start();