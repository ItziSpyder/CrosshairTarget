package io.github.itzispyder.crosshairtarget;

import io.github.itzispyder.crosshairtarget.gui.MenuCallbacks;
import io.github.itzispyder.improperui.ImproperUIAPI;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CrosshairTarget implements ModInitializer, Global {

    public static final KeyBinding BIND = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "binds.crosshairtarget.menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_MINUS,
            "binds.crosshairtarget"
    ));

    @Override
    public void onInitialize() {
        ImproperUIAPI.init(modId, CrosshairTarget.class, screens);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (BIND.wasPressed()) {
                ImproperUIAPI.parseAndRunFile(modId, "screen.ui", new MenuCallbacks());
            }
        });
    }
}
