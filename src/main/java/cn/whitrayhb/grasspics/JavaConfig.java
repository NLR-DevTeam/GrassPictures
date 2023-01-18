package cn.whitrayhb.grasspics;


import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;

public class JavaConfig extends JavaAutoSavePluginConfig {
    public static final JavaConfig INSTANCE = new JavaConfig();
    public final Value<String> user = value("user", "");
    public final Value<String> token = value("token", "");

    public JavaConfig() {
        super("SimsoftSecure");
    }
}
