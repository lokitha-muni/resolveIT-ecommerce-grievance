# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability, please send an email to security@resolveit.com. All security vulnerabilities will be promptly addressed.

## Security Features

- JWT Authentication with secure token management
- Password encryption using BCrypt
- Input sanitization to prevent XSS attacks
- Rate limiting to prevent abuse
- Session management with auto-logout
- Environment variable protection
- CORS protection

## Environment Setup

1. Copy `.env.example` to `.env`
2. Update all placeholder values with your actual credentials
3. Never commit `.env` files to version control
4. Use strong passwords and secure JWT secrets (minimum 32 characters)

## Best Practices

- Regularly update dependencies
- Use HTTPS in production
- Implement proper logging and monitoring
- Regular security audits
- Keep MongoDB credentials secure
- Use environment variables for all sensitive data