package com.solarbiscuit.ratlib.client.config;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Function;

public interface SolConfigEntries {
    void header(Component title);

    void hint(Component hint);

    void toggle(Component label, ForgeConfigSpec.BooleanValue value, ForgeConfigSpec spec);

    <E extends Enum<E>> void enumCycle(Component label, ForgeConfigSpec.EnumValue<E> value,
                                       Function<E, Component> names, ForgeConfigSpec spec);

    void intSlider(Component label, ForgeConfigSpec.IntValue value, int min, int max, ForgeConfigSpec spec);

    void doubleSlider(Component label, ForgeConfigSpec.DoubleValue value, double min, double max,
                      int decimals, ForgeConfigSpec spec);

    void percentSlider(Component label, ForgeConfigSpec.DoubleValue value, ForgeConfigSpec spec);
}
