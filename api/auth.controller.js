"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthController = void 0;
const auth_service_1 = require("./auth.service");
const logger_util_1 = __importDefault(require("./logger.util"));
class AuthController {
    async signUp(req, res, next) {
        try {
            const { idToken } = req.body;
            const data = await auth_service_1.authService.signUpWithGoogle(idToken);
            return res.status(201).json({
                message: 'User signed up successfully',
                data,
            });
        }
        catch (error) {
            logger_util_1.default.error('Google sign up error:', error);
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
    async signIn(req, res, next) {
        try {
            const { idToken } = req.body;
            const data = await auth_service_1.authService.signInWithGoogle(idToken);
            return res.status(200).json({
                message: 'User signed in successfully',
                data,
            });
        }
        catch (error) {
            logger_util_1.default.error('Google sign in error:', error);
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
    async logout(req, res, next) {
        try {
            // Since we're using stateless JWT tokens, logout is primarily client-side
            // The client should discard the token
            // Here we can log the logout event and perform any cleanup if needed
            logger_util_1.default.info('User logout requested', {
                userId: req.user?.id,
                timestamp: new Date().toISOString()
            });
            return res.status(200).json({
                message: 'User signed out successfully',
            });
        }
        catch (error) {
            logger_util_1.default.error('Logout error:', error);
            next(error);
        }
    }
}
exports.AuthController = AuthController;
