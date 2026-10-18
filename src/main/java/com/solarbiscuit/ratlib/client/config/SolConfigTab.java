package com.solarbiscuit.ratlib.client.config;

import net.minecraft.network.chat.Component;

public final class SolConfigTab {
    private final String id;
    private final Component title;
    private final int order;
    private final Contents contents;

    public SolConfigTab(String id, Component title, int order, Contents contents) {
        this.id = id;
        this.title = title;
        this.order = order;
        this.contents = contents;
    }

    public String id() {
        return id;
    }

    public Component title() {
        return title;
    }

    public int order() {
        return order;
    }

    public Contents contents() {
        return contents;
    }

    @FunctionalInterface
    public interface Contents {
        void addEntries(SolConfigEntries entries);
    }
}
