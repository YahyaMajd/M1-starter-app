"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.disconnectDB = exports.connectDB = void 0;
const mongoose_1 = __importDefault(require("mongoose"));
const connectDB = async () => {
    try {
        const uri = process.env.MONGODB_URI;
        await mongoose_1.default.connect(uri);
        console.log(`✅ MongoDB connected successfully`);
        mongoose_1.default.connection.on('error', error => {
            console.error('❌ MongoDB connection error:', error);
        });
        mongoose_1.default.connection.on('disconnected', () => {
            console.log('⚠️ MongoDB disconnected');
        });
        process.on('SIGINT', async () => {
            await mongoose_1.default.connection.close();
            console.log('MongoDB connection closed through app termination');
            process.exitCode = 0;
        });
    }
    catch (error) {
        console.error('❌ Failed to connect to MongoDB:', error);
        process.exitCode = 1;
    }
};
exports.connectDB = connectDB;
const disconnectDB = async () => {
    try {
        await mongoose_1.default.connection.close();
        console.log('✅ MongoDB disconnected successfully');
    }
    catch (error) {
        console.error('❌ Error disconnecting from MongoDB:', error);
    }
};
exports.disconnectDB = disconnectDB;
