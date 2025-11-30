require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const path = require('path');
const apiRoutes = require('./routes/api');

const app = express();
const PORT = process.env.PORT || 3000;

// Security middleware
app.use(helmet({
    contentSecurityPolicy: {
        directives: {
            defaultSrc: ["'self'"],
            styleSrc: ["'self'", "'unsafe-inline'", "https://fonts.googleapis.com"],
            fontSrc: ["'self'", "https://fonts.gstatic.com"],
            scriptSrc: ["'self'", "'unsafe-inline'"],
            imgSrc: ["'self'", "data:", "https:"],
        },
    },
}));

// CORS configuration
const corsOrigins = process.env.CORS_ORIGINS || '*';
app.use(cors({
    origin: corsOrigins === '*' ? '*' : corsOrigins.split(','),
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Client-Token', 'X-Server-Password']
}));

// Request logging
if (process.env.NODE_ENV !== 'production') {
    app.use(morgan('dev'));
}

// Body parsing
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Static files for web frontend
app.use(express.static(path.join(__dirname, '../public')));

// API routes
app.use('/api/v1', apiRoutes);

// Serve frontend for all other routes
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, '../public/index.html'));
});

// Error handling middleware
app.use((err, req, res, next) => {
    console.error('Error:', err.message);
    res.status(err.status || 500).json({
        success: false,
        message: err.message || 'Internal server error'
    });
});

// Start server
app.listen(PORT, () => {
    console.log(`Life App Server running on port ${PORT}`);
    console.log(`Web frontend: http://localhost:${PORT}`);
    console.log(`API endpoint: http://localhost:${PORT}/api/v1`);
});

module.exports = app;
