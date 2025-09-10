// Vercel entry point - exports the Express app for serverless deployment
const app = require('./backend/dist/index.js').default;

module.exports = app;
