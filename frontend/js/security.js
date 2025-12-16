// Security utilities for frontend
class SecurityManager {
    constructor() {
        this.sessionTimeout = 30 * 60 * 1000; // 30 minutes
        this.lastActivity = Date.now();
        this.setupSessionMonitoring();
    }

    // JWT Token Management
    setToken(token) {
        sessionStorage.setItem('authToken', token);
        this.lastActivity = Date.now();
    }

    getToken() {
        return sessionStorage.getItem('authToken');
    }

    removeToken() {
        sessionStorage.removeItem('authToken');
        localStorage.removeItem('user');
        localStorage.removeItem('staff');
    }

    // Session Timeout Management
    setupSessionMonitoring() {
        // Update activity on user interactions
        ['click', 'keypress', 'scroll', 'mousemove'].forEach(event => {
            document.addEventListener(event, () => {
                this.updateActivity();
            });
        });

        // Check session every minute
        setInterval(() => {
            this.checkSession();
        }, 60000);
    }

    updateActivity() {
        this.lastActivity = Date.now();
    }

    checkSession() {
        const now = Date.now();
        if (now - this.lastActivity > this.sessionTimeout) {
            this.logout('Session expired due to inactivity');
        }
    }

    // Input Sanitization
    sanitizeInput(input) {
        if (typeof input !== 'string') return input;
        
        return input
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#x27;')
            .replace(/\//g, '&#x2F;');
    }

    validateEmail(email) {
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        return emailRegex.test(email);
    }

    validatePhone(phone) {
        const phoneRegex = /^[+]?[0-9]{10,15}$/;
        return phoneRegex.test(phone.replace(/\s/g, ''));
    }

    // Secure API calls with JWT
    async secureApiCall(url, options = {}) {
        const token = this.getToken();
        
        if (!token) {
            this.logout('No authentication token');
            return null;
        }

        const headers = {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            ...options.headers
        };

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            if (response.status === 401) {
                this.logout('Authentication failed');
                return null;
            }

            if (response.status === 429) {
                throw new Error('Too many requests. Please try again later.');
            }

            this.updateActivity();
            return response;
        } catch (error) {
            console.error('API call failed:', error);
            throw error;
        }
    }

    // Logout with cleanup
    logout(reason = 'Logged out') {
        this.removeToken();
        
        // Show logout reason
        if (reason !== 'Logged out') {
            alert(reason);
        }
        
        // Redirect to appropriate login page
        const currentPath = window.location.pathname;
        if (currentPath.includes('staff') || currentPath.includes('admin')) {
            window.location.href = 'staff-login.html';
        } else {
            window.location.href = 'login.html';
        }
    }

    // Validate current session
    async validateSession() {
        const token = this.getToken();
        if (!token) {
            return false;
        }

        try {
            const response = await fetch('http://localhost:9090/api/auth/validate-token', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const data = await response.json();
                if (data.status === 'success') {
                    this.updateActivity();
                    return true;
                }
            }
            
            this.logout('Session validation failed');
            return false;
        } catch (error) {
            console.error('Session validation error:', error);
            return false;
        }
    }

    // XSS Protection for form data
    sanitizeFormData(formData) {
        const sanitized = {};
        for (const [key, value] of Object.entries(formData)) {
            if (typeof value === 'string') {
                sanitized[key] = this.sanitizeInput(value);
            } else {
                sanitized[key] = value;
            }
        }
        return sanitized;
    }
}

// Initialize security manager
const securityManager = new SecurityManager();

// Global logout function
function logout() {
    securityManager.logout();
}

// Secure form submission helper
async function secureSubmit(url, formData, options = {}) {
    try {
        const sanitizedData = securityManager.sanitizeFormData(formData);
        
        const response = await securityManager.secureApiCall(url, {
            method: 'POST',
            body: JSON.stringify(sanitizedData),
            ...options
        });

        return response;
    } catch (error) {
        console.error('Secure submit failed:', error);
        throw error;
    }
}