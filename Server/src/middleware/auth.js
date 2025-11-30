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
    
    if (providedPassword !== serverPassword) {
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
    
    if (clientToken && providedPassword && providedPassword === serverPassword) {
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
