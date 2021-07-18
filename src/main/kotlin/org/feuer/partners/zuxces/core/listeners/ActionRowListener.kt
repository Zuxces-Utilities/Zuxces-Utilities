package org.feuer.partners.zuxces.core.listeners

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ComponentLayout

class ActionRowListener(private val interactionCallback: HashMap<String, (interaction: InteractionEvent) -> Unit>, private val interactionClearAfterExecution: HashMap<String, Boolean>) {
    @SubscribeEvent
    fun onButtonEvent(event: ButtonClickEvent) {
        if (interactionCallback["button:${event.button?.id}"] != null) {
            val button = interactionCallback["button:${event.button?.id}"]
            button!!(InteractionEvent(event, null))
            for(buttons in event.message?.actionRows!!) {
                for (but in buttons.buttons) {
                    if (interactionClearAfterExecution["button:${but.id}"] == true) interactionCallback.remove("button:${but.id}")
                }
            }
        }
    }
    @SubscribeEvent
    fun onMenuEvent(event: SelectionMenuEvent) {
        if (interactionCallback["menu:${event.componentId}"] != null) {
            for (selection in event.selectedOptions!!) {
                val menu = interactionCallback["menu:${event.componentId}"]
                menu?.invoke(InteractionEvent(null, event))
            }
            if (interactionClearAfterExecution["menu:${event.componentId}"] == true) interactionCallback.remove("menu:${event.componentId}")
        }
    }
}
data class InteractionEvent(val buttonInteraction: ButtonClickEvent?, val menuSelectInteraction: SelectionMenuEvent?)