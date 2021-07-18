package org.feuer.partners.zuxces.commands

import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.internal.interactions.ButtonImpl
import org.feuer.partners.zuxces.core.annotation.MessageCommand
import org.feuer.partners.zuxces.core.interfaces.Command
import org.feuer.partners.zuxces.core.listeners.MessageCommandHandler

class ButtonTestCommand: Command {
    @MessageCommand(["test-button","tb"])
    override fun execute(event: Message, channel: MessageChannel, utils: MessageCommandHandler.Utils, args: Array<String>) {
        val messageBuilder = MessageBuilder()
        utils.Interactions().Button().create(messageBuilder.setContent("Are you sure you want to ban this user?").build(), true, Button.danger("true", "Yes") as ButtonImpl, Button.success("false", "No") as ButtonImpl).invoke{ev ->
            when (ev.buttonInteraction!!.button?.id) {
                "true" -> {
                    ev.buttonInteraction!!.reply("User has been banned from ${event.guild.name}").queue()
                }
                "false" -> {
                    ev.buttonInteraction!!.reply("Command has been cancelled").queue()
                }
            }
        }
    }
}