package com.tkisor.memorysweep.forge;

import com.tkisor.memorysweep.MemorySweep;
import net.minecraftforge.fml.common.Mod;

@Mod(MemorySweep.MOD_ID)
public final class MemorySweepForge {
    public MemorySweepForge() {
        // Run our common setup.
        MemorySweep.init();
    }
}
