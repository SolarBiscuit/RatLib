package com.solarbiscuit.ratlib.client.skin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads whether a 64x64 player-format skin has slim (3px) arms. Cached per texture.
 */
public final class SkinArms {
    private static final Map<ResourceLocation, Boolean> CACHE = new ConcurrentHashMap<>();

    private SkinArms() {}

    /** Drops cached results so resource-pack skin changes are re-read after a reload. */
    public static void clear() {
        CACHE.clear();
    }

    public static boolean isSlim(ResourceLocation texture) {
        return CACHE.computeIfAbsent(texture, SkinArms::read);
    }

    /** Slim skins leave the 4th right-arm column (x 54-55, y 20-31) transparent. */
    private static boolean read(ResourceLocation texture) {
        var resource = Minecraft.getInstance().getResourceManager().getResource(texture);
        if (resource.isEmpty()) {
            return true;
        }
        try (InputStream in = resource.get().open(); NativeImage image = NativeImage.read(in)) {
            if (image.getWidth() != 64 || image.getHeight() != 64) {
                return true;
            }
            for (int x = 54; x <= 55; x++) {
                for (int y = 20; y < 32; y++) {
                    if ((image.getPixelRGBA(x, y) >>> 24) != 0) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
