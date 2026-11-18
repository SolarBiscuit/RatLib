package com.solarbiscuit.ratlib.compat;

import com.solarbiscuit.ratlib.RatLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Better Combat animations reach mobs through Better Mob Combat automatically. A content mod
 * registers its namespace with a toggle; while the toggle is off, every entity id in that
 * namespace sits in BMC's {@code mob_blacklist} so those mobs keep vanilla poses.
 * Applied on server start; call {@link #applyOnServerThread()} after the toggle changes.
 */
@Mod.EventBusSubscriber(modid = RatLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BetterMobCombat {
    public static final String MOD_ID = "bettermobcombat";

    private static volatile Map<String, BooleanSupplier> toggles = Map.of();

    private BetterMobCombat() {}

    public static boolean isLoaded() {
        return Mods.loaded(MOD_ID);
    }

    /** {@code animationsEnabled} false blacklists every entity registered under {@code namespace}. */
    public static synchronized void register(String namespace, BooleanSupplier animationsEnabled) {
        Map<String, BooleanSupplier> next = new LinkedHashMap<>(toggles);
        next.put(namespace, animationsEnabled);
        toggles = Collections.unmodifiableMap(next);
    }

    // Config events fire on mod-loading workers or NightConfig's file-watcher thread. Better Mob
    // Combat reads its config on the server thread, so hand the write to it; with no server
    // running, ServerStartingEvent applies the blacklist instead.
    public static void applyOnServerThread() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(BetterMobCombat::apply);
        }
    }

    public static void apply() {
        if (!isLoaded() || toggles.isEmpty()) {
            return;
        }
        try {
            Class<?> bmcClass = Class.forName("me.Thelnfamous1.bettermobcombat.BetterMobCombat");
            Object config = bmcClass.getMethod("getServerConfig").invoke(null);
            if (config == null) {
                return;
            }
            Field blacklistField = config.getClass().getField("mob_blacklist");
            String[] current = (String[]) blacklistField.get(config);
            Set<String> ids = new LinkedHashSet<>();
            if (current != null) {
                for (String id : current) {
                    if (id != null && !id.isBlank()) {
                        ids.add(id);
                    }
                }
            }
            for (Map.Entry<String, BooleanSupplier> entry : toggles.entrySet()) {
                List<String> ours = entityIds(entry.getKey());
                if (enabled(entry.getValue())) {
                    ours.forEach(ids::remove);
                } else {
                    ids.addAll(ours);
                }
            }
            blacklistField.set(config, ids.toArray(String[]::new));
            Method update = bmcClass.getMethod("updateServerConfig", config.getClass(), boolean.class);
            update.invoke(null, config, true);
        } catch (Throwable ignored) {
        }
    }

    private static boolean enabled(BooleanSupplier toggle) {
        try {
            return toggle.getAsBoolean();
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static List<String> entityIds(String namespace) {
        List<String> ids = new ArrayList<>();
        for (ResourceLocation key : ForgeRegistries.ENTITY_TYPES.getKeys()) {
            if (namespace.equals(key.getNamespace())) {
                ids.add(key.toString());
            }
        }
        return ids;
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        apply();
    }
}
