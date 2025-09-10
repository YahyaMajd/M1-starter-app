"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.updateProfileSchema = exports.createUserSchema = void 0;
const zod_1 = __importDefault(require("zod"));
const hobbies_1 = require("./hobbies");
// Zod schemas
// ------------------------------------------------------------
exports.createUserSchema = zod_1.default.object({
    email: zod_1.default.string().email(),
    name: zod_1.default.string().min(1),
    googleId: zod_1.default.string().min(1),
    profilePicture: zod_1.default.string().optional(),
    bio: zod_1.default.string().max(500).optional(),
    hobbies: zod_1.default.array(zod_1.default.string()).default([]),
});
exports.updateProfileSchema = zod_1.default.object({
    name: zod_1.default.string().min(1).optional(),
    bio: zod_1.default.string().max(500).optional(),
    hobbies: zod_1.default
        .array(zod_1.default.string())
        .refine(val => val.length === 0 || val.every(v => hobbies_1.HOBBIES.includes(v)), {
        message: 'Hobby must be in the available hobbies list',
    })
        .optional(),
    profilePicture: zod_1.default.string().min(1).optional(),
});
