package com.solarbiscuit.ratlib.entity.crowd;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class CrowdAwareTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    public CrowdAwareTargetGoal(Mob mob, Class<T> targetType, int randomInterval, boolean mustSee, boolean mustReach,
                               @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, targetType, randomInterval, mustSee, mustReach, targetPredicate);
    }

    @Override
    public boolean canUse() {
        if (CrowdPathing.shouldDeferHeavyScan(this.mob)) {
            return false;
        }
        return super.canUse();
    }
}
