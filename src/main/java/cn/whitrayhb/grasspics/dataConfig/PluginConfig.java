package cn.whitrayhb.grasspics.dataConfig;


import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;

public class PluginConfig extends JavaAutoSavePluginConfig {
    public static final PluginConfig INSTANCE = new PluginConfig();
    public final Value<String> user = value("user", "");
    public final Value<String> token = value("token", "");

    public PluginConfig() {
        super("SimsoftSecure");
    }
}
