package com.solarbiscuit.ratlib.mixin;

import com.solarbiscuit.ratlib.recipe.RecipePreference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;
import java.util.List;
import java.util.SortedSet;

/**
 * Polymorph defaults to the first matching recipe sorted by output item id, which is arbitrary for
 * our purposes (e.g. silver -> copper sorts before silver -> gold). Prefer recipes registered with
 * {@link RecipePreference} instead. Skipped entirely when Polymorph is not installed.
 */
@Pseudo
@Mixin(targets = "com.illusivesoulworks.polymorph.common.capability.AbstractRecipeData", remap = false)
public class PolymorphRecipeDataMixin {
    private static Method ratlib$getResourceLocation;

    @Redirect(method = "getRecipe", at = @At(value = "INVOKE", target = "Ljava/util/SortedSet;first()Ljava/lang/Object;"),
            require = 0, remap = false)
    private Object ratlib$preferRegisteredRecipe(SortedSet<Object> pairs, RecipeType<?> type, Container container,
                                                 Level level, List<?> recipes) {
        Object first = pairs.first();
        if (!RecipePreference.hasAny() || level == null) {
            return first;
        }
        for (Object pair : pairs) {
            ResourceLocation id = ratlib$idOf(pair);
            if (id != null && level.getRecipeManager().byKey(id).filter(RecipePreference::isPreferred).isPresent()) {
                return pair;
            }
        }
        return first;
    }

    private static ResourceLocation ratlib$idOf(Object pair) {
        try {
            if (ratlib$getResourceLocation == null) {
                ratlib$getResourceLocation = Class.forName("com.illusivesoulworks.polymorph.api.common.base.IRecipePair")
                        .getMethod("getResourceLocation");
            }
            return (ResourceLocation) ratlib$getResourceLocation.invoke(pair);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return null;
        }
    }
}
