package com.discordbot.messagelogger.listener

import com.discordbot.messagelogger.service.MessageLoggerService
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MessageListener(
    private val messageLoggerService: MessageLoggerService
) : ListenerAdapter() {
    
    private val logger = LoggerFactory.getLogger(MessageListener::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        try {
            // Skip webhook messages and system messages that might cause issues
            if (event.isWebhookMessage || event.message.type.isSystem) {
                return
            }

            logger.debug("Received message from ${event.author.name} in ${event.channel.name}")
            messageLoggerService.logMessage(event.message)
        } catch (e: Exception) {
            logger.error("Error processing message received event: ${e.message}", e)
        }
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        try {
            logger.debug("Message updated by ${event.author.name} in ${event.channel.name}")
            messageLoggerService.updateMessage(event.message)
        } catch (e: Exception) {
            logger.error("Error processing message update event: ${e.message}", e)
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        try {
            logger.debug("Message deleted in ${event.channel.name}: ${event.messageId}")
            messageLoggerService.deleteMessage(event.messageId)
        } catch (e: Exception) {
            logger.error("Error processing message delete event: ${e.message}", e)
        }
    }
}