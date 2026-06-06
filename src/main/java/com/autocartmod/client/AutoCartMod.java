package com.autocartmod.client;

import com.autocartmod.gui.ClickGuiScreen;
import com.autocartmod.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoCartMod implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("autocartmod");
    public static ModuleManager moduleManager;
    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[AutoCartMod] Initializing...");

        moduleManager = new ModuleManager();

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autocartmod.opengui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.autocartmod"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }
            if (moduleManager != null) {
                moduleManager.onTick(client);
            }
        });

        LOGGER.info("[AutoCartMod] Initialized successfully!");
    }
}
