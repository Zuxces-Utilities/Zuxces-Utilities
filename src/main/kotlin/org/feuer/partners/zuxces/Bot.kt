package org.feuer.partners.zuxces

import org.feuer.partners.zuxces.core.ConfigurationManager
import org.feuer.partners.zuxces.core.ZuxcesClient
import org.feuer.partners.zuxces.core.utils.RuntimeUtils

fun main() {
 val utils = RuntimeUtils
 utils.startUpLog()
 val config = ConfigurationManager()
 val client = ZuxcesClient()
 client.setup(config["token"])
 client.registerEventListeners()
 client.registerMessageCommands()
 client.start()
}