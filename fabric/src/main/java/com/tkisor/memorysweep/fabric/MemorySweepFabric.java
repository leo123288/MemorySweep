package com.tkisor.memorysweep.fabric;

import com.tkisor.memorysweep.MemorySweep;
import net.fabricmc.api.ModInitializer;

public final class MemorySweepFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        MemorySweep.init();
    }
}
