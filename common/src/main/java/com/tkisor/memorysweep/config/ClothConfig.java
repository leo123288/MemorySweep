package com.tkisor.memorysweep.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfig {
    private Screen parent;

    private ClothConfig(Screen parent) {
        this.parent = parent;
    }

    public static Screen create(Screen parent) {
        return new ClothConfig(parent).builder();
    }

    private Screen builder() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("text.memorysweep.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ModConfig config = ModConfig.get();

        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("text.memorysweep.category.config"));

        BooleanListEntry b1 = entryBuilder.startBooleanToggle(Component.translatable("text.memorysweep.memorysweep"), config.memorySweep)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("text.memorysweep.desc.memorysweep"))
                .setSaveConsumer(save -> config.memorySweep = save)
                .build();
        IntegerListEntry b2 = entryBuilder.startIntField(Component.translatable("text.memorysweep.sweep_interval"), config.sweepInterval)
                .setDefaultValue(900)
                .setMin(0)
                .setMax(Integer.MAX_VALUE)
                .setTooltip(Component.translatable("text.memorysweep.desc.sweep_interval"))
                .setSaveConsumer(save -> config.sweepInterval = save)
                .build();
        IntegerListEntry b3 = entryBuilder.startIntField(Component.translatable("text.memorysweep.min_memory_usage"), config.minMemoryUsage)
                .setDefaultValue(75)
                .setMin(0)
                .setMax(100)
                .setTooltip(Component.translatable("text.memorysweep.desc.min_memory_usage"))
                .setSaveConsumer(save -> config.minMemoryUsage = save)
                .build();
        BooleanListEntry b4 = entryBuilder.startBooleanToggle(Component.translatable("text.memorysweep.silent"), config.silent)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("text.memorysweep.desc.silent"))
                .setSaveConsumer(save -> config.silent = save)
                .build();
        category.addEntry(b1);
        category.addEntry(b2);
        category.addEntry(b3);
        category.addEntry(b4);

        builder.setSavingRunnable(ModConfig::write);
        return builder.build();
    }
}
