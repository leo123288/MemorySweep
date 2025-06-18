package com.tkisor.memorysweep.mixin;

import com.tkisor.memorysweep.config.ModConfig;
import com.tkisor.memorysweep.data.Flags;
import com.tkisor.memorysweep.utils.SweepUtil;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;startMetricsRecordingTick()V"))
    private void memory(CallbackInfo ci) {
        if (Flags.isClient) return;
        if (!Flags.initConfig) {
            ModConfig.init(((MinecraftServer)(Object)this).getServerDirectory().toPath());
            Flags.initConfig = true;
        }

        if (SweepUtil.isSweep()) {
            SweepUtil.sweep();
        }
    }
}
