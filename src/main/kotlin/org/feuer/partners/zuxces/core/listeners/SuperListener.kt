package org.feuer.partners.zuxces.core.listeners

import com.github.ajalt.mordant.TermColors
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import org.feuer.partners.zuxces.core.timer.StatusChange
import org.feuer.partners.zuxces.core.utils.RuntimeUtils

class SuperListener {
  private val color = TermColors()
    @SubscribeEvent
    fun onClientReady(event: ReadyEvent) {
        println("${color.rgb("FE39FF")}Zuxces Utilities is ready\n${color.brightBlue}Members : ${event.jda.guildCache.first().memberCount}\nPing : ${event.jda.gatewayPing}${RuntimeUtils.color.reset}")
        StatusChange(event.jda).run()
    }
}