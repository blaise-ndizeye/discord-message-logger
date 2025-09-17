package com.discordbot.messagelogger.repository

import com.discordbot.messagelogger.model.DiscordMessage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface DiscordMessageRepository : MongoRepository<DiscordMessage, String> {
    
    // Find messages by Discord message ID
    fun findByMessageId(messageId: String): DiscordMessage?
    
    // Find messages by channel ID
    fun findByChannelId(channelId: String, pageable: Pageable): Page<DiscordMessage>
    
    // Find messages by guild ID
    fun findByGuildId(guildId: String, pageable: Pageable): Page<DiscordMessage>
    
    // Find messages by author ID
    fun findByAuthorId(authorId: String, pageable: Pageable): Page<DiscordMessage>
    
    // Find messages by content containing text
    fun findByContentContainingIgnoreCase(content: String, pageable: Pageable): Page<DiscordMessage>
    
    // Find messages within a time range
    fun findByTimestampBetween(start: LocalDateTime, end: LocalDateTime, pageable: Pageable): Page<DiscordMessage>
    
    // Find messages by channel and time range
    fun findByChannelIdAndTimestampBetween(
        channelId: String, 
        start: LocalDateTime, 
        end: LocalDateTime, 
        pageable: Pageable
    ): Page<DiscordMessage>
    
    // Find messages by author and channel
    fun findByAuthorIdAndChannelId(authorId: String, channelId: String, pageable: Pageable): Page<DiscordMessage>
    
    // Count messages by channel
    fun countByChannelId(channelId: String): Long
    
    // Count messages by author
    fun countByAuthorId(authorId: String): Long
    
    // Custom query to find recent messages
    @Query("{ 'timestamp': { '\$gte': ?0 } }")
    fun findRecentMessages(since: LocalDateTime, pageable: Pageable): Page<DiscordMessage>
    
    // Custom query to find messages with attachments
    @Query("{ 'attachments': { '\$ne': [] } }")
    fun findMessagesWithAttachments(pageable: Pageable): Page<DiscordMessage>
    
    // Custom query to find bot messages
    fun findByIsBot(isBot: Boolean, pageable: Pageable): Page<DiscordMessage>
}