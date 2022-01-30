package com.sarahisweird.uwubot

import me.jakejmattson.discordkt.dsl.Permission
import me.jakejmattson.discordkt.dsl.PermissionSet
import me.jakejmattson.discordkt.dsl.permission

object Permissions : PermissionSet {
    val GUILD_OWNER = permission("Guild owner") { users(guild!!.ownerId) }
    val EVERYONE = permission("Everyone") { roles(guild!!.everyoneRole.id) }

    override val commandDefault: Permission = EVERYONE
    override val hierarchy: List<Permission> = listOf(EVERYONE, GUILD_OWNER)
}