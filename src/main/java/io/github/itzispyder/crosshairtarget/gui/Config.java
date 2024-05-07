package io.github.itzispyder.crosshairtarget.gui;

import io.github.itzispyder.crosshairtarget.Global;
import io.github.itzispyder.improperui.config.ConfigKey;
import io.github.itzispyder.improperui.config.Properties;
import io.github.itzispyder.improperui.script.ScriptParser;

public class Config implements Global {

    private static ConfigKey createConfigKey(String property) {
        return new ConfigKey("%s:config.properties:%s".formatted(modId, property));
    }

    public static Properties.Value read(String property) {
        return ScriptParser.getCache(modId).getProperty(createConfigKey(property));
    }

    public static void write(String property, Object value, boolean save) {
        ScriptParser.getCache(modId).setProperty(createConfigKey(property), value, save);
    }

    public static boolean readBool(String property) {
        var val = read(property);
        if (val == null) {
            write(property, true, true);
            val = read(property);
        }
        return val.get(0).toBool();
    }

    public static double readDouble(String property) {
        var val = read(property);
        if (val == null) {
            write(property, 1.0, true);
            val = read(property);
        }
        return val.get(0).toDouble();
    }
}
