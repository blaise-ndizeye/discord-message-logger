package com.discordbot.messagelogger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DiscordMessageLoggerApplication

fun main(args: Array<String>) {
	runApplication<DiscordMessageLoggerApplication>(*args)
}