package org.feuer.partners.zuxces.core.timer

import com.mongodb.BasicDBObject
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import org.feuer.partners.zuxces.core.Database
import org.litote.kmongo.findOne
import java.util.*

class StatusChange(private val bot: JDA): Thread() {
    val statuses = listOf("zuxces.net", "dsc.gg/Zuxces", "Minecraft", "${bot.guildCache.first().memberCount} total members!", ">help", "${getCommandCount()} commands executed")
        override fun run() {
            name = "Status Change Thread"
            Timer().scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    bot.presence.activity = Activity.playing(statuses.random());
                }
            }, 0, 30000)
        }
    }
fun getCommandCount(): Int {
    val db = Database()
    val collection = db.getCollection("stats")
    val query = BasicDBObject()
    query["name"] = "stats"
    val doc = collection?.findOne(query)
    return if (doc?.get("commands") == null) 0 else doc["commands"] as Int
}