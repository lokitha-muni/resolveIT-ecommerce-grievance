// Configuration for frontend application
const CONFIG = {
    // API Base URL - reads from environment or defaults to localhost
    API_BASE_URL: 'http://localhost:8080', // This should be updated for production
    
    // Other configuration options
    APP_NAME: 'ResolveIT',
    VERSION: '1.0.0',
    
    // API Endpoints
    ENDPOINTS: {
        AUTH: '/api/auth',
        COMPLAINTS: '/api/complaints',
        DASHBOARD: '/api/dashboard',
        STAFF: '/api/staff',
        ADMIN: '/api/admin',
        COMMENTS: '/api/comments',
        RATINGS: '/api/ratings'
    }
};

// Helper function to get full API URL
function getApiUrl(endpoint = '') {
    return CONFIG.API_BASE_URL + endpoint;
}

// Export for use in other files
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CONFIG;
}