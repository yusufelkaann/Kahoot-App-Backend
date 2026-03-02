# Kahoot App

A Spring Boot-based interactive quiz application inspired by Kahoot, allowing users to create quizzes and host real-time quiz games in rooms.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

Kahoot App is a web-based quiz platform built with Spring Boot that enables users to:
- Create quizzes with multiple-choice questions
- Generate game rooms with unique 6-digit codes
- Host quiz sessions where players can join using room codes
- Track player scores and game progress
- Manage quiz flow with timed questions and points system

## ✨ Features

- **Quiz Management**: Create quizzes with multiple-choice questions, each with exactly one correct answer
- **Question Configuration**: Set time limits and points for each question
- **Room System**: Generate unique 6-digit room codes for game sessions
- **Player Management**: Players join rooms with nicknames, duplicate nicknames prevented per room
- **Game Flow**: Start games, track current question, and finish sessions
- **Quiz Assignment**: Assign quizzes to rooms before starting
- **Room States**: WAITING, STARTED, and FINISHED status tracking
- **Score Tracking**: Maintain player scores throughout the game
- **Validation**: Business rules enforced (single correct answer per question, positive points/time limits)

## 🛠 Technologies

- **Java 17**
- **Spring Boot 3.5.10**
  - Spring Data JPA
  - Spring Web
  - Spring Boot Validation
  - Spring Boot DevTools
- **PostgreSQL** - Database
- **Lombok** - Reduce boilerplate code
- **Maven** - Build and dependency management

## 📦 Prerequisites

Before running this application, ensure you have the following installed:

- **JDK 17** or higher
- **Maven 3.6+**
- **PostgreSQL 12+**
- **Git** (for cloning the repository)

## 🚀 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yusufelkaann/Kahoot_App.git
   cd Kahoot_App
   ```

2. **Set up PostgreSQL Database**
   ```bash
   # Login to PostgreSQL
   psql -U postgres
   
   # Create database
   CREATE DATABASE quizapp;
   
   # Exit psql
   \q
   ```

3. **Configure environment variables** (optional)
   
   You can set the following environment variables or update `application.properties`:
   - `DB_URL`: Database connection URL
   - `DB_USERNAME`: Database username
   - `DB_PASSWORD`: Database password

## ⚙️ Configuration

Update the `src/main/resources/application.properties` file with your database credentials:

```properties
spring.application.name=Kahoot_App
spring.datasource.url=jdbc:postgresql://localhost:5432/quizapp
spring.datasource.username=your_username
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

**Note**: For production, use environment variables instead of hardcoding credentials.

## 🏃 Running the Application

### Using Maven

```bash
# Clean and build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

### Using Java

```bash
# Build the JAR file
./mvnw clean package

# Run the JAR
java -jar target/Kahoot_App-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## 📁 Project Structure

```
Kahoot_App/
├── src/
│   ├── main/
│   │   ├── java/com/kahoot_app/Kahoot_App/
│   │   │   ├── KahootAppApplication.java     # Main application class
│   │   │   ├── game/                          # Game management module
│   │   │   │   ├── controller/                # Game controllers
│   │   │   │   └── service/                   # Game services
│   │   │   ├── player/                        # Player management module
│   │   │   │   ├── dtos/                      # Player DTOs
│   │   │   │   ├── entities/                  # Player entities
│   │   │   │   └── repositories/              # Player repositories
│   │   │   ├── quiz/                          # Quiz management module
│   │   │   │   ├── controllers/               # Quiz controllers
│   │   │   │   ├── dtos/                      # Quiz DTOs
│   │   │   │   ├── entities/                  # Quiz entities
│   │   │   │   ├── mappers/                   # Entity-DTO mappers
│   │   │   │   ├── repository/                # Quiz repositories
│   │   │   │   └── services/                  # Quiz services
│   │   │   ├── room/                          # Room management module
│   │   │   │   ├── dtos/                      # Room DTOs
│   │   │   │   ├── entities/                  # Room entities
│   │   │   │   ├── enums/                     # Room enums
│   │   │   │   ├── mappers/                   # Entity-DTO mappers
│   │   │   │   └── repository/                # Room repositories
│   │   │   └── mappers/                       # Common mappers
│   │   └── resources/
│   │       ├── application.properties         # Application configuration
│   │       ├── static/                        # Static resources
│   │       └── templates/                     # Templates
│   └── test/                                  # Test files
├── pom.xml                                    # Maven configuration
└── README.md                                  # This file
```

## 🔌 API Endpoints

### Quiz Endpoints (Base: `/api/v1/quizzes`)
- `POST /api/v1/quizzes` - Create a new quiz with questions and answer options
- `GET /api/v1/quizzes` - Get all quizzes
- `GET /api/v1/quizzes/{id}` - Get quiz by ID

### Room/Game Endpoints (Base: `/api/v1/rooms`)
- `POST /api/v1/rooms` - Create an empty room (generates 6-digit room code)
- `POST /api/v1/rooms/{roomCode}/join` - Join a room with nickname
  - Request body: `{ "nickname": "string" }`
- `POST /api/v1/rooms/{roomCode}/assign-quiz/{quizId}` - Assign a quiz to the room
- `POST /api/v1/rooms/{roomCode}/start` - Start the game (sets status to STARTED)
- `POST /api/v1/rooms/{roomCode}/finish` - Start the game (sets status to FINISHED)
- `GET /api/v1/rooms/{roomCode}` - Get room details including players and status

## 🗄 Database Schema

The application uses the following entities:

### Quiz
- `id` (Long, PK)
- `title` (String, required)
- `description` (String, max 1000 chars)
- `createdAt` (LocalDateTime)
- One-to-Many relationship with Questions

### Question
- `id` (Long, PK)
- `questionText` (String, required)
- `timeLimitSeconds` (Integer, required)
- `points` (Integer, required)
- `orderIndex` (Integer, required)
- `quiz_id` (FK to Quiz)
- One-to-Many relationship with AnswerOptions

### AnswerOption
- `id` (Long, PK)
- `text` (String, required)
- `isCorrect` (Boolean, required)
- `question_id` (FK to Question)

### Room
- `id` (Long, PK)
- `roomCode` (String, unique, 6 digits)
- `status` (Enum: WAITING, STARTED, FINISHED)
- `quiz_id` (FK to Quiz, optional)
- `currentQuestionIndex` (Integer, default 0)
- `createdAt` (LocalDateTime)
- One-to-Many relationship with Players

### Player
- `id` (Long, PK)
- `nickname` (String, required)
- `score` (Integer, default 0)
- `room_id` (FK to Room)
- `joinedAt` (LocalDateTime)

**Business Rules:**
- Each question must have exactly one correct answer
- Points and time limit must be greater than 0
- Nicknames must be unique within a room
- Quiz can only be assigned to rooms in WAITING status
- Games can only start when room has quiz and at least one player

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👤 Author

**Yusuf Elkaan**

- GitHub: [@yusufelkaann](https://github.com/yusufelkaann)

## 📧 Contact

For questions or suggestions, please open an issue on GitHub.

---

**Note**: This project is currently in development. Features and documentation may change.
