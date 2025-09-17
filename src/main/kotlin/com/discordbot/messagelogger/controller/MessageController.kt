package com.discordbot.messagelogger.controller

import com.discordbot.messagelogger.model.DiscordMessage
import com.discordbot.messagelogger.service.MessageLoggerService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = ["*"]) // Configure appropriately for production
class MessageController(
    private val messageLoggerService: MessageLoggerService
) {

    @GetMapping("/channel/{channelId}")
    fun getMessagesByChannel(
        @PathVariable channelId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "timestamp,desc") sort: String
    ): ResponseEntity<Page<DiscordMessage>> {
        val sortOrder = parseSort(sort)
        val pageable = PageRequest.of(page, size, sortOrder)
        val messages = messageLoggerService.getMessagesByChannel(channelId, pageable)
        return ResponseEntity.ok(messages)
    }

    @GetMapping("/author/{authorId}")
    fun getMessagesByAuthor(
        @PathVariable authorId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "timestamp,desc") sort: String
    ): ResponseEntity<Page<DiscordMessage>> {
        val sortOrder = parseSort(sort)
        val pageable = PageRequest.of(page, size, sortOrder)
        val messages = messageLoggerService.getMessagesByAuthor(authorId, pageable)
        return ResponseEntity.ok(messages)
    }

    @GetMapping("/search")
    fun searchMessages(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "timestamp,desc") sort: String
    ): ResponseEntity<Page<DiscordMessage>> {
        val sortOrder = parseSort(sort)
        val pageable = PageRequest.of(page, size, sortOrder)
        val messages = messageLoggerService.searchMessages(query, pageable)
        return ResponseEntity.ok(messages)
    }

    @GetMapping("/recent")
    fun getRecentMessages(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) since: LocalDateTime,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "timestamp,desc") sort: String
    ): ResponseEntity<Page<DiscordMessage>> {
        val sortOrder = parseSort(sort)
        val pageable = PageRequest.of(page, size, sortOrder)
        val messages = messageLoggerService.getRecentMessages(since, pageable)
        return ResponseEntity.ok(messages)
    }

    @GetMapping("/stats/channel/{channelId}")
    fun getChannelStats(@PathVariable channelId: String): ResponseEntity<Map<String, Any>> {
        val stats = messageLoggerService.getMessageStats(channelId)
        return ResponseEntity.ok(stats)
    }

    @GetMapping("/{messageId}")
    fun getMessageById(@PathVariable messageId: String): ResponseEntity<DiscordMessage> {
        // This would need to be implemented in the service
        return ResponseEntity.notFound().build()
    }

    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "UP",
                "timestamp" to LocalDateTime.now().toString()
            )
        )
    }

    private fun parseSort(sortString: String): Sort {
        val parts = sortString.split(",")
        val property = parts[0]
        val direction = if (parts.size > 1 && parts[1].lowercase() == "desc") {
            Sort.Direction.DESC
        } else {
            Sort.Direction.ASC
        }
        return Sort.by(direction, property)
    }
}

// Data classes for API responses
data class ApiResponse<T>(
    val data: T,
    val message: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)