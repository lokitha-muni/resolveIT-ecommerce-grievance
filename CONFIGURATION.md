# Configuration Setup Guide

## Environment Variables Setup

1. **Copy the environment file:**
   ```bash
   cp backend/test-spring/.env.example backend/test-spring/.env
   ```

2. **Update the .env file with your actual credentials:**

### Email Configuration (Gmail)
- `EMAIL_USERNAME`: Your Gmail address
- `EMAIL_PASSWORD`: Your Gmail App Password (not regular password)

### MongoDB Configuration
- `MONGODB_USERNAME`: Your MongoDB Atlas username
- `MONGODB_PASSWORD`: Your MongoDB Atlas password
- `MONGODB_CLUSTER`: Your MongoDB cluster URL
- `MONGODB_DATABASE`: Your database name

### JWT Configuration
- `JWT_SECRET`: A secure secret key (minimum 32 characters)
- `JWT_EXPIRATION`: Token expiration time in milliseconds

## EmailJS Configuration

1. **Update frontend/forgot-password.html:**
   - Replace `YOUR_EMAILJS_PUBLIC_KEY` with your EmailJS public key
   - Replace `YOUR_SERVICE_ID` with your EmailJS service ID
   - Replace `YOUR_TEMPLATE_ID` with your EmailJS template ID

2. **EmailJS Template Parameters:**
   Your template should include these variables:
   - `{{passcode}}` - The OTP code
   - `{{time}}` - Expiration time (15 minutes)
   - `{{email}}` - User's email address

## Security Notes

- Never commit the actual .env file to version control
- Use strong, unique passwords for all services
- Regularly rotate your API keys and passwords
- Use environment-specific configurations for production