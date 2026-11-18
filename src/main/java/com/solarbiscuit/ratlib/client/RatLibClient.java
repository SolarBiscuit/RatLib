package com.solarbiscuit.ratlib.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.solarbiscuit.ratlib.RatLib;
import com.solarbiscuit.ratlib.client.config.SolConfigScreen;
import com.solarbiscuit.ratlib.client.skin.HumanoidSkinTextures;
import com.solarbiscuit.ratlib.client.skin.SkinArms;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = RatLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RatLibClient {
    public static final KeyMapping OPEN_CONFIG = new KeyMapping(
            "key.ratlib.open_config",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.categories.solconfig");

    private RatLibClient() {}

    @SubscribeEvent
    @SuppressWarnings("removal")
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> SolConfigScreen.open(parent)));
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG);
    }

    /** Drops cached skin lookups on resource reload so added/removed resource-pack skins are picked up. */
    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            SkinArms.clear();
            HumanoidSkinTextures.invalidateAll();
        });
    }

    @Mod.EventBusSubscriber(modid = RatLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeClient {
        private ForgeClient() {}

        @SubscribeEvent
        public static void onKey(InputEvent.Key event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) {
                return;
            }
            while (OPEN_CONFIG.consumeClick()) {
                mc.setScreen(SolConfigScreen.open(null));
            }
        }
    }
}
