// UI Enhancement Manager
class UIManager {
    constructor() {
        this.theme = localStorage.getItem('theme') || 'light';
        this.language = localStorage.getItem('language') || 'en';
        this.translations = {};
        this.init();
    }

    init() {
        this.setupTheme();
        this.setupToastContainer();
        this.setupLoadingOverlay();
        this.setupAccessibility();
        this.setupMobileOptimizations();
        this.loadTranslations();
    }

    // Dark Mode Implementation
    setupTheme() {
        document.documentElement.setAttribute('data-theme', this.theme);
        const themeToggle = document.getElementById('themeToggle');
        if (themeToggle) {
            themeToggle.checked = this.theme === 'dark';
            themeToggle.addEventListener('change', () => this.toggleTheme());
        }
    }

    toggleTheme() {
        this.theme = this.theme === 'light' ? 'dark' : 'light';
        document.documentElement.setAttribute('data-theme', this.theme);
        localStorage.setItem('theme', this.theme);
        this.showToast(this.translate('theme_changed'), 'success');
    }

    // Toast Notifications
    setupToastContainer() {
        if (!document.getElementById('toast-container')) {
            const container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container';
            container.setAttribute('aria-live', 'polite');
            document.body.appendChild(container);
        }
    }

    showToast(message, type = 'info', duration = 4000) {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.setAttribute('role', 'alert');
        
        const icon = this.getToastIcon(type);
        toast.innerHTML = `
            <div class="toast-icon">${icon}</div>
            <div class="toast-message">${message}</div>
            <button class="toast-close" onclick="this.parentElement.remove()" aria-label="Close notification">×</button>
        `;

        document.getElementById('toast-container').appendChild(toast);
        
        // Auto remove
        setTimeout(() => {
            if (toast.parentElement) {
                toast.classList.add('toast-fade-out');
                setTimeout(() => toast.remove(), 300);
            }
        }, duration);
    }

    getToastIcon(type) {
        const icons = {
            success: '✅',
            error: '❌',
            warning: '⚠️',
            info: 'ℹ️'
        };
        return icons[type] || icons.info;
    }

    // Loading States
    setupLoadingOverlay() {
        if (!document.getElementById('loading-overlay')) {
            const overlay = document.createElement('div');
            overlay.id = 'loading-overlay';
            overlay.className = 'loading-overlay hidden';
            overlay.innerHTML = `
                <div class="loading-spinner">
                    <div class="spinner"></div>
                    <div class="loading-text" id="loading-text">Loading...</div>
                </div>
            `;
            document.body.appendChild(overlay);
        }
    }

    showLoading(message = 'Loading...') {
        const overlay = document.getElementById('loading-overlay');
        const text = document.getElementById('loading-text');
        if (text) text.textContent = this.translate(message);
        overlay.classList.remove('hidden');
    }

    hideLoading() {
        const overlay = document.getElementById('loading-overlay');
        overlay.classList.add('hidden');
    }

    // Accessibility Features
    setupAccessibility() {
        // Skip to main content link
        if (!document.getElementById('skip-link')) {
            const skipLink = document.createElement('a');
            skipLink.id = 'skip-link';
            skipLink.href = '#main-content';
            skipLink.textContent = this.translate('skip_to_main');
            skipLink.className = 'skip-link';
            document.body.insertBefore(skipLink, document.body.firstChild);
        }

        // Focus management
        this.setupFocusManagement();
        
        // Keyboard navigation
        this.setupKeyboardNavigation();
    }

