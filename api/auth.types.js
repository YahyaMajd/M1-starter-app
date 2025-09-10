"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.authenticateUserSchema = void 0;
const zod_1 = require("zod");
// Zod schemas
// ------------------------------------------------------------
exports.authenticateUserSchema = zod_1.z.object({
    idToken: zod_1.z.string().min(1, 'Google token is required'),
});
