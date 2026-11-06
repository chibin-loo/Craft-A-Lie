package com.artlu.craftalie;

import com.artlu.craftalie.game.GameManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CraftALie implements ModInitializer {
    public static final String MOD_ID = "craftalie";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CraftALieCommands.register();
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> GameManager.reset());
        ServerTickEvents.END_SERVER_TICK.register(GameManager::tick);
        LOGGER.info("Craft-A-Lie loaded!");
    }
}