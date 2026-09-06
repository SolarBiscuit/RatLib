package com.solarbiscuit.ratlib.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Field store for {@link NeutralMob} persistent anger. Pair with {@link DelegatingNeutralMob}.
 */
public final class PersistentAngerStore {
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    private int remainingTime;
    @Nullable
    private UUID target;

    public void start(RandomSource random) {
        this.remainingTime = PERSISTENT_ANGER_TIME.sample(random);
    }

    public int remainingTime() {
        return this.remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    @Nullable
    public UUID target() {
        return this.target;
    }

    public void setTarget(@Nullable UUID target) {
        this.target = target;
    }
}
