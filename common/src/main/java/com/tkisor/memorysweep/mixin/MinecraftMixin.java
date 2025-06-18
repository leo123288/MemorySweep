package com.tkisor.memorysweep.mixin;

import com.tkisor.memorysweep.MemorySweep;
import com.tkisor.memorysweep.config.ModConfig;
import com.tkisor.memorysweep.data.Flags;
import com.tkisor.memorysweep.utils.SweepUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runTick(Z)V"))
    private void memorysweep(CallbackInfo ci) {
        if (!Flags.isClient) Flags.isClient = true;
        if (!Flags.initConfig) {
            ModConfig.init(Minecraft.getInstance().gameDirectory.toPath());
            Flags.initConfig = true;
        }

        if (Flags.sweepClient) {
            Flags.sweepClient = false;

            Thread sweepThread = new Thread(() -> {
                try {
                    long before = SweepUtil.getUsedMemory();
                    if (MemorySweep.player != null && !ModConfig.get().silent) {
                        MemorySweep.player.displayClientMessage(Component.translatable("memorysweep.gc.start", (SweepUtil.getMemoryInGB())), true);
                    }

                    System.gc();
                    Thread.sleep(100);
                    long after = SweepUtil.getUsedMemory();
                    if (MemorySweep.player != null && !ModConfig.get().silent) {
                        MemorySweep.player.displayClientMessage(Component.translatable("memorysweep.gc.end", (SweepUtil.getMemoryInGB())), true);
                    }
                    MemorySweep.logger.info("MemorySweep: GC executed. Before: {}MB, After: {}MB",
                            before / (1024 * 1024), after / (1024 * 1024));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    MemorySweep.logger.warn("Memory sweep interrupted.", e);
                }
            });

            sweepThread.setDaemon(true);
            sweepThread.setName("MemorySweep-Thread");
            sweepThread.start();

        }
    }
}
