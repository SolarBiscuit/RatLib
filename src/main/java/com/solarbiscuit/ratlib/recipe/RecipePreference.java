package com.solarbiscuit.ratlib.recipe;

import net.minecraft.world.item.crafting.Recipe;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Marks recipes that should win by default when several recipes match the same grid.
 * Honoured by Polymorph's default pick (the player can still choose another recipe there).
 */
public final class RecipePreference {
    private static final List<Predicate<Recipe<?>>> PREFERRED = new CopyOnWriteArrayList<>();

    private RecipePreference() {}

    public static void register(Predicate<Recipe<?>> preferred) {
        PREFERRED.add(preferred);
    }

    public static boolean isPreferred(Recipe<?> recipe) {
        for (Predicate<Recipe<?>> predicate : PREFERRED) {
            if (predicate.test(recipe)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAny() {
        return !PREFERRED.isEmpty();
    }
}
