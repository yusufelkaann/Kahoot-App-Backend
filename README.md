# Kahoot App

A Spring Boot-based interactive quiz application inspired by Kahoot, allowing users to create, manage, and play quizzes in real-time.

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

Kahoot App is a web-based quiz platform built with Spring Boot that enables users to create interactive quizzes, manage game rooms, and participate in real-time quiz sessions. The application provides a RESTful API for managing quizzes, questions, answer options, players, and game sessions.

## ✨ Features

- **Quiz Management**: Create, update, and delete quizzes with multiple questions
- **Question & Answer Options**: Support for multiple-choice questions with customizable answer options
- **Room Management**: Create and manage game rooms for quiz sessions
- **Player Management**: Track players and their participation in games
- **Game Controller**: Handle game flow and session management
- **PostgreSQL Database**: Persistent storage for all quiz data
- **RESTful API**: Well-structured REST endpoints for all operations

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

### Quiz Endpoints
- `GET /api/quizzes` - Get all quizzes
- `GET /api/quizzes/{id}` - Get quiz by ID
- `POST /api/quizzes` - Create a new quiz
- `PUT /api/quizzes/{id}` - Update a quiz
- `DELETE /api/quizzes/{id}` - Delete a quiz

### Game Endpoints
- `GET /api/games` - Get all games
- `POST /api/games` - Create a new game session
- `GET /api/games/{id}` - Get game details

### Player Endpoints
- `GET /api/players` - Get all players
- `POST /api/players` - Register a new player
- `GET /api/players/{id}` - Get player details

### Room Endpoints
- `GET /api/rooms` - Get all rooms
- `POST /api/rooms` - Create a new room
- `GET /api/rooms/{id}` - Get room details
- `PUT /api/rooms/{id}` - Update room status

## 🗄 Database Schema

The application uses the following main entities:

- **Quiz**: Contains quiz information
- **Question**: Quiz questions with multiple answer options
- **AnswerOption**: Possible answers for questions
- **Player**: Users participating in quizzes
- **Room**: Game rooms for quiz sessions
- **Game**: Active game sessions

The database schema is automatically managed by Hibernate with `ddl-auto=update`.

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
