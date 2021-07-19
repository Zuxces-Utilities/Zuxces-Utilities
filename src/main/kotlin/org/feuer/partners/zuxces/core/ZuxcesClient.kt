package org.feuer.partners.zuxces.core

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.hooks.AnnotatedEventManager
import net.dv8tion.jda.api.requests.GatewayIntent
import org.feuer.partners.zuxces.commands.ButtonTestCommand
import org.feuer.partners.zuxces.commands.MenuTestCommand
import org.feuer.partners.zuxces.commands.roles.SelfRoleCommand
import org.feuer.partners.zuxces.core.listeners.ActionRowListener
import org.feuer.partners.zuxces.core.listeners.InteractionEvent
import org.feuer.partners.zuxces.core.listeners.MessageCommandHandler
import org.feuer.partners.zuxces.core.listeners.SuperListener

/**
 * Contains the methods and abstractions for the JDA instance
 */
class ZuxcesClient() {
    var builder: JDABuilder? = null
    private val messageHandler = MessageCommandHandler()
    /**
     * Set up the JDA instance with the basic intents and its token to be built later on in the stack
     * @param token The Discord Bot token
     */
    fun setup(token: String) {
       builder = JDABuilder
           .create(
               token,
               mutableListOf(
                   GatewayIntent.GUILD_MESSAGES,
                   GatewayIntent.GUILD_MEMBERS,
                   GatewayIntent.GUILD_PRESENCES
               ))
           .setEventManager(AnnotatedEventManager())

    }
    fun registerEventListeners() {
         builder!!.addEventListeners(
             SuperListener(),
             messageHandler,
             ActionRowListener(messageHandler.interactionCallback, messageHandler.interactionClearAfterExecution)
         )
    }
    fun registerMessageCommands() {
        messageHandler.registerCommands(ButtonTestCommand(), MenuTestCommand(), SelfRoleCommand())
    }
    fun start(): JDA {
        return builder!!.build()
    }
}