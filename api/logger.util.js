"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const sanitizeInput_util_1 = require("./sanitizeInput.util");
const logger = {
    info: (message, ...args) => {
        console.log(`[INFO] ${(0, sanitizeInput_util_1.sanitizeInput)(message)}`, ...(0, sanitizeInput_util_1.sanitizeArgs)(args));
    },
    error: (message, ...args) => {
        console.error(`[ERROR] ${(0, sanitizeInput_util_1.sanitizeInput)(message)}`, ...(0, sanitizeInput_util_1.sanitizeArgs)(args));
    },
    warn: (message, ...args) => {
        console.warn(`[WARN] ${(0, sanitizeInput_util_1.sanitizeInput)(message)}`, ...(0, sanitizeInput_util_1.sanitizeArgs)(args));
    },
    debug: (message, ...args) => {
        console.debug(`[DEBUG] ${(0, sanitizeInput_util_1.sanitizeInput)(message)}`, ...(0, sanitizeInput_util_1.sanitizeArgs)(args));
    },
};
exports.default = logger;
