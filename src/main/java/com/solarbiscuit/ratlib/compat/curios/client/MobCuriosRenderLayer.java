package com.solarbiscuit.ratlib.compat.curios.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.solarbiscuit.ratlib.compat.Mods;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Draws a mob's worn Curios with each item's registered {@link ICurioRenderer} (Curios only adds its
 * own layer to players). Touches Curios API types; only construct when Curios is loaded.
 */
public class MobCuriosRenderLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final String SIMPLE_HATS = "simplehats";

    private final RenderLayerParent<T, M> parent;

    public MobCuriosRenderLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
        this.parent = renderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        CuriosApi.getCuriosInventory(entity).ifPresent(handler -> {
            for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                ICurioStacksHandler stacksHandler = entry.getValue();
                IDynamicStackHandler stacks = stacksHandler.getStacks();
                IDynamicStackHandler cosmetics = stacksHandler.getCosmeticStacks();
                List<Boolean> renders = stacksHandler.getRenders();
                for (int i = 0; i < stacks.getSlots(); i++) {
                    if (i < renders.size() && !renders.get(i)) {
                        continue;
                    }
                    ItemStack stack = i < cosmetics.getSlots() ? cosmetics.getStackInSlot(i) : ItemStack.EMPTY;
                    if (stack.isEmpty()) {
                        stack = stacks.getStackInSlot(i);
                    }
                    if (stack.isEmpty()) {
                        continue;
                    }
                    renderCurio(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks,
                            ageInTicks, netHeadYaw, headPitch, entry.getKey(), i, stack);
                }
            }
        });
    }

    private void renderCurio(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                             float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                             float netHeadYaw, float headPitch, String slotId, int index, ItemStack stack) {
        // Simple Hats draws its own hats in head slots.
        if (Mods.loaded(SIMPLE_HATS) && isSimpleHats(stack) && ("head".equals(slotId) || "hat".equals(slotId))) {
            return;
        }
        Optional<ICurioRenderer> renderer = CuriosRendererRegistry.getRenderer(stack.getItem());
        if (renderer.isPresent()) {
            renderer.get().render(stack, new SlotContext(slotId, entity, index, false, true),
                    poseStack, this.parent, buffer, packedLight, limbSwing, limbSwingAmount,
                    partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }

    private static boolean isSimpleHats(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && SIMPLE_HATS.equals(id.getNamespace());
    }
}
