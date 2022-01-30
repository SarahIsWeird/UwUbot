package com.sarahisweird.uwubot

import kotlinx.serialization.Serializable
import me.jakejmattson.discordkt.dsl.Data

@Serializable
data class Node(
    val uri: String,
    val password: String
)

@Serializable
data class Configuration(
    val nodes: List<Node>
) : Data()
