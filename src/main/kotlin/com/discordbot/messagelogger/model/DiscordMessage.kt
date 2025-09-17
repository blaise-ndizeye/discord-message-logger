package com.discordbot.messagelogger.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "messages")
data class DiscordMessage(
    @Id
    val id: String? = null,
    
    val messageId: String,
    val channelId: String,
    val channelName: String,
    val guildId: String?,
    val guildName: String?,
    val authorId: String,
    val authorName: String,
    val authorDiscriminator: String,
    val content: String,
    val timestamp: LocalDateTime,
    val editedTimestamp: LocalDateTime? = null,
    val attachments: List<MessageAttachment> = emptyList(),
    val embeds: List<MessageEmbed> = emptyList(),
    val reactions: List<MessageReaction> = emptyList(),
    val isBot: Boolean = false,
    val messageType: String = "DEFAULT"
)

data class MessageAttachment(
    val id: String,
    val filename: String,
    val url: String,
    val proxyUrl: String,
    val size: Long,
    val contentType: String?
)

data class MessageEmbed(
    val title: String?,
    val description: String?,
    val url: String?,
    val color: Int?,
    val timestamp: LocalDateTime?,
    val footerText: String?,
    val authorName: String?,
    val fields: List<EmbedField> = emptyList()
)

data class EmbedField(
    val name: String,
    val value: String,
    val inline: Boolean
)

data class MessageReaction(
    val emoji: String,
    val count: Int,
    val users: List<String> = emptyList()
)