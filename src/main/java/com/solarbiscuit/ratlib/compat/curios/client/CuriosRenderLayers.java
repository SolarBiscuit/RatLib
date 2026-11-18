package com.solarbiscuit.ratlib.compat.curios.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

/** Adds a {@link MobCuriosRenderLayer}. Separate class so renderers don't load Curios types when it's absent. */
public final class CuriosRenderLayers {
    private CuriosRenderLayers() {}

    public static <T extends LivingEntity, M extends EntityModel<T>> void add(LivingEntityRenderer<T, M> renderer) {
        renderer.addLayer(new MobCuriosRenderLayer<>(renderer));
    }
}
