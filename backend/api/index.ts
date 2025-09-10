import type { VercelRequest, VercelResponse } from '@vercel/node';
import express from 'express';
import dotenv from 'dotenv';

// Import your existing modules
import { connectDB } from '../src/database';
import { errorHandler, notFoundHandler } from '../src/errorHandler.middleware';
import router from '../src/routes';

dotenv.config();

const app = express();

// Middleware
app.use(express.json());

// Health check at root
app.get('/', (req, res) => {
  res.json({ 
    message: 'CPEN321 Backend API is running on Vercel',
    status: 'OK',
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV || 'production'
  });
});

// API routes
app.use('/api', router);

// Error handlers
app.use('*', notFoundHandler);
app.use(errorHandler);

// Database connection state
let isConnected = false;

const connectToDatabase = async () => {
  if (!isConnected) {
    try {
      await connectDB();
      isConnected = true;
      console.log('Database connected successfully in serverless function');
    } catch (error) {
      console.error('Database connection failed:', error);
      // Don't throw - let the API work without DB for basic endpoints
    }
  }
};

// Export as serverless function
export default async function handler(req: VercelRequest, res: VercelResponse) {
  // Connect to database
  await connectToDatabase();
  
  // Use Express app to handle the request
  return app(req, res);
}
