package com.discordbot.messagelogger.service

import com.discordbot.messagelogger.model.DiscordMessage
import com.discordbot.messagelogger.repository.DiscordMessageRepository
import io.mockk.*
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MessageLoggerServiceTest {

    private lateinit var messageRepository: DiscordMessageRepository
    private lateinit var messageLoggerService: MessageLoggerService

    private lateinit var mockMessage: Message
    private lateinit var mockUser: User
    private lateinit var mockChannel: TextChannel
    private lateinit var mockGuild: Guild

    @BeforeEach
    fun setUp() {
        messageRepository = mockk()
        messageLoggerService = MessageLoggerService(messageRepository)

        // Mock JDA objects
        mockUser = mockk()
        mockChannel = mockk()
        mockGuild = mockk()
        mockMessage = mockk()

        // Setup common mock behaviors
        every { mockUser.id } returns "123456789"
        every { mockUser.name } returns "TestUser"
        every { mockUser.discriminator } returns "0001"
        every { mockUser.isBot } returns false

        every { mockChannel.id } returns "987654321"
        every { mockChannel.name } returns "test-channel"

        every { mockGuild.id } returns "111222333"
        every { mockGuild.name } returns "Test Guild"

        every { mockMessage.id } returns "message123"
        every { mockMessage.author } returns mockUser
        every { mockMessage.channel } returns mockChannel
        every { mockMessage.guild } returns mockGuild
        every { mockMessage.contentRaw } returns "Test message content"
        every { mockMessage.timeCreated } returns OffsetDateTime.now(ZoneOffset.UTC)
        every { mockMessage.timeEdited } returns null
        every { mockMessage.attachments } returns emptyList()
        every { mockMessage.embeds } returns emptyList()
        every { mockMessage.reactions } returns emptyList()
        every { mockMessage.type } returns MessageType.DEFAULT
    }

    @Test
    fun `logMessage should save new message successfully`() {
        // Given
        val expectedMessage = slot<DiscordMessage>()
        every { messageRepository.findByMessageId("message123") } returns null
        every { messageRepository.save(capture(expectedMessage)) } returns mockk()

        // When
        val result = messageLoggerService.logMessage(mockMessage)

        // Then
        assertNotNull(result)
        verify { messageRepository.save(any()) }
        
        val capturedMessage = expectedMessage.captured
        assertEquals("message123", capturedMessage.messageId)
        assertEquals("987654321", capturedMessage.channelId)
        assertEquals("test-channel", capturedMessage.channelName)
        assertEquals("123456789", capturedMessage.authorId)
        assertEquals("TestUser", capturedMessage.authorName)
        assertEquals("Test message content", capturedMessage.content)
        assertFalse(capturedMessage.isBot)
    }

    @Test
    fun `logMessage should not save duplicate message`() {
        // Given
        val existingMessage = mockk<DiscordMessage>()
        every { messageRepository.findByMessageId("message123") } returns existingMessage

        // When
        val result = messageLoggerService.logMessage(mockMessage)

        // Then
        assertEquals(existingMessage, result)
        verify(exactly = 0) { messageRepository.save(any()) }
    }

    @Test
    fun `logMessage should handle exception and return null`() {
        // Given
        every { messageRepository.findByMessageId("message123") } throws RuntimeException("Database error")

        // When
        val result = messageLoggerService.logMessage(mockMessage)

        // Then
        assertNull(result)
        verify(exactly = 0) { messageRepository.save(any()) }
    }

    @Test
    fun `updateMessage should update existing message`() {
        // Given
        val existingMessage = DiscordMessage(
            id = "1",
            messageId = "message123",
            channelId = "987654321",
            channelName = "test-channel",
            guildId = "111222333",
            guildName = "Test Guild",
            authorId = "123456789",
            authorName = "TestUser",
            authorDiscriminator = "0001",
            content = "Old content",
            timestamp = LocalDateTime.now(),
            isBot = false
        )

        val updatedMessage = existingMessage.copy(content = "Test message content")

        every { messageRepository.findByMessageId("message123") } returns existingMessage
        every { messageRepository.save(any()) } returns updatedMessage

        // When
        val result = messageLoggerService.updateMessage(mockMessage)

        // Then
        assertNotNull(result)
        verify { messageRepository.save(match { it.content == "Test message content" }) }
    }

    @Test
    fun `updateMessage should create new message if not found`() {
        // Given
        every { messageRepository.findByMessageId("message123") } returns null
        every { messageRepository.save(any()) } returns mockk()

        // When
        val result = messageLoggerService.updateMessage(mockMessage)

        // Then
        assertNotNull(result)
        verify { messageRepository.save(any()) }
    }

    @Test
    fun `deleteMessage should delete existing message`() {
        // Given
        val existingMessage = mockk<DiscordMessage>()
        every { messageRepository.findByMessageId("message123") } returns existingMessage
        every { messageRepository.delete(existingMessage) } just Runs

        // When
        val result = messageLoggerService.deleteMessage("message123")

        // Then
        assertTrue(result)
        verify { messageRepository.delete(existingMessage) }
    }

    @Test
    fun `deleteMessage should return false for non-existent message`() {
        // Given
        every { messageRepository.findByMessageId("message123") } returns null

        // When
        val result = messageLoggerService.deleteMessage("message123")

        // Then
        assertFalse(result)
        verify(exactly = 0) { messageRepository.delete(any()) }
    }

    @Test
    fun `getMessagesByChannel should return paginated results`() {
        // Given
        val messages = listOf(mockk<DiscordMessage>(), mockk<DiscordMessage>())
        val page = PageImpl(messages)
        val pageable = PageRequest.of(0, 10)

        every { messageRepository.findByChannelId("channelId", pageable) } returns page

        // When
        val result = messageLoggerService.getMessagesByChannel("channelId", pageable)

        // Then
        assertEquals(page, result)
        verify { messageRepository.findByChannelId("channelId", pageable) }
    }

    @Test
    fun `getMessagesByAuthor should return paginated results`() {
        // Given
        val messages = listOf(mockk<DiscordMessage>())
        val page = PageImpl(messages)
        val pageable = PageRequest.of(0, 10)

        every { messageRepository.findByAuthorId("authorId", pageable) } returns page

        // When
        val result = messageLoggerService.getMessagesByAuthor("authorId", pageable)

        // Then
        assertEquals(page, result)
        verify { messageRepository.findByAuthorId("authorId", pageable) }
    }

    @Test
    fun `searchMessages should return paginated results`() {
        // Given
        val messages = listOf(mockk<DiscordMessage>())
        val page = PageImpl(messages)
        val pageable = PageRequest.of(0, 10)

        every { messageRepository.findByContentContainingIgnoreCase("query", pageable) } returns page

        // When
        val result = messageLoggerService.searchMessages("query", pageable)

        // Then
        assertEquals(page, result)
        verify { messageRepository.findByContentContainingIgnoreCase("query", pageable) }
    }

    @Test
    fun `getRecentMessages should return paginated results`() {
        // Given
        val since = LocalDateTime.now().minusDays(1)
        val messages = listOf(mockk<DiscordMessage>())
        val page = PageImpl(messages)
        val pageable = PageRequest.of(0, 10)

        every { messageRepository.findRecentMessages(since, pageable) } returns page

        // When
        val result = messageLoggerService.getRecentMessages(since, pageable)

        // Then
        assertEquals(page, result)
        verify { messageRepository.findRecentMessages(since, pageable) }
    }

    @Test
    fun `getMessageStats should return channel statistics`() {
        // Given
        every { messageRepository.countByChannelId("channelId") } returns 42L

        // When
        val result = messageLoggerService.getMessageStats("channelId")

        // Then
        assertEquals("channelId", result["channelId"])
        assertEquals(42L, result["totalMessages"])
        verify { messageRepository.countByChannelId("channelId") }
    }
}