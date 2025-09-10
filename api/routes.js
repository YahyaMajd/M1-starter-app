"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = require("express");
const auth_middleware_1 = require("./auth.middleware");
const auth_routes_1 = __importDefault(require("./auth.routes"));
const hobbies_routes_1 = __importDefault(require("./hobbies.routes"));
const media_routes_1 = __importDefault(require("./media.routes"));
const user_routes_1 = __importDefault(require("./user.routes"));
const router = (0, express_1.Router)();
router.use('/auth', auth_routes_1.default);
router.use('/hobbies', auth_middleware_1.authenticateToken, hobbies_routes_1.default);
router.use('/user', auth_middleware_1.authenticateToken, user_routes_1.default);
router.use('/media', auth_middleware_1.authenticateToken, media_routes_1.default);
exports.default = router;
