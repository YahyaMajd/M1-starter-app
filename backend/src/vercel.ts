import dotenv from 'dotenv';
import express from 'express';

import { connectDB } from './database';
import { errorHandler, notFoundHandler } from './errorHandler.middleware';
import router from './routes';
import path from 'path';

dotenv.config();

const app = express();

app.use(express.json());

app.use('/api', router);
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));
app.use('*', notFoundHandler);
app.use(errorHandler);

// Connect to database
connectDB();

// Export the Express app for Vercel serverless functions
export default app;
