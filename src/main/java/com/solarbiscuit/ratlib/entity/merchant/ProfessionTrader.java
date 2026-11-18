package com.solarbiscuit.ratlib.entity.merchant;

import com.solarbiscuit.ratlib.entity.TraderMob;
import com.solarbiscuit.ratlib.entity.crowd.CrowdAwareStrollGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * A {@link TraderMob} that lives in a village like a villager: it has a profession (usually from
 * {@link ExclusiveProfessions}), strolls by day within {@link #homeRadius()} of where it spawned, heads back there
 * at night, opens doors, flees zombies and illagers, and restocks its trades every {@link #restockInterval()} ticks.
 * Subclasses fill {@link #updateTrades()}.
 */
public abstract class ProfessionTrader extends TraderMob {
    private static final double WALK_SPEED = 0.5D;
    private static final double FLEE_SPEED = 0.75D;

    @Nullable
    private BlockPos home;
    private int restockTimer;

    protected ProfessionTrader(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
        if (this.getNavigation() instanceof GroundPathNavigation navigation) {
            navigation.setCanOpenDoors(true);
        }
    }

    public abstract VillagerProfession getProfession();

    public Component getProfessionName() {
        return ExclusiveProfessions.displayName(getProfession());
    }

    /** How far from home this trader strolls by day. */
    protected int homeRadius() {
        return 32;
    }

    /** Ticks between restocks; villagers restock about twice a day. */
    protected int restockInterval() {
        return 12000;
    }

    @Nullable
    public BlockPos getHome() {
        return this.home;
    }

    public void setHome(@Nullable BlockPos pos) {
        this.home = pos == null ? null : pos.immutable();
        if (this.home == null) {
            this.clearRestriction();
        } else {
            this.restrictTo(this.home, homeRadius());
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, WALK_SPEED, FLEE_SPEED));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Evoker.class, 12.0F, WALK_SPEED, FLEE_SPEED));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vindicator.class, 8.0F, WALK_SPEED, FLEE_SPEED));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vex.class, 8.0F, WALK_SPEED, FLEE_SPEED));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Pillager.class, 15.0F, WALK_SPEED, FLEE_SPEED));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Illusioner.class, 12.0F, WALK_SPEED, FLEE_SPEED));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Ravager.class, 12.0F, WALK_SPEED, FLEE_SPEED));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zoglin.class, 10.0F, WALK_SPEED, FLEE_SPEED));
        this.goalSelector.addGoal(1, new PanicGoal(this, FLEE_SPEED));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new GoHomeAtNightGoal(this));
        this.goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, WALK_SPEED));
        this.goalSelector.addGoal(6, new CrowdAwareStrollGoal(this, WALK_SPEED) {
            @Override
            public boolean canUse() {
                return !ProfessionTrader.this.level().isNight() && super.canUse();
            }
        });
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        if (this.home == null) {
            setHome(this.blockPosition());
        }
        return data;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.isTrading()) {
            return;
        }
        if (++this.restockTimer >= restockInterval()) {
            this.restockTimer = 0;
            restock();
        }
    }

    /** Refills every trade, as a villager does at its job site. */
    public void restock() {
        boolean refilled = false;
        for (MerchantOffer offer : this.getOffers()) {
            if (offer.getUses() > 0) {
                offer.resetUses();
                refilled = true;
            }
        }
        SoundEvent work = getProfession().workSound();
        if (refilled && work != null) {
            this.playSound(work, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.home != null) {
            tag.put("Home", NbtUtils.writeBlockPos(this.home));
        }
        tag.putInt("RestockTimer", this.restockTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setHome(tag.contains("Home") ? NbtUtils.readBlockPos(tag.getCompound("Home")) : null);
        this.restockTimer = tag.getInt("RestockTimer");
    }

    /** Walks back home when night falls, then stays near it until morning (strolling stops at night). */
    static final class GoHomeAtNightGoal extends Goal {
        private static final double ARRIVED_SQR = 9.0D;
        private final ProfessionTrader trader;
        private int recalc;

        GoHomeAtNightGoal(ProfessionTrader trader) {
            this.trader = trader;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            BlockPos home = this.trader.getHome();
            return home != null && this.trader.level().isNight()
                    && this.trader.distanceToSqr(home.getCenter()) > ARRIVED_SQR;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() && !this.trader.isTrading();
        }

        @Override
        public void start() {
            this.recalc = 0;
        }

        @Override
        public void tick() {
            if (--this.recalc > 0) {
                return;
            }
            this.recalc = adjustedTickDelay(40);
            BlockPos home = this.trader.getHome();
            if (home != null) {
                this.trader.getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, WALK_SPEED);
            }
        }

        @Override
        public void stop() {
            this.trader.getNavigation().stop();
        }
    }
}
