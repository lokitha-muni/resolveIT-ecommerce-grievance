# ResolveIT - E-commerce Grievance System

A complete web application for managing e-commerce delivery complaints with user authentication and modern UI.

## 🏗️ Project Structure

```
Ecommerce_grievance/
├── frontend/          # HTML, CSS, JavaScript files
│   ├── index.html     # Landing page
│   ├── login.html     # User login
│   ├── register.html  # User registration
│   ├── dashboard.html # User dashboard
│   ├── style.css      # Modern styling
│   └── package.json   # Frontend dependencies
├── backend/           # Spring Boot application
│   └── test-spring/   # Java backend
│       ├── src/       # Source code
│       ├── pom.xml    # Maven dependencies
│       └── package.json # Backend scripts
└── README.md          # This file
```

## 🚀 Running the Application

### Backend (Spring Boot)
```bash
cd backend/test-spring
npm start
# Server runs on: http://localhost:9090
```

### Frontend (Static Files)
```bash
cd frontend
npm start
# Frontend runs on: http://localhost:3000
```

## 🔌 API Endpoints

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `GET /api/auth/users` - View all users (debug)

## 🛠️ Technologies

**Frontend**: HTML5, CSS3, JavaScript, Responsive Design
**Backend**: Spring Boot 4.0, Java 21, Spring Security
**Database**: H2 In-Memory Database, JPA/Hibernate

## 🔐 Features

✅ User registration with validation
✅ Secure login authentication  
✅ Password encryption (BCrypt)
✅ Modern responsive UI
✅ RESTful API design
✅ Database integration

## 📱 Access Points

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:9090/api/*
- **H2 Console**: http://localhost:9090/h2-console

## 🎯 Usage

1. Start backend server
2. Start frontend server  
3. Register new account
4. Login with credentials
5. Access dashboard features