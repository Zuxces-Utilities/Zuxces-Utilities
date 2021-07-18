package org.feuer.partners.zuxces.core

import io.github.cdimascio.dotenv.Dotenv
import java.lang.Exception
import java.lang.NullPointerException
import java.util.*

/**
 * Configuration class handling the ENV parsing
 */
class ConfigurationManager {
    private val dotenv = Dotenv.load()
    operator fun get(key: String): String {
        try {
            return dotenv[key.uppercase(Locale.getDefault())]!!
        } catch (e: Exception) {
            throw NullPointerException("An exception was raised when the bot attempted to grab something from the env file that didn't exist.")
        }
    }
}