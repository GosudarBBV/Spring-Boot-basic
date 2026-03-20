# 📚 Book Store API

## 🚀 Introduction
This project is a RESTful API for an online bookstore.  
It allows users to register, authenticate, browse books, manage a shopping cart, and place orders.

The goal of this project was to practice building a real-world backend application using modern Java technologies such as Spring Boot and Spring Security.

---

## 🛠️ Technologies Used

- Java 17
- Spring Boot 3
- Spring Security (JWT authentication)
- Spring Data JPA (Hibernate)
- MySQL
- Liquibase (database migrations)
- MapStruct (DTO mapping)
- Lombok
- Swagger (OpenAPI documentation)
- Maven

---

## 🔐 Features

### 👤 Authentication
- User registration
- User login with JWT token
- Role-based authorization

### 📚 Book Management
- Create, update, delete books (ADMIN)
- Get all books / get book by id (USER)

### 🗂️ Categories
- Create and manage categories
- Assign books to categories

### 🛒 Shopping Cart
- Add books to cart
- Update quantity
- Remove items
- View cart

### 📦 Orders
- Place order
- View order history
- Update order status (ADMIN)

---

## 📊 API Documentation

Swagger UI is available at:


http://localhost:8080/api/swagger-ui/index.html


---

## ⚙️ How to Run the Project

### 1. Clone repository
```bash
git clone <your-repo-link>
cd project-folder
```

### 2. Configure database

```
In application.properties:

spring.datasource.url=jdbc:mysql://localhost:3306/book_database
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Run the application

```
mvn clean install
mvn spring-boot:run
```

### 🧪 Example Request (Registration)

POST /api/auth/registration
```
{
  "email": "user@gmail.com",
  "password": "password123",
  "repeatPassword": "password123",
  "firstName": "Ivan",
  "lastName": "Petrenko",
  "shippingAddress": "Kyiv, Ukraine"
}
```

## 🎥 Demo Video

👉 [Watch Demo Video on YouTube](https://youtu.be/_sADYjuyfeM)

![ER Diagram](src/main/resources/images/er-diagram.png)

## 🔐 Role-based Access to Endpoints

The application implements role-based access control using Spring Security and JWT authentication.  
Endpoints are доступні залежно від ролі користувача: **USER** або **ADMIN**.

---

### 👤 User Endpoints

| Method | Endpoint | Description |
|-------|--------|------------|
| POST | `/api/auth/registration` | Register a new user |
| POST | `/api/auth/login` | Authenticate user and receive JWT token |
| GET | `/api/books` | Get all books with pagination |
| GET | `/api/books/{id}` | Get book details by ID |
| GET | `/api/categories` | Get all categories |
| GET | `/api/categories/{id}` | Get category by ID |
| GET | `/api/categories/by-category/{id}` | Get books by category |
| GET | `/api/cart` | Get current user's shopping cart |
| POST | `/api/cart` | Add book to shopping cart |
| PUT | `/api/cart/items/{cartItemId}` | Update cart item quantity |
| DELETE | `/api/cart/items/{cartItemId}` | Remove item from cart |
| GET | `/api/orders` | Get user's order history |
| POST | `/api/orders` | Place a new order |
| GET | `/api/orders/{orderId}/items` | Get all items from order |
| GET | `/api/orders/{orderId}/items/{itemId}` | Get specific item from order |

---

### 🛠️ Admin Endpoints

| Method | Endpoint | Description |
|-------|--------|------------|
| POST | `/api/books` | Create a new book |
| PUT | `/api/books/{id}` | Update book |
| DELETE | `/api/books/{id}` | Delete book |
| POST | `/api/categories` | Create a new category |
| PUT | `/api/categories/{id}` | Update category |
| DELETE | `/api/categories/{id}` | Delete category |
| DELETE | `/api/users/{id}` | Delete user |
| PATCH | `/api/orders/{id}` | Update order status |

---

All endpoints (except authentication) require a valid JWT token in the Authorization header:

Authorization: Bearer <your_token>