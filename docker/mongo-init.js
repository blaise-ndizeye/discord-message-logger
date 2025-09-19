// MongoDB initialization script for Discord Message Logger

db = db.getSiblingDB('discord-messages');

// Create collections with validation
db.createCollection('messages', {
   validator: {
      $jsonSchema: {
         bsonType: 'object',
         required: ['messageId', 'channelId', 'authorId', 'content', 'timestamp'],
         properties: {
            messageId: {
               bsonType: 'string',
               description: 'Discord message ID must be a string and is required'
            },
            channelId: {
               bsonType: 'string',
               description: 'Discord channel ID must be a string and is required'
            },
            authorId: {
               bsonType: 'string',
               description: 'Discord author ID must be a string and is required'
            },
            content: {
               bsonType: 'string',
               description: 'Message content must be a string and is required'
            },
            timestamp: {
               bsonType: 'date',
               description: 'Message timestamp must be a date and is required'
            }
         }
      }
   }
});

// Create indexes for better query performance
db.messages.createIndex({ 'messageId': 1 }, { unique: true });
db.messages.createIndex({ 'channelId': 1 });
db.messages.createIndex({ 'authorId': 1 });
db.messages.createIndex({ 'timestamp': -1 });
db.messages.createIndex({ 'guildId': 1 });
db.messages.createIndex({ 'content': 'text' }); // Text index for search

// Compound indexes for common queries
db.messages.createIndex({ 'channelId': 1, 'timestamp': -1 });
db.messages.createIndex({ 'authorId': 1, 'timestamp': -1 });
db.messages.createIndex({ 'guildId': 1, 'timestamp': -1 });

console.log('MongoDB initialization completed for discord-messages database');
console.log('Created messages collection with validation and indexes');