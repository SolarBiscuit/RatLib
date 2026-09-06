package com.solarbiscuit.ratlib.entity;

import com.solarbiscuit.ratlib.entity.crowd.CrowdPathing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Strike the attacker once, then run away.
 */
public class HitAndFleeGoal extends Goal {
    private final PathfinderMob mob;
    private LivingEntity attacker;
    private int attackTicks;
    private int fleeTicks;
    private int recast;
    private boolean struck;

    public HitAndFleeGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity hurtBy = this.mob.getLastHurtByMob();
        return hurtBy != null && hurtBy.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() && (this.attackTicks > 0 || this.fleeTicks > 0);
    }

    @Override
    public void start() {
        this.attacker = this.mob.getLastHurtByMob();
        this.attackTicks = 40;
        this.fleeTicks = 0;
        this.recast = 0;
        this.struck = false;
        this.mob.setTarget(this.attacker);
    }

    @Override
    public void stop() {
        this.mob.setTarget(null);
        this.attacker = null;
        this.attackTicks = 0;
        this.fleeTicks = 0;
        this.struck = false;
    }

    @Override
    public void tick() {
        LivingEntity target = this.attacker;
        if (target == null || !target.isAlive()) {
            return;
        }
        if (!this.struck && this.attackTicks > 0) {
            this.attackTicks--;
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (--this.recast <= 0) {
                this.recast = CrowdPathing.recastInterval(this.mob);
                this.mob.getNavigation().moveTo(target, 1.25D);
            }
            if (this.mob.distanceToSqr(target) <= 4.0D) {
                this.mob.doHurtTarget(target);
                this.struck = true;
                this.fleeTicks = 160;
                this.mob.setTarget(null);
            } else if (this.attackTicks <= 0) {
                this.struck = true;
                this.fleeTicks = 160;
                this.mob.setTarget(null);
            }
            return;
        }
        if (this.fleeTicks > 0) {
            this.fleeTicks--;
            if (this.mob.getNavigation().isDone()) {
                Vec3 away = DefaultRandomPos.getPosAway(this.mob, 16, 7, target.position());
                if (away != null) {
                    this.mob.getNavigation().moveTo(away.x, away.y, away.z, 1.4D);
                }
            }
        }
    }
}
