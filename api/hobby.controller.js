"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.HobbyController = void 0;
const hobbies_1 = require("./hobbies");
const logger_util_1 = __importDefault(require("./logger.util"));
class HobbyController {
    getAllHobbies(req, res, next) {
        try {
            res.status(200).json({
                message: 'All hobbies fetched successfully',
                data: { hobbies: hobbies_1.HOBBIES },
            });
        }
        catch (error) {
            logger_util_1.default.error('Failed to fetch available hobbies:', error);
            if (error instanceof Error) {
                return res.status(500).json({
                    message: error.message || 'Failed to fetch available hobbies',
                });
            }
            next(error);
        }
    }
}
exports.HobbyController = HobbyController;
