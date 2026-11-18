package com.solarbiscuit.ratlib.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cross-mod targeting vetoes. A mod registers rules about its own mobs or items (e.g. "tamed mobs
 * leave my Thieves alone while their owner wears my necklace"), and mobs from other mods that call
 * {@link #mayTarget} respect them without either mod knowing about the other.
 */
public final class TargetRules {
    private static volatile List<TargetRule> rules = List.of();

    private TargetRules() {}

    @FunctionalInterface
    public interface TargetRule {
        /** True to stop {@code attacker} from choosing {@code target}. */
        boolean forbids(Mob attacker, LivingEntity target);
    }

    public static synchronized void register(TargetRule rule) {
        List<TargetRule> next = new ArrayList<>(rules);
        next.add(rule);
        rules = Collections.unmodifiableList(next);
    }

    public static boolean mayTarget(Mob attacker, LivingEntity target) {
        for (TargetRule rule : rules) {
            if (rule.forbids(attacker, target)) {
                return false;
            }
        }
        return true;
    }
}
