# Docker Secrets

This directory contains example files for Docker secrets used in production.

## Setup for Production

1. Create the actual secret files (these should NOT be committed to git):

```bash
echo "your-actual-discord-bot-token" > secrets/discord_bot_token.txt
echo "your-secure-mongodb-username" > secrets/mongodb_username.txt
echo "your-secure-mongodb-password" > secrets/mongodb_password.txt
```

2. Set appropriate file permissions:

```bash
chmod 600 secrets/*.txt
```

3. Make sure these files are listed in `.gitignore`:

```
secrets/*.txt
*.env
```

## Example Files

- `discord_bot_token.example.txt` - Example Discord bot token file
- `mongodb_username.example.txt` - Example MongoDB username file  
- `mongodb_password.example.txt` - Example MongoDB password file

## Usage

Use the production Docker Compose file:

```bash
docker-compose -f docker-compose.prod.yml up -d
```

## Security Notes

- Never commit actual secrets to version control
- Use strong, randomly generated passwords
- Rotate secrets regularly
- Consider using external secret management systems in production (e.g., HashiCorp Vault, AWS Secrets Manager)