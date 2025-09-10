"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.userModel = exports.UserModel = void 0;
const mongoose_1 = __importStar(require("mongoose"));
const zod_1 = require("zod");
const hobbies_1 = require("./hobbies");
const user_types_1 = require("./user.types");
const logger_util_1 = __importDefault(require("./logger.util"));
const userSchema = new mongoose_1.Schema({
    googleId: {
        type: String,
        required: true,
        unique: true,
        index: true,
    },
    email: {
        type: String,
        required: true,
        unique: true,
        lowercase: true,
        trim: true,
    },
    name: {
        type: String,
        required: true,
        trim: true,
    },
    profilePicture: {
        type: String,
        required: false,
        trim: true,
    },
    bio: {
        type: String,
        required: false,
        trim: true,
        maxlength: 500,
    },
    hobbies: {
        type: [String],
        default: [],
        validate: {
            validator: function (hobbies) {
                return (hobbies.length === 0 ||
                    hobbies.every(hobby => hobbies_1.HOBBIES.includes(hobby)));
            },
            message: 'Hobbies must be non-empty strings and must be in the available hobbies list',
        },
    },
}, {
    timestamps: true,
});
class UserModel {
    user;
    constructor() {
        this.user = mongoose_1.default.model('User', userSchema);
    }
    async create(userInfo) {
        try {
            const validatedData = user_types_1.createUserSchema.parse(userInfo);
            return await this.user.create(validatedData);
        }
        catch (error) {
            if (error instanceof zod_1.z.ZodError) {
                console.error('Validation error:', error.issues);
                throw new Error('Invalid update data');
            }
            console.error('Error updating user:', error);
            throw new Error('Failed to update user');
        }
    }
    async update(userId, user) {
        try {
            const validatedData = user_types_1.updateProfileSchema.parse(user);
            const updatedUser = await this.user.findByIdAndUpdate(userId, validatedData, {
                new: true,
            });
            return updatedUser;
        }
        catch (error) {
            logger_util_1.default.error('Error updating user:', error);
            throw new Error('Failed to update user');
        }
    }
    async delete(userId) {
        try {
            await this.user.findByIdAndDelete(userId);
        }
        catch (error) {
            logger_util_1.default.error('Error deleting user:', error);
            throw new Error('Failed to delete user');
        }
    }
    async findById(_id) {
        try {
            const user = await this.user.findOne({ _id });
            if (!user) {
                return null;
            }
            return user;
        }
        catch (error) {
            console.error('Error finding user by Google ID:', error);
            throw new Error('Failed to find user');
        }
    }
    async findByGoogleId(googleId) {
        try {
            const user = await this.user.findOne({ googleId });
            if (!user) {
                return null;
            }
            return user;
        }
        catch (error) {
            console.error('Error finding user by Google ID:', error);
            throw new Error('Failed to find user');
        }
    }
}
exports.UserModel = UserModel;
exports.userModel = new UserModel();
