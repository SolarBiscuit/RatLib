package com.solarbiscuit.ratlib.client.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

final class SolConfigSlider extends AbstractSliderButton {
    private final DoubleFunction<Component> message;
    private final DoubleConsumer apply;

    private SolConfigSlider(int x, int y, int width, double normalized,
                            DoubleFunction<Component> message, DoubleConsumer apply) {
        super(x, y, width, 20, Component.empty(), normalized);
        this.message = message;
        this.apply = apply;
        this.updateMessage();
    }

    static SolConfigSlider ints(int x, int y, int width, Component label,
                                ForgeConfigSpec.IntValue value, int min, int max, ForgeConfigSpec spec) {
        int span = Math.max(1, max - min);
        double normalized = Mth.clamp((value.get() - min) / (double) span, 0.0D, 1.0D);
        return new SolConfigSlider(x, y, width, normalized,
                pos -> Component.translatable("options.generic_value", label, min + (int) Math.round(pos * span)),
                pos -> {
                    value.set(min + (int) Math.round(pos * span));
                    save(spec);
                });
    }

    static SolConfigSlider decimals(int x, int y, int width, Component label,
                                    ForgeConfigSpec.DoubleValue value, double min, double max,
                                    int decimals, ForgeConfigSpec spec) {
        double span = max - min;
        if (span <= 0.0D) {
            span = 1.0D;
        }
        double normalized = Mth.clamp((value.get() - min) / span, 0.0D, 1.0D);
        String format = "%." + Math.max(0, decimals) + "f";
        double finalSpan = span;
        return new SolConfigSlider(x, y, width, normalized,
                pos -> Component.translatable("options.generic_value", label,
                        String.format(Locale.ROOT, format, min + pos * finalSpan)),
                pos -> {
                    value.set(min + pos * finalSpan);
                    save(spec);
                });
    }

    static SolConfigSlider percent(int x, int y, int width, Component label,
                                   ForgeConfigSpec.DoubleValue value, ForgeConfigSpec spec) {
        double normalized = Mth.clamp(value.get(), 0.0D, 1.0D);
        return new SolConfigSlider(x, y, width, normalized,
                pos -> Component.translatable("options.percent_value", label, (int) Math.round(pos * 100.0D)),
                pos -> {
                    value.set(pos);
                    save(spec);
                });
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.message.apply(this.value));
    }

    @Override
    protected void applyValue() {
        this.apply.accept(this.value);
    }

    private static void save(ForgeConfigSpec spec) {
        if (spec.isLoaded()) {
            spec.save();
        }
    }
}
