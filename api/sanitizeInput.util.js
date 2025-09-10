"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.sanitizeInput = exports.sanitizeArgs = void 0;
const sanitizeArgs = (args) => {
    return args.map(arg => (0, exports.sanitizeInput)(String(arg)));
};
exports.sanitizeArgs = sanitizeArgs;
const sanitizeInput = (input) => {
    if (/[\r\n]/.test(input)) {
        throw new Error('CRLF injection attempt detected');
    }
    return input;
};
exports.sanitizeInput = sanitizeInput;
