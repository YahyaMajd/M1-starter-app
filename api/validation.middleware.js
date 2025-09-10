"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.validateBody = void 0;
const zod_1 = require("zod");
const validateBody = (schema) => {
    return (req, res, next) => {
        try {
            const validatedData = schema.parse(req.body);
            req.body = validatedData;
            next();
        }
        catch (error) {
            if (error instanceof zod_1.ZodError) {
                return res.status(400).json({
                    error: 'Validation error',
                    message: 'Invalid input data',
                    details: error.issues.map(issue => ({
                        field: issue.path.join('.'),
                        message: issue.message,
                    })),
                });
            }
            return res.status(500).json({
                error: 'Internal server error',
                message: 'Validation processing failed',
            });
        }
    };
};
exports.validateBody = validateBody;
