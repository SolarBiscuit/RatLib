package com.solarbiscuit.ratlib.client.skin;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class HumanoidSkinTextures {
    @SuppressWarnings("removal")
    public static final ResourceLocation STEVE = new ResourceLocation("minecraft", "textures/entity/steve.png");
    /** Bumped on resource reload; instances re-list their textures when their generation is stale. */
    private static final AtomicInteger GENERATION = new AtomicInteger();

    private final String folder;
    private final ResourceLocation fallback;
    private final List<ResourceLocation> textures = new ArrayList<>();
    private final List<ResourceLocation> aetherTextures = new ArrayList<>();
    private int loadedGeneration = -1;

    public HumanoidSkinTextures(String folder) {
        this(folder, STEVE);
    }

    public HumanoidSkinTextures(String folder, ResourceLocation fallback) {
        this.folder = folder;
        this.fallback = fallback;
    }

    /** Forces every instance to re-list its textures on next use, e.g. after a resource pack reload. */
    public static void invalidateAll() {
        GENERATION.incrementAndGet();
    }

    public ResourceLocation pick(int index) {
        return pickFrom(this.textures, index);
    }

    public ResourceLocation pickAether(int index) {
        // Load first so the empty check below doesn't see pre-load (or just-invalidated) lists.
        ensureLoaded();
        List<ResourceLocation> pool = this.aetherTextures.isEmpty() ? this.textures : this.aetherTextures;
        return pickFrom(pool, index);
    }

    public ResourceLocation pickFromUuid(UUID uuid) {
        ensureLoaded();
        if (this.textures.isEmpty()) {
            return this.fallback;
        }
        return this.textures.get(Math.floorMod(uuid.hashCode(), this.textures.size()));
    }

    public int skinCount() {
        ensureLoaded();
        return this.textures.size();
    }

    public int aetherSkinCount() {
        ensureLoaded();
        return this.aetherTextures.size();
    }

    public List<ResourceLocation> all() {
        ensureLoaded();
        return Collections.unmodifiableList(this.textures);
    }

    private ResourceLocation pickFrom(List<ResourceLocation> pool, int index) {
        ensureLoaded();
        if (pool.isEmpty()) {
            return this.fallback;
        }
        return pool.get(Math.floorMod(index, pool.size()));
    }

    private void ensureLoaded() {
        int generation = GENERATION.get();
        if (this.loadedGeneration == generation) {
            return;
        }
        this.textures.clear();
        this.aetherTextures.clear();
        Map<ResourceLocation, ?> resources = Minecraft.getInstance().getResourceManager()
                .listResources("textures/entity/" + this.folder, loc -> loc.getPath().endsWith(".png"));
        for (ResourceLocation loc : resources.keySet()) {
            if (loc.getPath().endsWith("_aether.png")) {
                this.aetherTextures.add(loc);
            } else {
                this.textures.add(loc);
            }
        }
        this.textures.sort(ResourceLocation::compareTo);
        this.aetherTextures.sort(ResourceLocation::compareTo);
        this.loadedGeneration = generation;
    }
}
