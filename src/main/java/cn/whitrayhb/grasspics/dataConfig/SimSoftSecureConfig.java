package cn.whitrayhb.grasspics.dataConfig;


import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;

public class SimSoftSecureConfig extends JavaAutoSavePluginConfig {
    public static final SimSoftSecureConfig INSTANCE = new SimSoftSecureConfig();
    public final Value<String> user = value("user", "");
    public final Value<String> token = value("token", "");

    public SimSoftSecureConfig() {
        super("SimsoftSecure");
    }
}
