package org.feuer.partners.zuxces.core.annotation

import net.dv8tion.jda.api.Permission

annotation class MessageCommand(
/**
 * Aliases of this command [Array]
 */
val aliases: Array<String>,
/**
 * Description of this command
 */
val description: String = "",
/**
 * Usage of this command
 */
val usage: String = "",
/**
 * Require the member to have a certain [net.dv8tion.jda.api.Permission]
 */
val permission: Permission = Permission.UNKNOWN,
)
