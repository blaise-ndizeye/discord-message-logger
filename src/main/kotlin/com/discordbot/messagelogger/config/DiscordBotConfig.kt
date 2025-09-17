package com.discordbot.messagelogger.config

import com.discordbot.messagelogger.listener.MessageListener
import com.discordbot.messagelogger.service.CommandHandlerService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PreDestroy

@Configuration
class DiscordBotConfig(
    @Value("\${discord.bot.token}")
    private val botToken: String,
    private val messageListener: MessageListener,
    private val commandHandlerService: CommandHandlerService
) {
    
    private val logger = LoggerFactory.getLogger(DiscordBotConfig::class.java)
    private var jda: JDA? = null

    @Bean
    fun jda(): JDA {
        logger.info("Initializing Discord bot...")
        
        jda = JDABuilder.createDefault(botToken)
            .enableIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.DIRECT_MESSAGES
            )
            .addEventListeners(messageListener, commandHandlerService)
            .build()
            
        // Wait for JDA to be ready then register commands
        jda!!.awaitReady()
        
        // Register slash commands
        jda!!.updateCommands().addCommands(
            Commands.slash("stats", "Get statistics for the current channel"),
            Commands.slash("search", "Search for messages containing specific text")
                .addOption(OptionType.STRING, "query", "Search query", true),
            Commands.slash("recent", "Get recent messages")
                .addOption(OptionType.INTEGER, "hours", "Hours back to search (default: 24)", false),
            Commands.slash("user-messages", "Get messages from a specific user")
                .addOption(OptionType.USER, "user", "User to search for", true)
        ).queue()
        
        logger.info("Discord bot initialized successfully with slash commands")
        return jda!!
    }

    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down Discord bot...")
        jda?.shutdown()
    }
}