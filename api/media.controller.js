"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.MediaController = void 0;
const logger_util_1 = __importDefault(require("./logger.util"));
const media_service_1 = require("./media.service");
const sanitizeInput_util_1 = require("./sanitizeInput.util");
class MediaController {
    async uploadImage(req, res, next) {
        try {
            if (!req.file) {
                return res.status(400).json({
                    message: 'No file uploaded',
                });
            }
            const user = req.user;
            const sanitizedFilePath = (0, sanitizeInput_util_1.sanitizeInput)(req.file.path);
            const image = await media_service_1.MediaService.saveImage(sanitizedFilePath, user._id.toString());
            res.status(200).json({
                message: 'Image uploaded successfully',
                data: {
                    image,
                },
            });
        }
        catch (error) {
            logger_util_1.default.error('Error uploading profile picture:', error);
            if (error instanceof Error) {
                return res.status(500).json({
                    message: error.message || 'Failed to upload profile picture',
                });
            }
            next(error);
        }
    }
}
exports.MediaController = MediaController;
