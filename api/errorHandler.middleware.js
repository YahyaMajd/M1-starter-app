"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.errorHandler = exports.notFoundHandler = void 0;
const logger_util_1 = __importDefault(require("./logger.util"));
const notFoundHandler = (req, res) => {
    res.status(404).json({
        error: 'Route not found',
        message: `Cannot ${req.method} ${req.originalUrl}`,
        timestamp: new Date().toISOString(),
        path: req.originalUrl,
        method: req.method,
    });
};
exports.notFoundHandler = notFoundHandler;
const errorHandler = (error, req, res) => {
    logger_util_1.default.error('Error:', error);
    return res.status(500).json({
        message: 'Internal server error',
    });
};
exports.errorHandler = errorHandler;
