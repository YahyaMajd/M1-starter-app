const express = require('express');
const app = express();

app.use(express.json());

// Simple test endpoint
app.get('/api/test', (req, res) => {
  res.json({ message: 'API is working!' });
});

app.post('/api/auth/signin', (req, res) => {
  res.json({ message: 'Signin endpoint reached', body: req.body });
});

// Export the Express API
module.exports = app;
