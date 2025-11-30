const crypto = require('crypto');

/**
 * Constant-time comparison to prevent timing attacks
 */
function safeCompare(a, b) {
    if (typeof a !== 'string' || typeof b !== 'string') {
        return false;
    }
    const bufferA = Buffer.from(a);
    const bufferB = Buffer.from(b);
    if (bufferA.length !== bufferB.length) {
        // Use timingSafeEqual with same-length buffers to avoid length timing attacks
        const paddedA = Buffer.alloc(Math.max(bufferA.length, bufferB.length));
        const paddedB = Buffer.alloc(Math.max(bufferA.length, bufferB.length));
        bufferA.copy(paddedA);
        bufferB.copy(paddedB);
        crypto.timingSafeEqual(paddedA, paddedB);
        return false;
    }
    return crypto.timingSafeEqual(bufferA, bufferB);
}

/**
 * Authentication middleware
 * Verifies client token and server password
 */
function authenticate(req, res, next) {
    const serverPassword = process.env.SERVER_PASSWORD;
    
    if (!serverPassword) {
        return res.status(500).json({
            success: false,
            message: 'Server password not configured'
        });
    }
    
    const clientToken = req.headers['x-client-token'];
    const providedPassword = req.headers['x-server-password'];
    
    if (!clientToken) {
        return res.status(401).json({
            success: false,
            message: 'Missing client token'
        });
    }
    
    if (!providedPassword) {
        return res.status(401).json({
            success: false,
            message: 'Missing server password'
        });
    }
    
    if (!safeCompare(providedPassword, serverPassword)) {
        return res.status(403).json({
            success: false,
            message: 'Invalid server password'
        });
    }
    
    // Attach client token to request for use in routes
    req.clientToken = clientToken;
    next();
}

/**
 * Optional authentication - allows unauthenticated access to public endpoints
 */
function optionalAuth(req, res, next) {
    const clientToken = req.headers['x-client-token'];
    const providedPassword = req.headers['x-server-password'];
    const serverPassword = process.env.SERVER_PASSWORD;
    
    if (clientToken && providedPassword && safeCompare(providedPassword, serverPassword)) {
        req.clientToken = clientToken;
        req.isAuthenticated = true;
    } else {
        req.isAuthenticated = false;
    }
    
    next();
}

module.exports = {
    authenticate,
    optionalAuth
};
