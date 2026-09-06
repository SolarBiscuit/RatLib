package com.solarbiscuit.ratlib.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Default {@link NeutralMob} methods that delegate to a {@link PersistentAngerStore} field.
 */
public interface DelegatingNeutralMob extends NeutralMob {
    PersistentAngerStore ratlibAnger();

    @Override
    default void startPersistentAngerTimer() {
        ratlibAnger().start(((Mob) this).getRandom());
    }

    @Override
    default void setRemainingPersistentAngerTime(int time) {
        ratlibAnger().setRemainingTime(time);
    }

    @Override
    default int getRemainingPersistentAngerTime() {
        return ratlibAnger().remainingTime();
    }

    @Override
    default void setPersistentAngerTarget(@Nullable UUID target) {
        ratlibAnger().setTarget(target);
    }

    @Nullable
    @Override
    default UUID getPersistentAngerTarget() {
        return ratlibAnger().target();
    }
}
