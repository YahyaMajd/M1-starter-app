import { Router } from 'express';

import { authenticateToken } from './auth.middleware';
import authRoutes from './auth.routes';
import hobbiesRoutes from './hobbies.routes';
import mediaRoutes from './media.routes';
import usersRoutes from './user.routes';

const router = Router();

// Health check endpoint
router.get('/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    message: 'Server is running',
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV || 'development'
  });
});

// API info endpoint
router.get('/', (req, res) => {
  res.json({
    message: 'CPEN321 Backend API',
    version: '1.0.0',
    endpoints: {
      auth: '/api/auth (POST /signup, /signin, /logout)',
      hobbies: '/api/hobbies (requires auth)',
      user: '/api/user (requires auth)',
      media: '/api/media (requires auth)',
      health: '/api/health'
    }
  });
});

router.use('/auth', authRoutes);

router.use('/hobbies', authenticateToken, hobbiesRoutes);

router.use('/user', authenticateToken, usersRoutes);

router.use('/media', authenticateToken, mediaRoutes);

export default router;
