"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.MediaService = void 0;
const fs_1 = __importDefault(require("fs"));
const path_1 = __importDefault(require("path"));
const hobbies_1 = require("./hobbies");
class MediaService {
    static async saveImage(filePath, userId) {
        try {
            const fileExtension = path_1.default.extname(filePath);
            const fileName = `${userId}-${Date.now()}${fileExtension}`;
            const newPath = path_1.default.join(hobbies_1.IMAGES_DIR, fileName);
            fs_1.default.renameSync(filePath, newPath);
            return newPath.split(path_1.default.sep).join('/');
        }
        catch (error) {
            if (fs_1.default.existsSync(filePath)) {
                fs_1.default.unlinkSync(filePath);
            }
            throw new Error(`Failed to save profile picture: ${error}`);
        }
    }
    static async deleteImage(url) {
        try {
            if (url.startsWith(hobbies_1.IMAGES_DIR)) {
                const filePath = path_1.default.join(process.cwd(), url.substring(1));
                if (fs_1.default.existsSync(filePath)) {
                    fs_1.default.unlinkSync(filePath);
                }
            }
        }
        catch (error) {
            console.error('Failed to delete old profile picture:', error);
        }
    }
    static async deleteAllUserImages(userId) {
        try {
            if (!fs_1.default.existsSync(hobbies_1.IMAGES_DIR)) {
                return;
            }
            const files = fs_1.default.readdirSync(hobbies_1.IMAGES_DIR);
            const userFiles = files.filter(file => file.startsWith(userId + '-'));
            await Promise.all(userFiles.map(file => this.deleteImage(file)));
        }
        catch (error) {
            console.error('Failed to delete user images:', error);
        }
    }
}
exports.MediaService = MediaService;
