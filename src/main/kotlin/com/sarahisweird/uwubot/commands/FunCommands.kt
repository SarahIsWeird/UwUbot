package com.sarahisweird.uwubot.commands

import com.sarahisweird.uwubot.repeat
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.commands.commands
import kotlin.random.Random

private val random = Random(System.currentTimeMillis())

private suspend fun gayFunc(it: GuildSlashCommandEvent<NoArgs>) {
    it.apply { respond("**${getMember()?.nickname ?: author.username}** is ${random.nextInt(101)}% gay.", false) }
}

fun funCommands() = commands("Fun") {

    slash("waifu") {
        description = "Find out if you're a good waifu!"

        execute {
            respond("**${getMember()?.nickname ?: author.username}** is a ${random.nextInt(100) + 1}/100 waifu.", false)
        }
    }

    slash("gayrate") {
        description = "Find out how gay you are!"

        execute(::gayFunc)
    }

    slash("gay") {
        description = "Find out how gay you are!"

        execute(::gayFunc)
    }

    slash("dick") {
        description = "Find out how big your dick is!"

        execute {
            val size = random.nextInt(17)

            if (size == 0) {
                respond("```<```", false)
                return@execute
            }

            respond("8${'='.repeat(size - 1)}D", false)
        }
    }
}