package com.solarbiscuit.ratlib.client.config;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Client-only registry. Content mods register from client setup so a missing mod never appears.
 * FMLClientSetupEvent runs mods in parallel, so registration is copy-on-write: writers take a
 * lock and publish a new immutable list, readers never lock.
 */
public final class SolConfigTabs {
    private static volatile List<SolConfigTab> TABS = List.of();

    private SolConfigTabs() {}

    public static synchronized void register(String id, Component title, int order, SolConfigTab.Contents contents) {
        List<SolConfigTab> next = new ArrayList<>(TABS);
        next.removeIf(tab -> tab.id().equals(id));
        next.add(new SolConfigTab(id, title, order, contents));
        next.sort(Comparator.comparingInt(SolConfigTab::order).thenComparing(SolConfigTab::id));
        TABS = List.copyOf(next);
    }

    public static List<SolConfigTab> all() {
        return TABS;
    }

    @Nullable
    public static SolConfigTab byId(String id) {
        for (SolConfigTab tab : TABS) {
            if (tab.id().equals(id)) {
                return tab;
            }
        }
        return null;
    }
}
