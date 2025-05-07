import pino from 'pino';

export interface LoggerOptions {
  serviceName: string;
  level?: string;
  pretty?: boolean;
}

export type Logger = pino.Logger;

export function createLogger(options: LoggerOptions): Logger {
  const { serviceName, level = 'info', pretty = false } = options;

  const pinoOptions: pino.LoggerOptions = {
    name: serviceName,
    level,
    timestamp: pino.stdTimeFunctions.isoTime
  };

  if (pretty) {
    return pino(pinoOptions, pino.transport({
      target: 'pino-pretty',
      options: {
        colorize: true
      }
    }));
  }

  return pino(pinoOptions);
}

export default createLogger;