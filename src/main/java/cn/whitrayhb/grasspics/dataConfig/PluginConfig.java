package cn.whitrayhb.grasspics.dataConfig;

import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;

public class PluginConfig extends JavaAutoSavePluginConfig {
    public final static PluginConfig INSTANCE = new PluginConfig();
    public Value<Long> getPictureLockTime = value("getPictureLockTime", 30 * 1000L);
    public Value<Long> getPictureTimeout = value("fetchPictureTimeout", 10 * 1000L);
    public Value<Long> postPictureLockTime = value("postPictureLockTime", 10 * 1000L);
    public Value<Long> postPictureTimeout = value("postPictureTimeout", 30 * 1000L);
    public Value<Boolean> isQuotePostEnabled = value("isQuotePostEnabled", true);
    public Value<String> NLRPassToken = value("NLRPassToken","");
    private PluginConfig() {
        super("Config");
    }
}
