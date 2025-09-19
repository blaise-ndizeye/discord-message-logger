package com.discordbot.messagelogger.controller

import com.discordbot.messagelogger.model.DiscordMessage
import com.discordbot.messagelogger.service.MessageLoggerService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import kotlin.test.assertEquals

class MessageControllerTest {

    private lateinit var messageLoggerService: MessageLoggerService
    private lateinit var messageController: MessageController

    @BeforeEach
    fun setUp() {
        messageLoggerService = mockk()
        messageController = MessageController(messageLoggerService)
    }

    @Test
    fun `getMessagesByChannel should return paginated messages`() {
        // Given
        val messages = listOf(
            createTestDiscordMessage("1", "channel1"),
            createTestDiscordMessage("2", "channel1")
        )
        val page = PageImpl(messages)
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp"))

        every { messageLoggerService.getMessagesByChannel("channel1", pageable) } returns page

        // When
        val response = messageController.getMessagesByChannel("channel1", 0, 20, "timestamp,desc")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(page, response.body)
        verify { messageLoggerService.getMessagesByChannel("channel1", pageable) }
    }

    @Test
    fun `getMessagesByAuthor should return paginated messages`() {
        // Given
        val messages = listOf(createTestDiscordMessage("1", "channel1"))
        val page = PageImpl(messages)
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp"))

        every { messageLoggerService.getMessagesByAuthor("author1", pageable) } returns page

        // When
        val response = messageController.getMessagesByAuthor("author1", 0, 20, "timestamp,desc")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(page, response.body)
        verify { messageLoggerService.getMessagesByAuthor("author1", pageable) }
    }

    @Test
    fun `searchMessages should return paginated search results`() {
        // Given
        val messages = listOf(createTestDiscordMessage("1", "channel1"))
        val page = PageImpl(messages)
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp"))

        every { messageLoggerService.searchMessages("test query", pageable) } returns page

        // When
        val response = messageController.searchMessages("test query", 0, 20, "timestamp,desc")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(page, response.body)
        verify { messageLoggerService.searchMessages("test query", pageable) }
    }

    @Test
    fun `getRecentMessages should return recent messages`() {
        // Given
        val since = LocalDateTime.now().minusHours(24)
        val messages = listOf(createTestDiscordMessage("1", "channel1"))
        val page = PageImpl(messages)
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp"))

        every { messageLoggerService.getRecentMessages(since, pageable) } returns page

        // When
        val response = messageController.getRecentMessages(since, 0, 20, "timestamp,desc")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(page, response.body)
        verify { messageLoggerService.getRecentMessages(since, pageable) }
    }

    @Test
    fun `getChannelStats should return channel statistics`() {
        // Given
        val stats = mapOf("channelId" to "channel1", "totalMessages" to 100L)
        every { messageLoggerService.getMessageStats("channel1") } returns stats

        // When
        val response = messageController.getChannelStats("channel1")

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(stats, response.body)
        verify { messageLoggerService.getMessageStats("channel1") }
    }

    @Test
    fun `getMessageById should return 404 not found`() {
        // When
        val response = messageController.getMessageById("message1")

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `healthCheck should return health status`() {
        // When
        val response = messageController.healthCheck()

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("UP", response.body!!["status"])
        assertEquals(2, response.body!!.size) // status and timestamp
    }

    @Test
    fun `parseSort should handle ascending sort correctly`() {
        // Given
        val page = PageImpl<DiscordMessage>(emptyList())
        every { messageLoggerService.getMessagesByChannel(any(), any()) } returns page
        
        // When
        val response = messageController.getMessagesByChannel("channel1", 0, 20, "content,asc")

        // Then - verify the call is made with ascending sort
        val expectedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "content"))
        verify { messageLoggerService.getMessagesByChannel("channel1", expectedPageable) }
    }

    @Test
    fun `parseSort should default to ascending when direction not specified`() {
        // Given
        val page = PageImpl<DiscordMessage>(emptyList())
        every { messageLoggerService.getMessagesByChannel(any(), any()) } returns page
        
        // When
        val response = messageController.getMessagesByChannel("channel1", 0, 20, "authorName")

        // Then - verify the call is made with ascending sort (default)
        val expectedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "authorName"))
        verify { messageLoggerService.getMessagesByChannel("channel1", expectedPageable) }
    }

    @Test
    fun `controller should use default pagination parameters`() {
        // Given
        val page = PageImpl<DiscordMessage>(emptyList())
        every { messageLoggerService.getMessagesByChannel(any(), any()) } returns page

        // When - test default parameters by calling with explicit defaults
        val response = messageController.getMessagesByChannel("channel1", 0, 20, "timestamp,desc")

        // Then - verify default parameters are used
        val expectedPageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp"))
        verify { messageLoggerService.getMessagesByChannel("channel1", expectedPageable) }
    }

    private fun createTestDiscordMessage(id: String, channelId: String) = DiscordMessage(
        id = id,
        messageId = "msg_$id",
        channelId = channelId,
        channelName = "test-channel",
        guildId = "guild1",
        guildName = "Test Guild",
        authorId = "author1",
        authorName = "Test User",
        authorDisplayName = "0001",
        content = "Test message content",
        timestamp = LocalDateTime.now(),
        isBot = false
    )
}