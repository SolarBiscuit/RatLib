package com.solarbiscuit.ratlib.client.skin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.util.function.Function;

public class PlayerModelRenderer<T extends Mob> extends HumanoidMobRenderer<T, HumanoidModel<T>> {
    private final Function<T, ResourceLocation> texture;

    public PlayerModelRenderer(EntityRendererProvider.Context context, boolean armor,
                               Function<T, ResourceLocation> texture) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.texture = texture;
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        if (armor) {
            this.addLayer(new HumanoidArmorLayer<>(this,
                    new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                    new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                    context.getModelManager()));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture.apply(entity);
    }
}
