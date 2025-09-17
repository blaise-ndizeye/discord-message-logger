# Discord Message Logger

A comprehensive Discord bot that logs all messages in channels and stores them in MongoDB database, providing both Discord slash commands and REST API endpoints for querying the data.

## Features

- 🤖 **Discord Bot Integration**: Automatically logs all messages from Discord channels
- 📊 **Message Analytics**: Get statistics about channel activity and user participation  
- 🔍 **Advanced Search**: Search through message history with full-text search
- 🌐 **REST API**: Access logged data via comprehensive REST endpoints
- 📱 **Slash Commands**: Query data directly from Discord using `/` commands
- 🐳 **Docker Support**: Easy deployment with Docker and Docker Compose
- 🔒 **Production Ready**: Includes security best practices and secrets management
- 📈 **Scalable Architecture**: Built with Spring Boot and MongoDB for high performance

## Tech Stack

- **Backend**: Kotlin + Spring Boot 3.4.0
- **Database**: MongoDB with optimized indexes
- **Discord**: JDA (Java Discord API) 5.0.0
- **Containerization**: Docker & Docker Compose
- **Testing**: JUnit 5 + MockK
- **Architecture**: MVC pattern with service layer

## Quick Start

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Discord Bot Token

### 1. Setup Discord Bot

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Create a new application and bot
3. Copy the bot token
4. Enable these bot permissions:
   - Read Messages/View Channels
   - Send Messages
   - Use Slash Commands
   - Read Message History

### 2. Environment Configuration

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` and add your Discord bot token:

```bash
DISCORD_BOT_TOKEN=your-discord-bot-token-here
MONGODB_PASSWORD=your-secure-password
```

### 3. Run with Docker

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f discord-logger

# Stop services
docker-compose down
```

### 4. Invite Bot to Server

Use this URL (replace CLIENT_ID with your bot's client ID):
```
https://discord.com/api/oauth2/authorize?client_id=YOUR_CLIENT_ID&permissions=68608&scope=bot%20applications.commands
```

## Discord Commands

The bot provides these slash commands:

| Command | Description | Usage |
|---------|-------------|--------|
| `/stats` | Get channel statistics | `/stats` |
| `/search` | Search messages | `/search query:hello world` |
| `/recent` | Get recent messages | `/recent hours:24` |
| `/user-messages` | Get messages from user | `/user-messages user:@username` |

## REST API

The application exposes REST endpoints at `http://localhost:8080/api/messages`:

### Endpoints

```http
# Get messages by channel
GET /api/messages/channel/{channelId}?page=0&size=20&sort=timestamp,desc

# Get messages by author
GET /api/messages/author/{authorId}?page=0&size=20

# Search messages
GET /api/messages/search?query=hello&page=0&size=20

# Get recent messages
GET /api/messages/recent?since=2024-01-01T00:00:00

# Get channel statistics
GET /api/messages/stats/channel/{channelId}

# Health check
GET /api/messages/health
```

### Example Response

```json
{
  "content": [
    {
      "id": "507f1f77bcf86cd799439011",
      "messageId": "1234567890123456789",
      "channelId": "987654321098765432",
      "channelName": "general",
      "guildId": "111222333444555666",
      "guildName": "My Discord Server",
      "authorId": "123456789012345678",
      "authorName": "JohnDoe",
      "authorDiscriminator": "0001",
      "content": "Hello, world!",
      "timestamp": "2024-01-15T10:30:00",
      "isBot": false,
      "messageType": "DEFAULT"
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20
}
```

## Development

### Local Development Setup

1. **Clone the repository**:
```bash
git clone <repository-url>
cd discord-message-logger
```

2. **Set up MongoDB** (if not using Docker):
```bash
# Install MongoDB locally or use MongoDB Atlas
# Create database: discord-messages
```

3. **Configure application.properties**:
```properties
discord.bot.token=your-bot-token
spring.data.mongodb.uri=mongodb://localhost:27017/discord-messages
```

4. **Run the application**:
```bash
./gradlew bootRun
```

5. **Run tests**:
```bash
./gradlew test
```

### Building

```bash
# Build JAR
./gradlew build

# Build Docker image
docker build -t discord-message-logger .
```

## Production Deployment

### Using Docker Secrets

For production environments, use Docker secrets:

1. **Create secret files**:
```bash
echo "your-bot-token" | docker secret create discord_bot_token -
echo "production-db-password" | docker secret create mongodb_password -
```

2. **Deploy with production compose**:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Configuration Options

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `DISCORD_BOT_TOKEN` | Discord bot token | Required |
| `MONGODB_URI` | MongoDB connection string | `mongodb://localhost:27017/discord-messages` |
| `SERVER_PORT` | Application port | `8080` |
| `JAVA_OPTS` | JVM options | `-Xmx512m -Xms256m` |

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Discord API   │◄──►│  Message        │◄──►│    MongoDB      │
│                 │    │  Listener       │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Service       │
                       │   Layer         │
                       └─────────────────┘
                              │
                    ┌─────────┼─────────┐
                    ▼         ▼         ▼
            ┌──────────┐ ┌──────────┐ ┌──────────┐
            │   REST   │ │  Slash   │ │   Web    │
            │   API    │ │ Commands │ │    UI    │
            └──────────┘ └──────────┘ └──────────┘
```

## Database Schema

### Messages Collection

```javascript
{
  _id: ObjectId,
  messageId: String,        // Discord message ID (unique)
  channelId: String,        // Discord channel ID  
  channelName: String,      // Channel name
  guildId: String,          // Discord guild/server ID
  guildName: String,        // Guild/server name
  authorId: String,         // Message author ID
  authorName: String,       // Author username
  authorDiscriminator: String, // Author discriminator
  content: String,          // Message content
  timestamp: Date,          // When message was sent
  editedTimestamp: Date,    // When message was edited (optional)
  attachments: Array,       // File attachments
  embeds: Array,           // Rich embeds
  reactions: Array,        // Message reactions
  isBot: Boolean,          // Whether author is a bot
  messageType: String      // Message type (DEFAULT, REPLY, etc.)
}
```

### Indexes

The application creates optimized indexes for fast queries:

- `messageId` (unique)
- `channelId` + `timestamp` (compound)
- `authorId` + `timestamp` (compound)
- `timestamp` (descending)
- `content` (text search)

## Monitoring

### Health Checks

- **Application**: `GET /api/messages/health`
- **Docker Health**: Built-in container health checks
- **MongoDB**: Database connection monitoring

### Logging

- Application logs are written to `./logs/` directory
- Docker containers use JSON file logging with rotation
- Log levels configurable via `application.properties`

## Security Considerations

- ✅ Non-root user in Docker containers
- ✅ Secrets management with Docker secrets
- ✅ Environment variable configuration
- ✅ Input validation on all endpoints
- ✅ CORS configuration for API endpoints
- ✅ Database connection with authentication

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please follow the [conventional commits](https://conventionalcommits.org/) specification.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

If you encounter any issues or have questions:

1. Check the [troubleshooting guide](docs/TROUBLESHOOTING.md)
2. Search existing [GitHub issues](issues)
3. Create a new issue with detailed information

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for a list of changes and version history.