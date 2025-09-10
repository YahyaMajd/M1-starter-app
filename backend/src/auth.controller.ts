import { NextFunction, Request, Response } from 'express';

import { authService } from './auth.service';
import {
  AuthenticateUserRequest,
  AuthenticateUserResponse,
  LogoutResponse,
} from './auth.types';
import logger from './logger.util';

export class AuthController {
  async signUp(
    req: Request<unknown, unknown, AuthenticateUserRequest>,
    res: Response<AuthenticateUserResponse>,
    next: NextFunction
  ) {
    try {
      const { idToken } = req.body;

      const data = await authService.signUpWithGoogle(idToken);

      return res.status(201).json({
        message: 'User signed up successfully',
        data,
      });
    } catch (error) {
      logger.error('Google sign up error:', error);

      if (error instanceof Error) {
        if (error.message === 'Invalid Google token') {
          return res.status(401).json({
            message: 'Invalid Google token',
          });
        }

        if (error.message === 'User already exists') {
          return res.status(409).json({
            message: 'User already exists, please sign in instead.',
          });
        }

        if (error.message === 'Failed to process user') {
          return res.status(500).json({
            message: 'Failed to process user information',
          });
        }
      }

      next(error);
    }
  }

  async signIn(
    req: Request<unknown, unknown, AuthenticateUserRequest>,
    res: Response<AuthenticateUserResponse>,
    next: NextFunction
  ) {
    try {
      const { idToken } = req.body;

      const data = await authService.signInWithGoogle(idToken);

      return res.status(200).json({
        message: 'User signed in successfully',
        data,
      });
    } catch (error) {
      logger.error('Google sign in error:', error);

      if (error instanceof Error) {
        if (error.message === 'Invalid Google token') {
          return res.status(401).json({
            message: 'Invalid Google token',
          });
        }

        if (error.message === 'User not found') {
          return res.status(404).json({
            message: 'User not found, please sign up first.',
          });
        }

        if (error.message === 'Failed to process user') {
          return res.status(500).json({
            message: 'Failed to process user information',
          });
        }
      }

      next(error);
    }
  }

  async logout(
    req: Request,
    res: Response<LogoutResponse>,
    next: NextFunction
  ) {
    try {
      // Since we're using stateless JWT tokens, logout is primarily client-side
      // The client should discard the token
      // Here we can log the logout event and perform any cleanup if needed
      
      logger.info('User logout requested', { 
        userId: req.user?.id,
        timestamp: new Date().toISOString()
      });

      return res.status(200).json({
        message: 'User signed out successfully',
      });
    } catch (error) {
      logger.error('Logout error:', error);
      next(error);
    }
  }
}
