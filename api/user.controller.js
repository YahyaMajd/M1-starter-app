"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.UserController = void 0;
const logger_util_1 = __importDefault(require("./logger.util"));
const media_service_1 = require("./media.service");
const user_model_1 = require("./user.model");
class UserController {
    getProfile(req, res) {
        const user = req.user;
        res.status(200).json({
            message: 'Profile fetched successfully',
            data: { user },
        });
    }
    async updateProfile(req, res, next) {
        try {
            const user = req.user;
            const updatedUser = await user_model_1.userModel.update(user._id, req.body);
            if (!updatedUser) {
                return res.status(404).json({
                    message: 'User not found',
                });
            }
            res.status(200).json({
                message: 'User info updated successfully',
                data: { user: updatedUser },
            });
        }
        catch (error) {
            logger_util_1.default.error('Failed to update user info:', error);
            if (error instanceof Error) {
                return res.status(500).json({
                    message: error.message || 'Failed to update user info',
                });
            }
            next(error);
        }
    }
    async deleteProfile(req, res, next) {
        try {
            const user = req.user;
            await media_service_1.MediaService.deleteAllUserImages(user._id.toString());
            await user_model_1.userModel.delete(user._id);
            res.status(200).json({
                message: 'User deleted successfully',
            });
        }
        catch (error) {
            logger_util_1.default.error('Failed to delete user:', error);
            if (error instanceof Error) {
                return res.status(500).json({
                    message: error.message || 'Failed to delete user',
                });
            }
            next(error);
        }
    }
}
exports.UserController = UserController;
