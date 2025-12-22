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
        
        // Always redirect to index page
        window.location.href = 'index.html';
    }

    // Show logout confirmation modal
    showLogoutConfirmation() {
        return new Promise((resolve) => {
            // Create modal HTML
            const modal = document.createElement('div');
            modal.className = 'logout-modal-overlay';
            modal.innerHTML = `
                <div class="logout-modal">
                    <div class="logout-modal-header">
                        <h3>Confirm Logout</h3>
                    </div>
                    <div class="logout-modal-body">
                        <p>Are you sure you want to logout?</p>
                    </div>
                    <div class="logout-modal-actions">
                        <button class="logout-cancel-btn" onclick="this.closest('.logout-modal-overlay').remove(); resolve(false)">Cancel</button>
                        <button class="logout-confirm-btn" onclick="this.closest('.logout-modal-overlay').remove(); resolve(true)">Logout</button>
                    </div>
                </div>
            `;
            
            // Add styles
            const style = document.createElement('style');
            style.textContent = `
                .logout-modal-overlay {
                    position: fixed;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background: rgba(0, 0, 0, 0.5);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    z-index: 10000;
                    animation: fadeIn 0.3s ease;
                }
                .logout-modal {
                    background: white;
                    border-radius: 12px;
                    padding: 0;
                    min-width: 320px;
                    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
                    animation: slideIn 0.3s ease;
                }
                .logout-modal-header {
                    padding: 20px 24px 16px;
                    border-bottom: 1px solid #eee;
                }
                .logout-modal-header h3 {
                    margin: 0;
                    color: #333;
                    font-size: 18px;
                }
                .logout-modal-body {
                    padding: 20px 24px;
                }
                .logout-modal-body p {
                    margin: 0;
                    color: #666;
                    line-height: 1.5;
                }
                .logout-modal-actions {
                    padding: 16px 24px 20px;
                    display: flex;
                    gap: 12px;
                    justify-content: flex-end;
                }
                .logout-cancel-btn, .logout-confirm-btn {
                    padding: 8px 20px;
                    border: none;
                    border-radius: 6px;
                    cursor: pointer;
                    font-size: 14px;
                    font-weight: 500;
                    transition: all 0.2s;
                }
                .logout-cancel-btn {
                    background: #f5f5f5;
                    color: #666;
                }
                .logout-cancel-btn:hover {
                    background: #e5e5e5;
                }
                .logout-confirm-btn {
                    background: #dc3545;
                    color: white;
                }
                .logout-confirm-btn:hover {
                    background: #c82333;
                }
                @keyframes fadeIn {
                    from { opacity: 0; }
                    to { opacity: 1; }
                }
                @keyframes slideIn {
                    from { transform: translateY(-20px); opacity: 0; }
                    to { transform: translateY(0); opacity: 1; }
                }
            `;
            
            document.head.appendChild(style);
            document.body.appendChild(modal);
            
            // Handle button clicks
            modal.querySelector('.logout-cancel-btn').onclick = () => {
                modal.remove();
                style.remove();
                resolve(false);
            };
            
            modal.querySelector('.logout-confirm-btn').onclick = () => {
                modal.remove();
                style.remove();
                resolve(true);
            };
            
            // Close on overlay click
            modal.onclick = (e) => {
                if (e.target === modal) {
                    modal.remove();
                    style.remove();
                    resolve(false);
                }
            };
        });
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
    if (confirm('Are you sure you want to logout?')) {
        localStorage.clear();
        sessionStorage.clear();
        window.location.href = 'index.html';
    }
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
