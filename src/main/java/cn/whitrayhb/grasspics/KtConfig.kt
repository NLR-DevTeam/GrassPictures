package cn.whitrayhb.grasspics

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value

class KtConfig {
    object SimsoftSecure : AutoSavePluginConfig("SimsoftSecure") {
        @ValueDescription("SIMS_USER")
        var user by value<String>("")

        @ValueDescription("SIMS_TOKEN")
        var token by value<String>("")
    }
}