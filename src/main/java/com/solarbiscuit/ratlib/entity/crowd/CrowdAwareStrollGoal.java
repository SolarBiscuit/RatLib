package com.solarbiscuit.ratlib.entity.crowd;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class CrowdAwareStrollGoal extends WaterAvoidingRandomStrollGoal {
    public CrowdAwareStrollGoal(PathfinderMob mob, double speedModifier) {
        super(mob, speedModifier);
    }

    @Override
    public boolean canUse() {
        return CrowdPathing.allowIdleStroll(this.mob) && super.canUse();
    }
}
