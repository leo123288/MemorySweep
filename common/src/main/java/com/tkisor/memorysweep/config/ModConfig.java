package com.tkisor.memorysweep.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import com.tkisor.memorysweep.MemorySweep;
import dev.architectury.platform.Platform;
import net.minecraft.client.gui.screens.Screen;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ModConfig {
    private static Path path;
    private static volatile ModConfig config;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final String _comment1 = "Whether to enable MemorySweep.";
    public boolean memorySweep = true;

    private final String _comment2 = "The interval time of memory cleaning. Unit: seconds.";
    public int sweepInterval = 900;

    private final String _comment3 = "The minimum memory usage percentage (calculated over a 10-second average) to trigger cleaning; lower values enforce stricter cleanup.";
    public int minMemoryUsage = 75;

    private final String _comment4 = "Enable silent memory cleaning; no UI messages displayed, but logs remain.";
    public Boolean silent = false;


    private ModConfig() {
    }

    public static void init(Path path_) {
        path = path_.resolve("config/"+ MemorySweep.MOD_ID+".json");
    }

    public static ModConfig create() {
        if (path == null)
            throw new IllegalStateException("[MemorySweep.Config]Path must be initialized before creating Config.");
        if (config == null) {
            synchronized (ModConfig.class) {
                if (config == null) {
                    config = new ModConfig();
                }
            }
        }
        read();
        write();

        return config;
    }

    public static ModConfig get() {
        if (config == null) create();
        return config;
    }

    public static void read() {
        if (!Files.exists(path)) {
            LogUtils.getLogger().info("[Config.read] No config file found; using default values.");

        } else {
            try {
                List<String> lines = Files.readAllLines(path);
                String rawData = String.join("\n", lines);
                config = GSON.fromJson(rawData, ModConfig.class);
                LogUtils.getLogger().info("[Config.read] Loaded config info from '{}'!", path);

            } catch (JsonIOException | JsonSyntaxException e) {
                writeCopy();
                reset();
                LogUtils.getLogger().info("[Config.read] The config couldn't be loaded; copied old data and reset:", e);

            } catch (IOException e) {
                reset();
                LogUtils.getLogger().error("[Config.read] An error occurred while trying to load config data from '{}':", path, e);

            }
        }
    }

    public static void write() {
        try (FileWriter fw = new FileWriter(path.toFile())) {
            GSON.toJson(config, config.getClass(), fw);
            LogUtils.getLogger().info("[Config.write] Saved config info to '{}'!", path);

        } catch (Exception e) {
            LogUtils.getLogger().error("[Config.write] An error occurred while trying to save the config to '{}':", path, e);

        }
    }

    public static void writeCopy() {
        try {
            Files.copy(path, path.resolveSibling("memorysweep_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".json"));
        } catch (IOException e) {
            LogUtils.getLogger().warn("[Config.writeCopy] An error occurred trying to write a copy of the original config file:", e);
        }
    }

    public static void reset() {
        config = new ModConfig();
    }

    private static final String[] CONFIG_MODID = {
            "cloth-config",
            "cloth_config"
    };

    public static boolean isLoaderConfigMod() {
        return Arrays.stream(CONFIG_MODID)
                .anyMatch(Platform::isModLoaded);
    }

    public Screen getConfigScreen(Screen parent) {
        if (isLoaderConfigMod()) {
            return ClothConfig.create(parent);
        }
        return null;
    }
}
