package com.solarbiscuit.ratlib.entity;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired from {@link TraderMob#mobInteract} before vanilla villager trading.
 * Content mods (RatRep) can set a result to handle the interaction.
 */
@Cancelable
public class TraderMobInteractEvent extends Event {
    private final TraderMob trader;
    private final Player player;
    private final InteractionHand hand;
    private InteractionResult interactionResult = InteractionResult.PASS;

    public TraderMobInteractEvent(TraderMob trader, Player player, InteractionHand hand) {
        this.trader = trader;
        this.player = player;
        this.hand = hand;
    }

    public TraderMob getTrader() {
        return this.trader;
    }

    public Player getPlayer() {
        return this.player;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public InteractionResult getInteractionResult() {
        return this.interactionResult;
    }

    public void setInteractionResult(InteractionResult result) {
        this.interactionResult = result == null ? InteractionResult.PASS : result;
        if (this.interactionResult != InteractionResult.PASS) {
            this.setCanceled(true);
        }
    }
}
