package com.sarahisweird.uwubot

import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.Icon
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.schlaubi.lavakord.LavaKord
import dev.schlaubi.lavakord.kord.lavakord
import me.jakejmattson.discordkt.dsl.bot
import kotlin.system.exitProcess

lateinit var lavalink: LavaKord
lateinit var botIcon: Icon

@OptIn(KordPreview::class, PrivilegedIntent::class)
fun main() {
    val token = System.getenv("uwubot_token")

    bot(token) {
        val config = data("./config.json") {
            Configuration(listOf())
        }

        if (config.nodes.isEmpty()) {
            println("Lavalink node list can't be empty!")
            exitProcess(1)
        }

        prefix { "uwu!" }

        configure {
            intents = Intents.all
            permissions = Permissions
        }

        onStart {
            lavalink = kord.lavakord {
                link {
                    autoReconnect = false
                }
            }

            for (node in config.nodes) {
                lavalink.addNode(node.uri, node.password)
            }

            botIcon = kord.getSelf().avatar!!
        }
    }
}