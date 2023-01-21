package cn.whitrayhb.grasspics.dataConfig;

import net.mamoe.mirai.console.data.Value;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;

import java.util.ArrayList;
import java.util.List;

public class PluginData extends JavaAutoSavePluginData {
    public static final PluginData INSTANCE = new PluginData();
    public final Value<List<Long>> savedQQ = typedValue("savedQQ", createKType(List.class, createKType(Long.class)), new ArrayList<>());

    public PluginData() {
        super("data");
    }
}
