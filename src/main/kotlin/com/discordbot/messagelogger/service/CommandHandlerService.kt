package com.discordbot.messagelogger.service

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class CommandHandlerService(
    private val messageLoggerService: MessageLoggerService
) : ListenerAdapter() {
    
    private val logger = LoggerFactory.getLogger(CommandHandlerService::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        try {
            when (event.name) {
                "stats" -> handleStatsCommand(event)
                "search" -> handleSearchCommand(event)
                "recent" -> handleRecentCommand(event)
                "user-messages" -> handleUserMessagesCommand(event)
                else -> {
                    event.reply("Unknown command: ${event.name}").setEphemeral(true).queue()
                }
            }
        } catch (e: Exception) {
            logger.error("Error handling slash command ${event.name}: ${e.message}", e)
            event.reply("An error occurred while processing your command.").setEphemeral(true).queue()
        }
    }

    private fun handleStatsCommand(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()
        
        val channelId = event.channel.id
        val stats = messageLoggerService.getMessageStats(channelId)
        
        val response = buildString {
            appendLine("📊 **Channel Statistics**")
            appendLine("Channel: <#$channelId>")
            appendLine("Total Messages: ${stats["totalMessages"]}")
        }
        
        event.hook.editOriginal(response).queue()
    }

    private fun handleSearchCommand(event: SlashCommandInteractionEvent) {
        val query = event.getOption("query")?.asString
        if (query == null) {
            event.reply("Please provide a search query.").setEphemeral(true).queue()
            return
        }
        
        event.deferReply().queue()
        
        val pageable = PageRequest.of(0, 5) // Limit to 5 results for Discord
        val results = messageLoggerService.searchMessages(query, pageable)
        
        if (results.isEmpty) {
            event.hook.editOriginal("No messages found containing \"$query\".").queue()
            return
        }
        
        val response = buildString {
            appendLine("🔍 **Search Results for \"$query\"**")
            appendLine("Found ${results.totalElements} total matches (showing first 5):")
            appendLine()
            
            results.content.forEach { message ->
                val timestamp = message.timestamp.format(dateFormatter)
                val content = if (message.content.length > 100) {
                    message.content.take(100) + "..."
                } else {
                    message.content
                }
                appendLine("**${message.authorName}** ($timestamp):")
                appendLine(content)
                appendLine()
            }
        }
        
        event.hook.editOriginal(response).queue()
    }

    private fun handleRecentCommand(event: SlashCommandInteractionEvent) {
        val hoursBack = event.getOption("hours")?.asLong ?: 24L
        
        event.deferReply().queue()
        
        val since = LocalDateTime.now().minusHours(hoursBack)
        val pageable = PageRequest.of(0, 10)
        val results = messageLoggerService.getRecentMessages(since, pageable)
        
        if (results.isEmpty) {
            event.hook.editOriginal("No messages found in the last $hoursBack hours.").queue()
            return
        }
        
        val response = buildString {
            appendLine("⏰ **Recent Messages (Last $hoursBack hours)**")
            appendLine("Found ${results.totalElements} total messages (showing first 10):")
            appendLine()
            
            results.content.forEach { message ->
                val timestamp = message.timestamp.format(dateFormatter)
                val content = if (message.content.length > 80) {
                    message.content.take(80) + "..."
                } else {
                    message.content
                }
                appendLine("**${message.authorName}** in <#${message.channelId}> ($timestamp):")
                appendLine(content)
                appendLine()
            }
        }
        
        event.hook.editOriginal(response).queue()
    }

    private fun handleUserMessagesCommand(event: SlashCommandInteractionEvent) {
        val userId = event.getOption("user")?.asUser?.id
        if (userId == null) {
            event.reply("Please specify a user.").setEphemeral(true).queue()
            return
        }
        
        event.deferReply().queue()
        
        val pageable = PageRequest.of(0, 5)
        val results = messageLoggerService.getMessagesByAuthor(userId, pageable)
        
        if (results.isEmpty) {
            event.hook.editOriginal("No messages found for this user in this channel.").queue()
            return
        }
        
        val response = buildString {
            appendLine("👤 **Messages by <@$userId>**")
            appendLine("Found ${results.totalElements} total messages (showing first 5):")
            appendLine()
            
            results.content.forEach { message ->
                val timestamp = message.timestamp.format(dateFormatter)
                val content = if (message.content.length > 100) {
                    message.content.take(100) + "..."
                } else {
                    message.content
                }
                appendLine("**${message.channelName}** ($timestamp):")
                appendLine(content)
                appendLine()
            }
        }
        
        event.hook.editOriginal(response).queue()
    }
}