package org.feuer.partners.zuxces.core.listeners

import com.google.gson.Gson
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import org.feuer.partners.zuxces.core.annotation.MessageCommand
import java.lang.Exception
import java.lang.NumberFormatException
import java.lang.reflect.Method
import java.util.HashMap
import java.util.concurrent.Executors

/**
 * Handles the fucking commands
 */
class MessageCommandHandler() {
    val commands = HashMap<String, Command>()
    /**
     * The handling method using jda annotation methods.
     */
    @SubscribeEvent
    fun handle(event: GuildMessageReceivedEvent) {
        val prefix = ">"
        if (event.message.contentRaw.startsWith("<@!750368143209267281>")) return event.message.reply("My prefix in this guild is set to `${prefix}`").queue()
        if (!event.message.contentRaw.startsWith(prefix)) return
        if (event.author.isBot || event.message.isWebhookMessage) return
        val splitContent = event.message.contentRaw.removePrefix(prefix).split(" ").toTypedArray()
        if (!commands.containsKey(splitContent[0])) return
        val command = commands[splitContent[0]]
        val annotation = command!!.commandAnnotation
        async.submit {
            invokeMethod(command, getParameters(splitContent, command, event.message, event.jda), event)
        }
    }


    /**
     * Register a normal Message Type command
     */
    fun registerCommand(command: org.feuer.partners.zuxces.core.interfaces.Command) {
        for (method in command.javaClass.methods) {
            val annotation = method.getAnnotation(MessageCommand::class.java) ?: continue
            require(annotation.aliases.isNotEmpty()) { "No aliases have been defined!" }
            val simpleCommand: Command = Command(annotation, method, command)
            for (alias in annotation.aliases) {
                commands[alias.toLowerCase()] = simpleCommand
            }
        }
    }
    /**
     * Register multiple normal Message Type commands
     */
    fun registerCommands(vararg commandClasses: org.feuer.partners.zuxces.core.interfaces.Command) {
        for (command in commandClasses) {
        for (method in command.javaClass.methods) {
            val annotation = method.getAnnotation(MessageCommand::class.java) ?: continue
            require(annotation.aliases.isNotEmpty()) { "No aliases have been defined!" }
            val simpleCommand = Command(annotation, method, command)
            for (alias in annotation.aliases) {
                commands[alias.toLowerCase()] = simpleCommand
             }
           }
        }
    }

    /**
     * Parameters
     */
    private fun getParameters(splitMessage: Array<String>, command: Command?, message: Message?, jda: JDA): Array<Any?> {
        val args = splitMessage.copyOfRange(1, splitMessage.size)
        val parameterTypes = command!!.method.parameterTypes
        val parameters = arrayOfNulls<Any>(parameterTypes.size)
        var stringCounter = 0
        for (i in parameterTypes.indices) {
            val type = parameterTypes[i]
            if (type == String::class.java) {
                if (stringCounter++ == 0) {
                } else {
                    if (args.size + 2 > stringCounter) {
                        parameters[i] = args[stringCounter - 2]
                    }
                }
            } else if (type == Array<String>::class.java) {
                parameters[i] = args
            } else if (type == Message::class.java) {
                parameters[i] = message
            } else if (type == JDA::class.java) {
                parameters[i] = jda
            } else if (type == TextChannel::class.java) {
                parameters[i] = message?.textChannel
            } else if (type == User::class.java) {
                parameters[i] = message?.author
            } else if (type == MessageChannel::class.java) {
                parameters[i] = message?.channel
            } else if (type == Guild::class.java) {
                if (message?.channelType != ChannelType.TEXT) {
                    parameters[i] = message?.guild
                }
            } else if (type == Array<Any>::class.java) {
                parameters[i] = getObjectsFromString(jda, args)
            } else {
                parameters[i] = null
            }
        }
        return parameters
    }

    /**
     * Do I need to explain this?
     */
    private fun getObjectsFromString(jda: JDA, args: Array<String>): Array<Any?> {
        val objects = arrayOfNulls<Any>(args.size)
        for (i in args.indices) {
            objects[i] = getObjectFromString(jda, args[i])
        }
        return objects
    }

    /**
     * Grab a singular Object from string
     */
    private fun getObjectFromString(jda: JDA, arg: String): Any {
        try {
            return Integer.valueOf(arg)
        } catch (e: NumberFormatException) {
            println("[ERROR] $e")
        }
        if (arg.matches("<@([0-9]*)>".toRegex())) {
            val id = arg.substring(2, arg.length - 1)
            val user = jda.getUserById(id)
            if (user != null) {
                return user
            }
        }
        if (arg.matches("<#([0-9]*)>".toRegex())) {
            val id = arg.substring(2, arg.length - 1)
            val channel = jda.getTextChannelById(id) as Invite.Channel?
            if (channel != null) {
                return channel
            }
        }
        return arg
    }

    /**
     * Invokes (runs) the command
     */
    private fun invokeMethod(command: Command?, parameters: Array<Any?>, event: GuildMessageReceivedEvent) {
        val m = command!!.method
        try {
            m.invoke(command.executor, *parameters)
        } catch(e: Exception) {
            event.message.reply("Something broke when attempting to execute that command.. :c").queue()
        }
    }

    /**
     * Command le constructor
     */
    inner class Command internal constructor(
        val commandAnnotation: MessageCommand,
        val method: Method,
        val executor: org.feuer.partners.zuxces.core.interfaces.Command
    )



    /**
     * Execute commands asynchronously
     */
    companion object {
        private val async = Executors.newCachedThreadPool()
    }

}
class Utils(private val event: GuildMessageReceivedEvent, private val commands: HashMap<String, MessageCommandHandler.Command>) {
    fun getCommands(): HashMap<String, MessageCommandHandler.Command> {
        return commands
    }
    fun report() {
        TODO("The mongo controller has not been built yet..")
    }
}
