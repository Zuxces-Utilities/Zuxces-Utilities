package org.feuer.partners.zuxces.commands

import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl
import org.feuer.partners.zuxces.core.annotation.MessageCommand
import org.feuer.partners.zuxces.core.interfaces.Command
import org.feuer.partners.zuxces.core.listeners.MessageCommandHandler

class MenuTestCommand: Command {
    @MessageCommand(["tm","test-menu"])
    override fun execute(
        event: Message,
        channel: MessageChannel,
        utils: MessageCommandHandler.Utils,
    ) {
        val messageSelection = MessageBuilder("Selection menus mmmm").build()
        val menu = SelectionMenu.create("uwu")
            .addOption(
                "Cute",
                "c",
                "Are u CUTE???",
                Emoji.fromEmote("NC_hug",842261072718856222,false)
            )
            .addOption(
                "Not Cute",
                "nc",
                "Are u not cute??? :c",
                Emoji.fromEmote("NC_O2blush",841509413269667840,false)
            )
            .build() as SelectionMenuImpl
         utils.Interactions().Menu().create(messageSelection, false, menu).invoke { event ->
            val interaction = event.menuSelectInteraction!!
            when (interaction.selectedOptions!!.first().value) {
                "c" -> return@invoke interaction.reply("${interaction.interaction.member?.asMention}, You are CUTE!!! <a:NC_love:830701610929094677>").queue()
                "nc" -> return@invoke interaction.reply("${interaction.interaction.member?.asMention}, :c... You're cute shush :(").queue()
            }
        }
    }
}