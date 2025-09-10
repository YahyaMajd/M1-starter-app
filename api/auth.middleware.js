"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.authenticateToken = void 0;
const jsonwebtoken_1 = __importDefault(require("jsonwebtoken"));
const user_model_1 = require("./user.model");
const authenticateToken = async (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;
        const token = authHeader?.split(' ')[1];
        if (!token) {
            res.status(401).json({
                error: 'Access denied',
                message: 'No token provided',
            });
            return;
        }
        const decoded = jsonwebtoken_1.default.verify(token, process.env.JWT_SECRET);
        if (!decoded || !decoded.id) {
            res.status(401).json({
                error: 'Invalid token',
                message: 'Token verification failed',
            });
            return;
        }
        const user = await user_model_1.userModel.findById(decoded.id);
        if (!user) {
            res.status(401).json({
                error: 'User not found',
                message: 'Token is valid but user no longer exists',
            });
            return;
        }
        req.user = user;
        next();
    }
    catch (error) {
        if (error instanceof jsonwebtoken_1.default.JsonWebTokenError) {
            res.status(401).json({
                error: 'Invalid token',
                message: 'Token is malformed or expired',
            });
            return;
        }
        if (error instanceof jsonwebtoken_1.default.TokenExpiredError) {
            res.status(401).json({
                error: 'Token expired',
                message: 'Please login again',
            });
            return;
        }
        next(error);
    }
};
exports.authenticateToken = authenticateToken;
