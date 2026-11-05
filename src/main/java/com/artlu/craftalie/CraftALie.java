package com.artlu.craftalie;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CraftALie implements ModInitializer {
    public static final String MOD_ID = "craftalie";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Craft-A-Lie loaded!");
    }
}