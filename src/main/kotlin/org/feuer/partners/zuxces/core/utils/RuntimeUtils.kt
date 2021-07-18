package org.feuer.partners.zuxces.core.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.ajalt.mordant.TermColors
import org.feuer.partners.zuxces.core.VersionManager
import org.slf4j.LoggerFactory

/**
 * And object containing utils useful and actioned at JVM runtime
 */
object RuntimeUtils {
    val color = TermColors()
    val versions = VersionManager
    /**
     * This disables JDA logging, such as Login
     */
    fun disableJDALogging() {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        loggerContext.getLogger("net.dv8tion.jda").level = Level.OFF
    }

    fun startUpLog() {
        println("${color.rgb("FE39FF")}Zuxces Utilities is initializing...\n${color.brightBlue}Zuxces Utilities Version : ${versions.zuxcesUtilities}\n${color.magenta}JDA Version : ${versions.JDA}${color.reset}")
    }
}