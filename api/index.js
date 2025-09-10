"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const dotenv_1 = __importDefault(require("dotenv"));
const express_1 = __importDefault(require("express"));
const database_1 = require("./database");
const errorHandler_middleware_1 = require("./errorHandler.middleware");
const routes_1 = __importDefault(require("./routes"));
const path_1 = __importDefault(require("path"));
dotenv_1.default.config();
const app = (0, express_1.default)();
app.use(express_1.default.json());
app.use('/api', routes_1.default);
app.use('/uploads', express_1.default.static(path_1.default.join(__dirname, '../uploads')));
app.use('*', errorHandler_middleware_1.notFoundHandler);
app.use(errorHandler_middleware_1.errorHandler);
// Connect to database
(0, database_1.connectDB)();
// Export the Express app for Vercel serverless functions
exports.default = app;
