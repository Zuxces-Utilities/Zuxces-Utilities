package org.feuer.partners.zuxces.core.interfaces

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import org.feuer.partners.zuxces.core.listeners.MessageCommandHandler


interface Command {
        fun execute(event: Message, channel: MessageChannel, utils: MessageCommandHandler.Utils)
}