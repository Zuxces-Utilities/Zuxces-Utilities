package org.feuer.partners.zuxces.commands.roles

import com.google.gson.Gson
import com.mongodb.BasicDBObject
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu
import net.dv8tion.jda.internal.interactions.ButtonImpl
import net.dv8tion.jda.internal.interactions.SelectionMenuImpl
import org.bson.Document
import org.feuer.partners.zuxces.core.annotation.MessageCommand
import org.feuer.partners.zuxces.core.interfaces.Command
import org.feuer.partners.zuxces.core.listeners.MessageCommandHandler
import org.feuer.partners.zuxces.core.schemas.SelfRoleList
import org.feuer.partners.zuxces.core.schemas.builders.SelfRoleBuilder
import org.litote.kmongo.findOne
import org.litote.kmongo.json
import org.litote.kmongo.*

class SelfRoleCommand: Command {
    val gson = Gson()
    @MessageCommand(["selfrole","sr","self-role"])
    override fun execute(
        event: Message,
        channel: MessageChannel,
        utils: MessageCommandHandler.Utils,
    ) {
        val collection = utils.getDB().getCollection("lists")
        val query = BasicDBObject()
        query["name"] = "roles"
        val args = event.contentRaw.removePrefix(">").split(" ").toMutableList()
        args.removeAt(0)
        val types = mutableListOf("deploy", "edit", "create", "delete")
        if (args.isEmpty()) return event.reply("A type is required for this command").queue()
        val type = args[0]
        if (!utils.check(type, types)) return event.reply("Parameter 'type' did not contain a valid value\n Valid values are ${types.joinToString{s -> "`$s`"}}").queue()
        when(type)
        {   // Deploy
            types[0] -> {

            } // Edit
            types[1] -> {
                val doc = collection?.findOne(query) ?: return event.reply("No self-role menus exist!").queue()
                val anyLists = doc["data"] as ArrayList<*>?
                val roleLists = mutableListOf<SelfRoleList>()
                if (anyLists != null) {
                    for (roles in anyLists) {
                        val json = gson.toJson(roles)
                        val data = gson.fromJson(json, SelfRoleList::class.java)
                        roleLists.add(data)
                    }
                }
                val message = MessageBuilder().setContent("Pick a item below to edit that list.")
                val selectListMenu = SelectionMenu.create("selectMenu")
                for (l in  0 until roleLists.size) {
                    val list = roleLists[l]
                    val plural = if (list.roles.size == 1) "role" else "roles"
                    selectListMenu.addOption("${list.name} (Contains ${list.roles.size} $plural)", "$l")
                }
              utils.Interactions().Menu().create(message.build(), true, selectListMenu.build() as SelectionMenuImpl).invoke {
                  if(it.menuSelectInteraction!!.member!!.id == event.author.id) {
                      val selection = roleLists[it.menuSelectInteraction!!.selectedOptions?.first()?.value?.toInt()!!]
                      utils.Interactions().Button().create(
                          MessageBuilder().setContent("What would you like to do for `${selection.name}`?").build(),
                          true,
                          Button.primary("name", "Change Name") as ButtonImpl,
                          Button.primary("add", "Change Roles") as ButtonImpl
                      ).invoke { ev ->
                          if(it.menuSelectInteraction!!.member!!.id == event.author.id) {
                              val even = ev.buttonInteraction!!
                              when (even.button?.id) {
                                  "name" -> {
                                      even.editMessage("You want to **${even.button!!.label}**").queue()
                                      utils.Interactions().Messages().create(
                                          MessageBuilder().setContent("What do you want the new name of `${selection.name}` to be?")
                                              .build(), true, event.author
                                      ).invoke { message ->
                                          val msg = message!!.messageInteraction?.message
                                          val previousAnyLists = doc["data"] as ArrayList<*>?
                                          val previousRoleLists = mutableListOf<SelfRoleList>()
                                          if (previousAnyLists != null) {
                                              for (roles in previousAnyLists) {
                                                  val json = gson.toJson(roles)
                                                  val data = gson.fromJson(json, SelfRoleList::class.java)
                                                  if (data.name == selection.name) {
                                                      val selfRoleBuilder = SelfRoleBuilder()
                                                      selfRoleBuilder.name = msg?.contentStripped
                                                      selfRoleBuilder.roles = data.roles
                                                      previousRoleLists.add(selfRoleBuilder.build())
                                                      break
                                                  }
                                                  previousRoleLists.add(data)
                                              }
                                          }
                                          val filter = Filters.eq("name", "roles")
                                          val update = Updates.set("data", previousRoleLists)
                                          collection.updateOne(filter, update)
                                          event.reply("Got it!\n**${selection.name}** -> **${msg?.contentStripped}**")
                                              .queue()

                                      }
                                  }
                                  "add" -> {
                                      even.editMessage("You asked to add a role to ${selection.name}").queue()
                                      val addRoleSelectMenu = SelectionMenu.create("roles")
                                      val selectedRoles = mutableListOf<String>()
                                          for (role in event.guild.roles) {
                                              addRoleSelectMenu.addOption(role.name, role.id)
                                          }
                                      var sent = false
                                      utils.Interactions().Menu().create(MessageBuilder("What roles do you want this list to have? (This message will be updated when you select the roles)").build(), false, addRoleSelectMenu.build() as SelectionMenuImpl).invoke { interactionEvent ->
                                          if (event.author.id == interactionEvent.menuSelectInteraction!!.member?.id) {

                                              val menuEvent = interactionEvent.menuSelectInteraction!!
                                              if (!sent) {
                                                  sent = true
                                                  utils.Interactions().Button().create(
                                                      MessageBuilder("When done, use this.").build(),
                                                      true,
                                                      Button.success("save", "Save") as ButtonImpl,
                                                      Button.danger("cancel", "Cancel") as ButtonImpl
                                                  ).invoke { ev ->
                                                      val buttonEvent = ev.buttonInteraction!!
                                                      when (buttonEvent.button?.id) {
                                                          "save" -> {
                                                              val previousAnyLists = doc["data"] as ArrayList<*>?
                                                              val previousRoleLists = mutableListOf<SelfRoleList>()
                                                              if (previousAnyLists != null) {
                                                                  for (roles in previousAnyLists) {
                                                                      val json = gson.toJson(roles)
                                                                      val data =
                                                                          gson.fromJson(json, SelfRoleList::class.java)
                                                                      if (data.name == selection.name) {
                                                                          val selfRoleBuilder = SelfRoleBuilder()
                                                                          selfRoleBuilder.name = data.name
                                                                          selfRoleBuilder.roles = selectedRoles
                                                                          previousRoleLists.add(selfRoleBuilder.build())
                                                                          break
                                                                      }
                                                                      previousRoleLists.add(data)
                                                                  }
                                                              }
                                                              println(previousRoleLists)
                                                              val filter = Filters.eq("name", "roles")
                                                              val update = Updates.set("data", previousRoleLists)
                                                              collection.updateOne(filter, update)
                                                              it.menuSelectInteraction.message?.delete()?.queue()
                                                              even.message?.delete()?.queue()
                                                              menuEvent.message?.delete()
                                                              val disabledButtons = mutableListOf<Button>()
                                                              for (button in buttonEvent.message?.buttons!!) {
                                                                  disabledButtons.add(button.asDisabled())
                                                              }
                                                              val roles = mutableListOf<String>()
                                                              for (role in selectedRoles) {
                                                                  roles.add(event.guild.getRoleById(role)?.asMention.toString())
                                                              }
                                                              buttonEvent.editMessage("Your edits have been carefully recorded..").setEmbeds(
                                                                  EmbedBuilder()
                                                                      .setTitle("${selection.name} Role List")
                                                                      .addField("Roles (${selectedRoles.size} Total)", roles.joinToString("\n"),false).build()
                                                              ).setActionRow(disabledButtons).queue()
                                                          }
                                                          "cancel" -> {
                                                              it.menuSelectInteraction.message?.delete()?.queue()
                                                              even.message?.delete()?.queue()
                                                              menuEvent.message?.delete()
                                                              return@invoke buttonEvent.editMessage("Command cancelled")
                                                                  .queue()
                                                          }
                                                      }
                                                  }
                                              }
                                                  if (!selectedRoles.contains(menuEvent.selectedOptions!!.first().value)) {
                                                      selectedRoles.add(menuEvent.selectedOptions!!.first().value)
                                                      menuEvent.editMessage("Selected roles are ${selectedRoles.joinToString { s -> "`${event.guild.getRoleById(s)?.name}`" }}")
                                                          .queue()
                                                  }
                                          }
                                      }
                                  }
                              }
                          }
                      }
                      return@invoke it.menuSelectInteraction.editMessage("You've selected **${selection.name}**").queue()
                  }
              }
            } // Create
            types[2] -> {
                val message = MessageBuilder().setContent("Alright, what do you want the name of this self-role list to be?").build()
                utils.Interactions().Messages().create(message, true, event.author).invoke { ev ->
                    val builder = SelfRoleBuilder()
                    builder.name = ev.messageInteraction!!.message.contentStripped
                    val obj = BasicDBObject()
                        obj["name"] = "roles"
                        obj["data"] = mutableListOf(builder.build())
                        val doc = collection?.findOne(query)
                        if (doc == null) {
                            collection?.insertOne(obj.json)
                        } else {
                            val previousAnyLists = doc["data"] as ArrayList<*>?
                            val previousRoleLists = mutableListOf<SelfRoleList>()
                            if (previousAnyLists != null) {
                                for (roles in previousAnyLists) {
                                    val json = gson.toJson(roles)
                                    println(json)
                                    val data = gson.fromJson(json, SelfRoleList::class.java)
                                    previousRoleLists.add(data)
                                }
                            }
                            previousRoleLists.add(builder.build())
                            val filter = Filters.eq("name", "roles")
                            val update = Updates.set("data", previousRoleLists)
                            collection.updateOne(filter, update)
                        }
                    return@invoke event.reply("Your self-role list '${ev.messageInteraction!!.message.contentStripped}' was created! (Use Edit type to edit the list)").queue()

                }
            } // Delete
            types[3] -> {

            }
        }
    }
}