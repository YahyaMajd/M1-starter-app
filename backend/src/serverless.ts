import type { VercelRequest, VercelResponse } from '@vercel/node';
import express from 'express';
import dotenv from 'dotenv';
import { connectDB } from './database';
import { errorHandler, notFoundHandler } from './errorHandler.middleware';
import router from './routes';
import path from 'path';

dotenv.config();

const app = express();

// Middleware
app.use(express.json());

// Routes
app.use('/api', router);
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

// Health check at root
app.get('/', (req, res) => {
  res.json({ 
    message: 'CPEN321 Backend API is running',
    status: 'OK',
    timestamp: new Date().toISOString()
  });
});

// Error handlers
app.use('*', notFoundHandler);
app.use(errorHandler);

// Connect to database once
let isConnected = false;

const connectToDatabase = async () => {
  if (!isConnected) {
    try {
      await connectDB();
      isConnected = true;
      console.log('Database connected successfully');
    } catch (error) {
      console.error('Database connection failed:', error);
    }
  }
};

// Export the Express app as a serverless function
export default async (req: VercelRequest, res: VercelResponse) => {
  await connectToDatabase();
  return app(req, res);
};
