package com.sarahisweird.uwubot.commands

import com.sarahisweird.uwubot.botIcon
import com.sarahisweird.uwubot.lavalink
import com.sarahisweird.uwubot.toTimeString
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.EmbedBuilder
import dev.schlaubi.lavakord.audio.*
import dev.schlaubi.lavakord.audio.player.Track
import dev.schlaubi.lavakord.rest.TrackResponse
import dev.schlaubi.lavakord.rest.loadItem
import me.jakejmattson.discordkt.NoArgs
import me.jakejmattson.discordkt.TypeContainer
import me.jakejmattson.discordkt.arguments.EveryArg
import me.jakejmattson.discordkt.arguments.IntegerArg
import me.jakejmattson.discordkt.arguments.MessageArg
import me.jakejmattson.discordkt.commands.GuildSlashCommand
import me.jakejmattson.discordkt.commands.GuildSlashCommandEvent
import me.jakejmattson.discordkt.commands.commands
import me.jakejmattson.discordkt.extensions.addField

private val queues = mutableMapOf<Snowflake, MutableList<Track>>()

private val <T : TypeContainer> GuildSlashCommandEvent<T>.link: Link
    get() = lavalink.getLink(guild.id.value)

suspend fun <T : TypeContainer> GuildSlashCommandEvent<T>.joinIfNotConnected(): Boolean {
    val vcId = author.asMember(guild.id).getVoiceState().channelId ?: return false

    if (link.state == Link.State.CONNECTED) return true

    link.connectAudio(vcId.value)
    queues[guild.id] = mutableListOf()

    link.player.on<TrackEvent, TrackEndEvent> {
        maybePlay()
    }

    link.player.on<TrackEvent, TrackExceptionEvent> {
        maybePlay()
    }

    link.player.on<TrackEvent, TrackStuckEvent> {
        maybePlay()
    }

    return true
}

private fun EmbedBuilder.renderTrackEmbed(track: Track) {
    author {
        icon = botIcon.url
        name = "UwUbot"
    }

    title = "Added ${track.title} to the queue."
    description = track.author
    url = track.uri
}

private suspend fun <T : TypeContainer> GuildSlashCommandEvent<T>.maybePlay() {
    if (link.player.playingTrack != null) return
    val track = queues[guild.id]!!.removeFirstOrNull() ?: return
    link.player.playTrack(track)
}

private suspend fun <T : TypeContainer> GuildSlashCommandEvent<T>.play(query: String) {
    val item = link.loadItem(query)

    when (item.loadType) {
        TrackResponse.LoadType.TRACK_LOADED -> {
            queues[guild.id]!!.add(item.tracks.first().toTrack())

            respond(false) {
                renderTrackEmbed(item.track.toTrack())
            }

            maybePlay()
        }

        TrackResponse.LoadType.PLAYLIST_LOADED -> {
            item.tracks.forEach { queues[guild.id]!!.add(it.toTrack()) }

            respond("Queued ${item.tracks.size} tracks!", false)

            maybePlay()
        }

        TrackResponse.LoadType.SEARCH_RESULT -> {
            queues[guild.id]!!.add(item.tracks.first().toTrack())

            respond(false) {
                renderTrackEmbed(item.tracks.first().toTrack())
            }

            maybePlay()
        }

        TrackResponse.LoadType.NO_MATCHES -> {
            respond("Es konnten keine Uebereinstimmungen gefunden werden. :(", false)
        }

        TrackResponse.LoadType.LOAD_FAILED -> {
            respond("Es gab einen Fehler beim Laden D:\n${item.exception?.message}", false)
        }
    }
}

fun GuildSlashCommand.ifConnected(execute: suspend GuildSlashCommandEvent<NoArgs>.() -> Unit) {
    execute {
        if (link.state != Link.State.CONNECTED) {
            respond("I'm not connected!")
        } else {
            execute(this)
        }
    }
}

@OptIn(KordVoice::class)
fun musicCommands() = commands("Music") {
    slash("join") {
        description = "Joins your current channel."

        execute {
            if (!joinIfNotConnected()) {
                respond("Please join a channel first!")
                return@execute
            }

            respond("Joined your channel!", false)
        }
    }

    slash("leave") {
        description = "Leaves your channel."

        ifConnected {
            link.disconnectAudio()

            respond("Byebye!", false)
        }
    }

    slash("play") {
        description = "Plays a song."

        execute(EveryArg) {
            if (!joinIfNotConnected()) {
                respond("Please join a channel first.")
                return@execute
            }

            val query = if (args.first.startsWith("http")) args.first else "ytsearch:${args.first}"

            play(query)
        }
    }

    slash("playfile", appName = "Play this file") {
        description = "Plays an audio attachment."

        execute(MessageArg) {
            if (!joinIfNotConnected()) {
                respond("Please join a channel first!")
                return@execute
            }

            if (args.first.attachments.isEmpty()) {
                respond("There is no file in this message!")
                return@execute
            }

            if (args.first.attachments.size > 1) {
                respond("There is more than one attachment in this message. I don't know which one to pick! D:")
                return@execute
            }

            val att = args.first.attachments.first()

            if (!att.filename.matches(".*(wav|mp3|ogg|webm|aac|mp4|m4a|flac|m3u|pls)$".toRegex())) {
                respond("The attachment isn't a music file!")
                return@execute
            }

            play(att.url)
        }
    }

    slash("pause") {
        description = "Pauses the current song."

        ifConnected {
            link.player.pause(!link.player.paused)
            respond("Paused the current song.", false)
        }
    }

    slash("resume") {
        description = "Resumes the current song."

        ifConnected {
            link.player.unPause()
            respond("Resumed the current song.", false)
        }
    }

    slash("queue") {
        description = "Shows the current queue."

        ifConnected {
            respond(false) {
                author {
                    icon = botIcon.url
                    name = "UwUbot"
                }

                title = "Current queue"

                addField("Now playing", link.player.playingTrack?.let {
                    "[${it.title}](${it.uri}) (${it.length.toTimeString()}) by ${it.author}"
                } ?: "Nothing\n")

                queues[guild.id]!!.mapIndexed { i, track ->
                    "${i + 1}. [${track.title}](${track.uri}) (${track.length.toTimeString()}) by ${track.author}"
                }.forEachIndexed { i, it -> addField(if (i == 0) "Next up:" else "", it) }
            }
        }
    }

    slash("skip") {
        description = "Skips the current song."

        ifConnected {
            if (link.player.playingTrack == null) {
                if (queues[guild.id]!!.firstOrNull() != null) {
                    maybePlay()
                    respond("Skipped the current song.", false)

                    return@ifConnected
                }

                respond("There's nothing playing right now.")
                return@ifConnected
            }

            link.player.stopTrack()

            respond("Skipped the current song.", false)
        }
    }

    slash("clear") {
        description = "Clears the current queue."

        ifConnected {
            if (queues[guild.id]?.isEmpty() != false) {
                respond("The queue is already empty!")
                return@ifConnected
            }

            queues[guild.id] = mutableListOf()
            respond("Cleared the queue!", false)
        }
    }

    slash("remove") {
        description = "Removes a song from the queue."

        execute(IntegerArg) {
            if (link.state != Link.State.CONNECTED) {
                respond("I'm not connected!")
                return@execute
            }

            if (queues[guild.id]!!.elementAtOrNull(args.first - 1) == null) {
                respond("There is no ${args.first}. song in the queue!")
                return@execute
            }

            val removedTrack = queues[guild.id]!!.removeAt(args.first - 1)
            respond("Removed ${args.first}. track (${removedTrack.title} by ${removedTrack.author}).")
        }
    }
}