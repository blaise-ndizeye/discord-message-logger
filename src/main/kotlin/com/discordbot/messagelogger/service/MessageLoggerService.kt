package com.discordbot.messagelogger.service

import com.discordbot.messagelogger.model.*
import com.discordbot.messagelogger.repository.DiscordMessageRepository
import net.dv8tion.jda.api.entities.Message
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class MessageLoggerService(
    private val messageRepository: DiscordMessageRepository
) {
    
    private val logger = LoggerFactory.getLogger(MessageLoggerService::class.java)

    fun logMessage(jdaMessage: Message): DiscordMessage? {
        return try {
            val discordMessage = convertJdaMessage(jdaMessage)
            
            // Check if message already exists to avoid duplicates
            val existingMessage = messageRepository.findByMessageId(jdaMessage.id)
            if (existingMessage != null) {
                logger.debug("Message ${jdaMessage.id} already exists, skipping...")
                return existingMessage
            }
            
            val savedMessage = messageRepository.save(discordMessage)
            logger.info("Saved message from ${jdaMessage.author.name} in channel ${jdaMessage.channel.name}")
            savedMessage
        } catch (e: Exception) {
            logger.error("Error logging message ${jdaMessage.id}: ${e.message}", e)
            null
        }
    }

    fun updateMessage(jdaMessage: Message): DiscordMessage? {
        return try {
            val existingMessage = messageRepository.findByMessageId(jdaMessage.id)
            if (existingMessage == null) {
                logger.warn("Attempted to update non-existent message ${jdaMessage.id}")
                return logMessage(jdaMessage)
            }
            
            val updatedMessage = existingMessage.copy(
                content = jdaMessage.contentRaw,
                editedTimestamp = LocalDateTime.now(),
                attachments = convertAttachments(jdaMessage),
                embeds = convertEmbeds(jdaMessage)
            )
            
            val savedMessage = messageRepository.save(updatedMessage)
            logger.info("Updated message ${jdaMessage.id} from ${jdaMessage.author.name}")
            savedMessage
        } catch (e: Exception) {
            logger.error("Error updating message ${jdaMessage.id}: ${e.message}", e)
            null
        }
    }

    fun deleteMessage(messageId: String): Boolean {
        return try {
            val existingMessage = messageRepository.findByMessageId(messageId)
            if (existingMessage != null) {
                messageRepository.delete(existingMessage)
                logger.info("Deleted message $messageId")
                true
            } else {
                logger.warn("Attempted to delete non-existent message $messageId")
                false
            }
        } catch (e: Exception) {
            logger.error("Error deleting message $messageId: ${e.message}", e)
            false
        }
    }

    // Query methods
    fun getMessagesByChannel(channelId: String, pageable: Pageable): Page<DiscordMessage> {
        return messageRepository.findByChannelId(channelId, pageable)
    }

    fun getMessagesByAuthor(authorId: String, pageable: Pageable): Page<DiscordMessage> {
        return messageRepository.findByAuthorId(authorId, pageable)
    }

    fun searchMessages(query: String, pageable: Pageable): Page<DiscordMessage> {
        return messageRepository.findByContentContainingIgnoreCase(query, pageable)
    }

    fun getRecentMessages(since: LocalDateTime, pageable: Pageable): Page<DiscordMessage> {
        return messageRepository.findRecentMessages(since, pageable)
    }

    fun getMessageStats(channelId: String): Map<String, Any> {
        val messageCount = messageRepository.countByChannelId(channelId)
        return mapOf(
            "channelId" to channelId,
            "totalMessages" to messageCount
        )
    }

    private fun convertJdaMessage(jdaMessage: Message): DiscordMessage {
        return DiscordMessage(
            messageId = jdaMessage.id,
            channelId = jdaMessage.channel.id,
            channelName = jdaMessage.channel.name,
            guildId = jdaMessage.guild?.id,
            guildName = jdaMessage.guild?.name,
            authorId = jdaMessage.author.id,
            authorName = jdaMessage.author.name,
            authorDiscriminator = jdaMessage.author.discriminator,
            content = jdaMessage.contentRaw,
            timestamp = jdaMessage.timeCreated.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
            editedTimestamp = jdaMessage.timeEdited?.atZoneSameInstant(ZoneId.systemDefault())?.toLocalDateTime(),
            attachments = convertAttachments(jdaMessage),
            embeds = convertEmbeds(jdaMessage),
            reactions = convertReactions(jdaMessage),
            isBot = jdaMessage.author.isBot,
            messageType = jdaMessage.type.name
        )
    }

    private fun convertAttachments(jdaMessage: Message): List<MessageAttachment> {
        return jdaMessage.attachments.map { attachment ->
            MessageAttachment(
                id = attachment.id,
                filename = attachment.fileName,
                url = attachment.url,
                proxyUrl = attachment.proxyUrl,
                size = attachment.size,
                contentType = attachment.contentType
            )
        }
    }

    private fun convertEmbeds(jdaMessage: Message): List<MessageEmbed> {
        return jdaMessage.embeds.map { embed ->
            MessageEmbed(
                title = embed.title,
                description = embed.description,
                url = embed.url,
                color = embed.colorRaw,
                timestamp = embed.timestamp?.atZoneSameInstant(ZoneId.systemDefault())?.toLocalDateTime(),
                footerText = embed.footer?.text,
                authorName = embed.author?.name,
                fields = embed.fields.map { field ->
                    EmbedField(
                        name = field.name ?: "",
                        value = field.value ?: "",
                        inline = field.isInline
                    )
                }
            )
        }
    }

    private fun convertReactions(jdaMessage: Message): List<MessageReaction> {
        return jdaMessage.reactions.map { reaction ->
            MessageReaction(
                emoji = reaction.emoji.name,
                count = reaction.count
            )
        }
    }
}