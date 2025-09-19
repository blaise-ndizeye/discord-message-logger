# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a Discord Message Logger built with Kotlin + Spring Boot that logs Discord messages to MongoDB and provides both Discord slash commands and REST API endpoints for querying the data.

**Tech Stack**: Kotlin, Spring Boot 3.4.0, MongoDB, JDA (Java Discord API) 5.0.0, Docker

## Architecture

The application follows a standard Spring Boot MVC pattern with these key components:

### Core Architecture Flow
1. **Discord Events** → `MessageListener` receives Discord events (message received/updated/deleted)
2. **Event Processing** → `MessageLoggerService` processes and converts JDA Message objects to `DiscordMessage` entities
3. **Data Persistence** → `DiscordMessageRepository` (MongoDB) stores and queries messages with optimized indexes
4. **API Layer** → `MessageController` exposes REST endpoints for external queries
5. **Discord Commands** → `CommandHandlerService` handles slash commands and responds within Discord

### Key Components

**Models (`com.discordbot.messagelogger.model`)**:
- `DiscordMessage` - Main entity representing a Discord message with all metadata
- `MessageAttachment`, `MessageEmbed`, `MessageReaction` - Supporting data structures

**Services (`com.discordbot.messagelogger.service`)**:
- `MessageLoggerService` - Core business logic for message CRUD operations and JDA↔MongoDB conversion
- `CommandHandlerService` - Handles Discord slash commands (`/stats`, `/search`, `/recent`, `/user-messages`)

**Repository (`com.discordbot.messagelogger.repository`)**:
- `DiscordMessageRepository` - MongoDB repository with custom queries for complex searches and aggregations

**Configuration (`com.discordbot.messagelogger.config`)**:
- `DiscordBotConfig` - JDA setup, event listener registration, and slash command registration

**REST API (`com.discordbot.messagelogger.controller`)**:
- `MessageController` - Paginated REST endpoints for external access to logged data

## Common Development Commands

### Local Development
```bash
# Run the application locally (requires MongoDB running)
./gradlew bootRun

# Run tests
./gradlew test

# Build JAR
./gradlew build

# Clean build
./gradlew clean build
```

### Docker Development
```bash
# Start all services (MongoDB + Application + Mongo Express)
docker-compose up -d

# View application logs
docker-compose logs -f discord-logger

# View all logs
docker-compose logs -f

# Stop all services
docker-compose down

# Rebuild application image and restart
docker-compose up -d --build discord-logger

# Start with database admin tools
docker-compose --profile tools up -d
```

### Testing Commands
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests MessageControllerTest

# Run specific test method
./gradlew test --tests MessageLoggerServiceTest.testLogMessage

# Run tests with verbose output
./gradlew test --info
```

## Environment Configuration

The application uses these key environment variables:

- `DISCORD_BOT_TOKEN` - Discord bot token (required)
- `MONGODB_URI` - MongoDB connection string
- `SERVER_PORT` - Application port (default: 8080)
- `SPRING_PROFILES_ACTIVE` - Spring profiles (docker/local)

Copy `.env.example` to `.env` and configure before running.

## Database Schema

### Messages Collection
- **Indexes**: Optimized for queries on `messageId` (unique), `channelId + timestamp`, `authorId + timestamp`, `timestamp`, and full-text search on `content`
- **Schema**: Rich message data including attachments, embeds, reactions, and Discord metadata

### Key Query Patterns
- Channel-based pagination: `findByChannelId(channelId, pageable)`
- Author-based queries: `findByAuthorId(authorId, pageable)`
- Full-text search: `findByContentContainingIgnoreCase(query, pageable)`
- Time-based queries: `findRecentMessages(since, pageable)`

## API Endpoints

Base URL: `http://localhost:8080/api/messages`

- `GET /channel/{channelId}` - Get paginated messages by channel
- `GET /author/{authorId}` - Get paginated messages by author  
- `GET /search?query=...` - Full-text search messages
- `GET /recent?since=...` - Get messages since timestamp
- `GET /stats/channel/{channelId}` - Get channel statistics
- `GET /health` - Health check endpoint

All endpoints support pagination with `page`, `size`, and `sort` parameters.

## Discord Commands

The bot registers these slash commands:
- `/stats` - Channel message statistics
- `/search query:text` - Search messages
- `/recent hours:24` - Recent messages  
- `/user-messages user:@mention` - Messages by specific user

## Development Notes

### Message Processing Flow
1. `MessageListener.onMessageReceived()` captures Discord events
2. `MessageLoggerService.logMessage()` converts JDA Message → DiscordMessage
3. Duplicate checking via `findByMessageId()` before saving
4. Rich metadata extraction (attachments, embeds, reactions)
5. Optimized MongoDB storage with proper indexing

### Service Layer Architecture
- **MessageLoggerService**: Core CRUD + JDA conversion logic
- **CommandHandlerService**: Discord slash command handling with formatted responses
- Clear separation between Discord API logic and database operations

### Error Handling Patterns
- All service methods include try-catch blocks with proper logging
- Discord event failures are logged but don't crash the application
- Graceful degradation for missing Discord metadata (optional guild info)

## Testing

Tests are located in `src/test/kotlin/com/discordbot/messagelogger/` and use:
- **JUnit 5** for test framework
- **MockK** for Kotlin-specific mocking
- **Spring Boot Test** for integration testing

Key test classes:
- `MessageControllerTest` - REST API endpoint testing
- `MessageLoggerServiceTest` - Business logic testing  
- `DiscordMessageLoggerApplicationTests` - Application context loading

## Production Deployment

Use `docker-compose.prod.yml` for production with Docker secrets:

```bash
# Create secrets
echo "your-bot-token" | docker secret create discord_bot_token -
echo "secure-db-password" | docker secret create mongodb_password -

# Deploy
docker-compose -f docker-compose.prod.yml up -d
```

## Monitoring

- **Health Check**: `GET /api/messages/health`
- **Application Logs**: `./logs/` directory (Docker volume mounted)
- **MongoDB Logs**: Via Docker container logs
- **Discord Connection**: JDA provides connection status logging