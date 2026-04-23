# Kahoot App

A Spring Boot-based real-time interactive quiz application inspired by Kahoot, featuring WebSocket support, Redis caching, AI-powered quiz generation, and Docker deployment capabilities.

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Technologies](#-technologies)
- [API Endpoints](#-api-endpoints)
- [WebSocket Events](#-websocket-events)
- [Game Flow](#-game-flow)
- [Key Features Explained](#-key-features-explained)
- [Contributing](#-contributing)
- [Author](#-author)

## 🎯 Overview

Kahoot App is a modern web-based quiz platform built with Spring Boot that enables users to create and participate in real-time quiz competitions. The application features automatic question progression, live scoring updates via WebSocket, and efficient state management using Redis.

**Key Capabilities:**
- Create and manage quizzes with multiple-choice questions
- Generate quizzes instantly using AI (topic, difficulty, question count)
- Generate unique 6-digit room codes for game sessions
- Real-time gameplay with WebSocket communication
- Automatic and manual question advancement
- Timed questions with countdown timers
- Live leaderboard updates
- Host and player role management

## ✨ Features

### Quiz Management
- Create quizzes with unlimited questions
- Configure time limits (seconds) for each question
- Assign points to questions
- Multiple-choice questions with exactly one correct answer
- Question ordering support

### AI Quiz Generation
- Generate a complete quiz from a topic, difficulty, and question count
- Powered by Spring AI with structured output — returns a fully validated quiz ready to play
- Difficulty controls time limits and point values automatically (easy/medium/hard)
- Up to 20 questions per generation

### Game Room System
- Generate unique 6-digit room codes
- Three room states: **WAITING**, **STARTED**, **FINISHED**
- Host role with game control capabilities
- Player role with answer submission rights
- Prevent duplicate nicknames within rooms

### Real-Time Gameplay
- **WebSocket Integration**: Live updates for all participants
- **Automatic Question Timer**: Questions auto-advance after time expires
- **Manual Advance**: Host can manually skip to next question
- **Live Score Updates**: Real-time score broadcasting
- **Leaderboard**: Dynamic ranking of players

### State Management
- **Redis Caching**: Fast in-memory game state storage
- Score tracking per player per room
- Answer tracking to prevent duplicate submissions
- Timer management with token validation
- Room status synchronization

### Player System
- Two roles: **HOST** (game creator) and **PLAYER** (participant)
- Nickname-based identification
- Score persistence to database on game completion
- Join restrictions (only during WAITING phase)

## 🏗 Architecture

The application follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────┐
│          Controllers (REST + WebSocket)         │
├─────────────────────────────────────────────────┤
│               Service Layer                     │
│  ├─ GameService (Core game logic)               │
│  ├─ QuizService (Quiz management)               │
│  ├─ AiQuizService (AI quiz generation)          │
│  ├─ QuizSessionDomainService (Domain rules)     │
│  ├─ GameTimerService (Async timers)             │
│  └─ GameWebSocketService (Real-time events)     │
├─────────────────────────────────────────────────┤
│          Repository Layer (JPA)                 │
│  └─ GameSessionStateStore (state abstraction)   │
├─────────────────────────────────────────────────┤
│    PostgreSQL (Persistence) + Redis (Cache)     │
│    └─ RedisGameStateService implements          │
│       GameSessionStateStore                     │
└─────────────────────────────────────────────────┘
```

## 🛠 Technologies

### Backend Framework
- **Java 17**
- **Spring Boot 3.5.10**
  - Spring Data JPA (Database operations)
  - Spring Web (REST APIs)
  - Spring WebSocket (Real-time communication)
  - Spring Data Redis (Caching & state management)
  - Spring Boot Validation (Input validation)
  - Spring Boot DevTools (Development)

### Databases
- **PostgreSQL** - Persistent data storage
- **Redis** - In-memory cache for game state

### Tools & Libraries
- **Lombok** - Reduce boilerplate code
- **Maven** - Build and dependency management
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Spring AI** - AI quiz generation via chat model integration

### WebSocket Configuration

WebSocket endpoint is configured at `/ws` with SockJS fallback support. The broker uses:
- **Destination Prefix**: `/app`
- **Broker**: `/topic`
- **Allowed Origins**: `http://localhost:3000` (configurable in `WebSocketConfig.java`)

## 🔌 API Endpoints

### Quiz Management (`/api/v1/quizzes`)

#### Create Quiz
```http
POST /api/v1/quizzes
Content-Type: application/json

{
  "title": "Science Quiz",
  "description": "Test your science knowledge",
  "questions": [
    {
      "questionText": "What is H2O?",
      "timeLimitSeconds": 30,
      "points": 100,
      "orderIndex": 0,
      "options": [
        {"text": "Water", "isCorrect": true},
        {"text": "Oxygen", "isCorrect": false},
        {"text": "Hydrogen", "isCorrect": false},
        {"text": "Carbon Dioxide", "isCorrect": false}
      ]
    }
  ]
}
```

#### Get All Quizzes
```http
GET /api/v1/quizzes
```

#### Get Quiz by ID
```http
GET /api/v1/quizzes/{id}
```

#### Delete Quiz
```http
DELETE /api/v1/quizzes/{id}
```
Returns `409 Conflict` if the quiz is currently active in a game session.

#### Generate Quiz with AI
```http
POST /api/v1/quizzes/generate
Content-Type: application/json

{
  "topic": "World War II",
  "questionCount": 10,
  "difficulty": "medium"
}

Response: Fully generated QuizResponseDTO ready to assign to a room
```

### Game Room Management (`/api/v1/rooms`)

#### Create Room
```http
POST /api/v1/rooms
Content-Type: application/json

{
  "hostNickname": "GameMaster"
}

Response: Returns room with unique 6-digit code
```

#### Join Room
```http
POST /api/v1/rooms/{roomCode}/join
Content-Type: application/json

{
  "nickname": "Player1"
}
```

#### Assign Quiz to Room
```http
POST /api/v1/rooms/{roomCode}/assign-quiz/{quizId}
```

#### Start Game
```http
POST /api/v1/rooms/{roomCode}/start
```

#### Submit Answer
```http
POST /api/v1/rooms/{roomCode}/submit-answer?playerId={playerId}&answerOptionId={answerId}

Response: { "playerId": 1, "currentScore": 250 }
```

#### Advance Question (Manual - Host Only)
```http
POST /api/v1/rooms/{roomCode}/advance?hostPlayerId={hostId}
```

#### Finish Game
```http
POST /api/v1/rooms/{roomCode}/finish
```

#### Get Room Details
```http
GET /api/v1/rooms/{roomCode}
```

#### Get Current Question
```http
GET /api/v1/rooms/{roomCode}/current-question
```

#### Get Leaderboard
```http
GET /api/v1/rooms/{roomCode}/leaderboard

Response: [
  { "playerId": 1, "nickname": "Player1", "score": 350 },
  { "playerId": 2, "nickname": "Player2", "score": 200 }
]
```

#### Get Time Remaining
```http
GET /api/v1/rooms/{roomCode}/time-remaining

Response: { "secondsRemaining": 15 }
```

## 🔄 WebSocket Events

Connect to WebSocket at: `ws://localhost:8080/ws` (with SockJS)

### Subscribe Topics

#### Room Updates
```
/topic/room/{roomCode}
```
Broadcasts: Complete room state changes

#### Question Advances
```
/topic/room/{roomCode}/question
```
Broadcasts: New question index when question changes

#### Score Updates
```
/topic/room/{roomCode}/scores
```
Broadcasts: `{ "playerId": 1, "score": 250 }`

#### Game Finished
```
/topic/room/{roomCode}/finish
```
Broadcasts: `{ "status": "FINISHED" }`

## 🎮 Game Flow

### 1. Room Creation Phase
```
Host creates room → Generates 6-digit code → Room status: WAITING
```

### 2. Setup Phase
```
Players join room → Host assigns quiz → Players wait in lobby
```

### 3. Game Start
```
Host starts game → Room status: STARTED → Timer starts for first question
```

### 4. Question Cycle
```
┌─────────────────────────────────────────┐
│ Question displayed to all players       │
├─────────────────────────────────────────┤
│ Timer counts down (auto-advance)        │
├─────────────────────────────────────────┤
│ Players submit answers (once per Q)     │
├─────────────────────────────────────────┤
│ Score calculated & stored in Redis      │
├─────────────────────────────────────────┤
│ Timer expires OR Host advances manually │
├─────────────────────────────────────────┤
│ Next question OR Finish game           │
└─────────────────────────────────────────┘
```

### 5. Game End
```
Last question completed → Room status: FINISHED → Scores synced to DB → Leaderboard displayed
```

## 🧩 Key Features Explained

### Timer System
- Each question has a configurable time limit
- Timer runs asynchronously using `@Async`
- Automatically advances to next question when time expires
- Timer token prevents race conditions
- Host can manually advance before timer expires

### Score Calculation
- Only correct answers earn points
- Score = Question points (no time bonus currently)
- Scores stored in Redis during game (fast access)
- Scores persisted to PostgreSQL on game finish
- Host cannot submit answers

### Redis State Management
Redis is used as the real-time game state store for all active sessions:

- **Game Status**: Stored per room (WAITING / STARTED / FINISHED)
- **Current Question Index**: Tracked in real-time
- **Scores**: Hash map of `playerId → score` per room
- **Answers**: Tracked per question to prevent duplicate submissions
- **Timer State**: TTL-based countdown, token-validated to prevent race conditions

Redis state is cleared on game finish; final scores are persisted to PostgreSQL at that point.

#### GameSessionStateStore Abstraction
All Redis access goes through the `GameSessionStateStore` interface. `RedisGameStateService` implements this interface and handles all Redis operations. `GameService` and `GameTimerService` depend only on the interface — not on Redis directly — making the storage layer swappable and independently testable.

### AI Quiz Generation
Quizzes can be generated from a single API call using Spring AI:
- **Input**: topic, difficulty (`easy` / `medium` / `hard`), question count (1–20)
- **Output**: A fully structured quiz persisted to the database, immediately assignable to a room
- Difficulty automatically sets `timeLimitSeconds` (30 / 20 / 15) and `points` (100 / 200 / 300) per question
- Uses structured output — the AI response is deserialized directly into `QuizRequestDTO` and validated before saving

### Quiz Deletion & Active Session Guard
- Quizzes can be deleted via `DELETE /api/v1/quizzes/{id}`
- Before deletion, `QuizSessionDomainService` checks whether the quiz is currently assigned to any room with status `STARTED`
- If it is, the request is rejected with `409 Conflict` — you cannot delete a quiz mid-game
- This check is intentionally a **domain service** (not a repository query or a flag on the quiz), because "is this quiz active?" is a fact about the relationship between two aggregates (`Quiz` and `Room`), not a property of the quiz itself

### WebSocket Broadcasting
All game events are broadcast to room participants:
- Room state changes
- Question advances
- Score updates
- Game completion

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 👤 Author

**Yusuf Elkaan**

- GitHub: [@yusufelkaann](https://github.com/yusufelkaann)

---

**Last Updated**: April 2026  
**Version**: 0.0.1-SNAPSHOT  
**Status**: Active Development
