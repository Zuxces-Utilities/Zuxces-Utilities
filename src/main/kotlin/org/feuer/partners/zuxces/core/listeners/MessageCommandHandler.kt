package org.feuer.partners.zuxces.core.listeners

import com.mongodb.BasicDBObject
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent
import net.dv8tion.jda.internal.interactions.ButtonImpl
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl
import org.feuer.partners.zuxces.core.Database
import org.feuer.partners.zuxces.core.annotation.MessageCommand
import org.feuer.partners.zuxces.core.schemas.SelfRoleList
import org.litote.kmongo.findOne
import org.litote.kmongo.json
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.Executors
import javax.naming.InvalidNameException
import org.litote.kmongo.json
import org.litote.kmongo.*

/**
 * Handles the fucking commands
 */
class MessageCommandHandler() {
    val commands = HashMap<String, Command>()
    val interactionClearAfterExecution = HashMap<String, Boolean>()
    val interactionCallback = HashMap<String, (interaction: InteractionEvent) -> Unit>()
    val db = Database()
    /**
     * The handling method using jda annotation methods.
     */
    @SubscribeEvent
    fun handle(event: GuildMessageReceivedEvent) {
        val prefix = ">"
        if (!event.message.contentRaw.startsWith(prefix)) return
        if (event.author.isBot || event.message.isWebhookMessage) return
        println(event.message.contentRaw.removePrefix(prefix).split(" ").toTypedArray()[0])
        val splitContent = event.message.contentRaw.removePrefix(prefix).split(" ").toTypedArray()
        if (!commands.containsKey(splitContent[0])) return
        val command = commands[splitContent[0]]
        async.submit {
            updateCommandStats()
            command!!.executor.execute(event.message, event.channel, Utils(event, commands, db))
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
        println("Does this work")
        try {
            m.invoke(command.executor, Utils(event, commands, db), *parameters)
        } catch(e: Exception) {
            e.printStackTrace()
            event.message.reply("Something broke when attempting to execute that command.. :c").queue()
        }
    }
    private fun updateCommandStats() {
        val collection = db.getCollection("stats")
        val query = BasicDBObject()
        query["name"] = "stats"
        val doc = collection?.findOne(query)
        var count = if (doc?.get("commands") == null) 0 else doc["commands"] as Int
        count += 1
        val updatedCommandStats = BasicDBObject()
        updatedCommandStats["name"] = "stats"
        updatedCommandStats["commands"] = count
        if (doc != null) {
            val filter = Filters.eq("name", "stats")
            val update = Updates.set("commands", count)
            collection?.updateOne(filter, update)
        }
        if (doc == null) {
            collection?.insertOne(updatedCommandStats.json)
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
    inner class Utils(private val event: GuildMessageReceivedEvent, private val commands: HashMap<String, MessageCommandHandler.Command>, private val db: Database) {
        fun getDB(): Database {
            return db
        }
        fun getCommands(): HashMap<String, MessageCommandHandler.Command> {
            return commands
        }
        fun check(arg: String, check: String): Boolean {
            if (check.contains(arg)) return true
            return false
        }
        fun check(arg: String, check: MutableCollection<String>): Boolean {
            if (check.contains(arg)) return true
            return false
        }
        fun convert(str: String): Map<String, String> {
            val tokens = str.split(" |=".toRegex()).toTypedArray()
            val map: MutableMap<String, String> = HashMap()
            var i = 0
            while (i < tokens.size - 1) {
                map[tokens[i++]] = tokens[i++]
            }
            return map
        }
        fun report() {
            TODO("The mongo controller has not been built yet..")
        }
        inner class Interactions {
            inner class Messages {
                fun create(
                    message: Message,
                    clearAfterExecution: Boolean = true,
                    user: User
                ): ((InteractionEvent) -> Unit) -> Unit {
                    val msg = event.message.reply(message)
                    msg.queue()
                    return fun(listener: (InteractionEvent) -> Unit) {
                        interactionClearAfterExecution["message:${event.channel.id}:${user.id}"] = clearAfterExecution
                        interactionCallback["message:${event.channel.id}:${user.id}"] = listener
                    }
                }
            }
            inner class Button {
                fun create(message: Message, clearAfterExecution: Boolean = true, vararg buttons: ButtonImpl): ((InteractionEvent) -> Unit) -> Unit {
                    val msg = event.message.reply(message)
                    val buttonQueue = mutableListOf<net.dv8tion.jda.api.interactions.components.Button>()
                    for (button in buttons) {
                        if (button.id?.startsWith("button:") == true) throw InvalidNameException("Button id can't contain button: at start!")
                        buttonQueue.add(button as net.dv8tion.jda.api.interactions.components.Button)
                    }
                    msg.setActionRow(buttonQueue)
                    msg.queue()
                    return fun(listener: (InteractionEvent) -> Unit) {
                        for (button in buttons) {
                            interactionClearAfterExecution["button:${button.id}"] = clearAfterExecution
                            interactionCallback["button:${button.id}"] = listener
                        }
                    }
                }
                fun remove(vararg buttons: String) {
                    for (button in buttons) {
                        interactionCallback.remove("button:${button}")
                    }
                }
            }
            inner class Menu {
                fun create(message: Message, clearAfterExecution: Boolean = true, menu: SelectionMenuImpl): ((InteractionEvent) -> Unit) -> Unit {
                    val msg = event.message.reply(message)
                    if (menu.id?.startsWith("menu:") == true) throw InvalidNameException("Menu id can't contain menu: at start!")
                    msg.setActionRow(menu)
                    msg.queue()
                    return fun (listener: (InteractionEvent) -> Unit) {
                        interactionClearAfterExecution["menu:${menu.id}"] = clearAfterExecution
                        interactionCallback["menu:${menu.id}"] = listener
                    }
                }
                }
            }
        }
    }


