"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.authService = exports.AuthService = void 0;
const google_auth_library_1 = require("google-auth-library");
const jsonwebtoken_1 = __importDefault(require("jsonwebtoken"));
const logger_util_1 = __importDefault(require("./logger.util"));
const user_model_1 = require("./user.model");
class AuthService {
    googleClient;
    constructor() {
        this.googleClient = new google_auth_library_1.OAuth2Client(process.env.GOOGLE_CLIENT_ID, process.env.GOOGLE_CLIENT_SECRET);
    }
    async verifyGoogleToken(idToken) {
        try {
            const ticket = await this.googleClient.verifyIdToken({
                idToken,
                audience: process.env.GOOGLE_CLIENT_ID,
            });
            const payload = ticket.getPayload();
            if (!payload) {
                throw new Error('Invalid token payload');
            }
            if (!payload.email || !payload.name) {
                throw new Error('Missing required user information from Google');
            }
            return {
                googleId: payload.sub,
                email: payload.email,
                name: payload.name,
                profilePicture: payload.picture,
            };
        }
        catch (error) {
            logger_util_1.default.error('Google token verification failed:', error);
            throw new Error('Invalid Google token');
        }
    }
    generateAccessToken(user) {
        return jsonwebtoken_1.default.sign({ id: user._id }, process.env.JWT_SECRET, {
            expiresIn: '19h',
        });
    }
    async signUpWithGoogle(idToken) {
        try {
            const googleUserInfo = await this.verifyGoogleToken(idToken);
            // Check if user already exists
            const existingUser = await user_model_1.userModel.findByGoogleId(googleUserInfo.googleId);
            if (existingUser) {
                throw new Error('User already exists');
            }
            // Create new user
            const user = await user_model_1.userModel.create(googleUserInfo);
            const token = this.generateAccessToken(user);
            return { token, user };
        }
        catch (error) {
            logger_util_1.default.error('Sign up failed:', error);
            throw error;
        }
    }
    async signInWithGoogle(idToken) {
        try {
            const googleUserInfo = await this.verifyGoogleToken(idToken);
            // Find existing user
            const user = await user_model_1.userModel.findByGoogleId(googleUserInfo.googleId);
            if (!user) {
                throw new Error('User not found');
            }
            const token = this.generateAccessToken(user);
            return { token, user };
        }
        catch (error) {
            logger_util_1.default.error('Sign in failed:', error);
            throw error;
        }
    }
}
exports.AuthService = AuthService;
exports.authService = new AuthService();
