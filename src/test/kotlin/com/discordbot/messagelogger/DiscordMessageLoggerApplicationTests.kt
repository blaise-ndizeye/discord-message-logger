package com.discordbot.messagelogger

import com.discordbot.messagelogger.config.DiscordBotConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import net.dv8tion.jda.api.JDA

@SpringBootTest
class DiscordMessageLoggerApplicationTests {

	@MockBean
	private lateinit var jda: JDA

	@Test
	fun contextLoads() {
		// This test verifies that the Spring application context loads successfully
		// JDA is mocked to avoid requiring a real Discord bot token
	}

}
