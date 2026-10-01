package com.sixtyseven;

import net.fabricmc.api.ModInitializer;

public class SixtySevenMod implements ModInitializer {
    public static final String MOD_ID = "sixtyseven";

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModItems.register();
        Powers.init();
    }
}