    setupFocusManagement() {
        // Trap focus in modals
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Tab') {
                const modal = document.querySelector('.modal:not(.hidden)');
                if (modal) {
                    this.trapFocus(e, modal);
                }
            }
        });
    }

    trapFocus(e, container) {
        const focusableElements = container.querySelectorAll(
            'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];

        if (e.shiftKey && document.activeElement === firstElement) {
            e.preventDefault();
            lastElement.focus();
        } else if (!e.shiftKey && document.activeElement === lastElement) {
            e.preventDefault();
            firstElement.focus();
        }
    }

    setupKeyboardNavigation() {
        // ESC key to close modals
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                const modal = document.querySelector('.modal:not(.hidden)');
                if (modal) {
                    this.closeModal(modal);
                }
            }
        });
    }

    // Mobile Optimizations
    setupMobileOptimizations() {
        // Touch gestures
        this.setupTouchGestures();
        
        // Responsive navigation
        this.setupMobileNavigation();
        
        // Viewport adjustments
        this.setupViewportAdjustments();
    }

    setupTouchGestures() {
        let startX, startY;
        
        document.addEventListener('touchstart', (e) => {
            startX = e.touches[0].clientX;
            startY = e.touches[0].clientY;
        });

        document.addEventListener('touchend', (e) => {
            if (!startX || !startY) return;
            
            const endX = e.changedTouches[0].clientX;
            const endY = e.changedTouches[0].clientY;
            
            const diffX = startX - endX;
            const diffY = startY - endY;
            
            // Swipe detection
            if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 50) {
                if (diffX > 0) {
                    this.handleSwipeLeft();
                } else {
                    this.handleSwipeRight();
                }
            }
        });
    }

    setupMobileNavigation() {
        const mobileMenuToggle = document.getElementById('mobile-menu-toggle');
        if (mobileMenuToggle) {
            mobileMenuToggle.addEventListener('click', () => {
                const nav = document.querySelector('.dashboard-nav');
                nav.classList.toggle('mobile-open');
            });
        }
    }

    setupViewportAdjustments() {
        // Handle viewport height changes on mobile
        const setVH = () => {
            const vh = window.innerHeight * 0.01;
            document.documentElement.style.setProperty('--vh', `${vh}px`);
        };
        
        setVH();
        window.addEventListener('resize', setVH);
        window.addEventListener('orientationchange', setVH);
    }

    // Internationalization
    loadTranslations() {
        this.translations = {
            en: {
                loading: 'Loading...',
                success: 'Success',
                error: 'Error',
                warning: 'Warning',
                theme_changed: 'Theme changed successfully',
                skip_to_main: 'Skip to main content',
                close: 'Close',
                menu: 'Menu',
                dashboard: 'Dashboard',
                complaints: 'Complaints',
                profile: 'Profile',
                logout: 'Logout',
                total_complaints: 'Total Complaints',
                pending: 'Pending',
                in_progress: 'In Progress',
                resolved: 'Resolved',
                welcome_back: 'Welcome Back',
                sign_in_account: 'Sign in to your account',
                hi_staff: 'Hi, Staff'
            },
            es: {
                loading: 'Cargando...',
                success: 'Éxito',
                error: 'Error',
                warning: 'Advertencia',
                theme_changed: 'Tema cambiado exitosamente',
                skip_to_main: 'Saltar al contenido principal',
                close: 'Cerrar',
                menu: 'Menú',
                dashboard: 'Panel',
                complaints: 'Quejas',
                profile: 'Perfil',
                logout: 'Cerrar sesión',
                total_complaints: 'Total de Quejas',
                pending: 'Pendiente',
                in_progress: 'En Progreso',
                resolved: 'Resuelto',
                welcome_back: 'Bienvenido de nuevo',
                sign_in_account: 'Inicia sesión en tu cuenta',
                hi_staff: 'Hola, Personal'
            },
            fr: {
                loading: 'Chargement...',
                success: 'Succès',
                error: 'Erreur',
                warning: 'Avertissement',
                theme_changed: 'Thème changé avec succès',
                skip_to_main: 'Aller au contenu principal',
                close: 'Fermer',
                menu: 'Menu',
                dashboard: 'Tableau de bord',
                complaints: 'Plaintes',
                profile: 'Profil',
                logout: 'Déconnexion',
                total_complaints: 'Total des Plaintes',
                pending: 'En attente',
                in_progress: 'En cours',
                resolved: 'Résolu',
                welcome_back: 'Bon retour',
                sign_in_account: 'Connectez-vous à votre compte',
                hi_staff: 'Salut, Personnel'
            }
        };
        
        this.updatePageTranslations();
    }

    translate(key) {
        return this.translations[this.language]?.[key] || key;
    }

    changeLanguage(lang) {
        this.language = lang;
        localStorage.setItem('language', lang);
        this.updatePageTranslations();
        this.showToast(this.translate('success'), 'success');
    }

    updatePageTranslations() {
        document.querySelectorAll('[data-translate]').forEach(element => {
            const key = element.getAttribute('data-translate');
            element.textContent = this.translate(key);
        });
    }

    // Utility Methods
    handleSwipeLeft() {
        // Handle left swipe (e.g., next page)
        console.log('Swipe left detected');
    }

    handleSwipeRight() {
        // Handle right swipe (e.g., previous page)
        console.log('Swipe right detected');
    }

    closeModal(modal) {
        modal.classList.add('hidden');
        // Return focus to trigger element
        const trigger = document.querySelector('[data-modal-trigger]');
        if (trigger) trigger.focus();
    }

    // Enhanced API calls with loading states
    async apiCall(url, options = {}, loadingMessage = 'loading') {
        this.showLoading(loadingMessage);
        
        try {
            const response = await fetch(url, options);
            const data = await response.json();
            
            if (response.ok) {
                this.showToast(this.translate('success'), 'success');
                return data;
            } else {
                this.showToast(data.message || this.translate('error'), 'error');
                return null;
            }
        } catch (error) {
            this.showToast(this.translate('error'), 'error');
            console.error('API Error:', error);
            return null;
        } finally {
            this.hideLoading();
        }
    }
}

// Initialize UI Manager
const uiManager = new UIManager();

// Global utility functions
function showToast(message, type = 'info') {
    uiManager.showToast(message, type);
}

function showLoading(message) {
    uiManager.showLoading(message);
}

function hideLoading() {
    uiManager.hideLoading();
}

function changeLanguage(lang) {
    uiManager.changeLanguage(lang);
}

function toggleTheme() {
    uiManager.toggleTheme();
}