package com.solarbiscuit.ratlib.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

/**
 * Non-breeding, persistent merchant. Subclasses fill {@link #updateTrades()} and optionally equipment.
 * Reputation gating is applied by listeners of {@link TraderMobInteractEvent} (RatRep).
 */
public abstract class TraderMob extends AbstractVillager {
    protected TraderMob(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        EquipmentDrops.disableAll(this);
        this.setAge(0);
        this.setPersistenceRequired();
        afterFinalizeSpawn(level, difficulty, reason, data);
        return data;
    }

    protected void afterFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                      @Nullable SpawnGroupData spawnData) {
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        TraderMobInteractEvent event = new TraderMobInteractEvent(this, player, hand);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getInteractionResult() != InteractionResult.PASS) {
            return event.getInteractionResult();
        }
        InteractionResult trade = openTrades(player, hand);
        if (trade != InteractionResult.PASS) {
            return trade;
        }
        return super.mobInteract(player, hand);
    }

    /**
     * Opens the trade screen the way a wandering trader does. Runs when no {@link TraderMobInteractEvent}
     * listener handled the click, e.g. without RatRep or for a trader with no faction.
     */
    protected InteractionResult openTrades(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!this.isAlive() || this.isTrading() || player.isSecondaryUseActive()
                || held.is(Items.NAME_TAG) || held.getItem() instanceof SpawnEggItem) {
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide) {
            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(Stats.TALKED_TO_VILLAGER);
            }
            if (!this.getOffers().isEmpty()) {
                this.setTradingPlayer(player);
                this.openTradingScreen(player, this.getDisplayName(), 1);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, this.getSoundVolume(), this.getVoicePitch());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.EXPERIENCE_ORB_PICKUP;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean positive) {
        return positive ? SoundEvents.EXPERIENCE_ORB_PICKUP : SoundEvents.UI_BUTTON_CLICK.value();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }
}
