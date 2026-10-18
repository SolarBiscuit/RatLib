package com.solarbiscuit.ratlib.client.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.function.Function;

final class SolConfigOptionList extends ContainerObjectSelectionList<SolConfigOptionList.Entry>
        implements SolConfigEntries {
    private static final int BUTTON_WIDTH = 310;
    private static final int BUTTON_HEIGHT = 20;

    SolConfigOptionList(Minecraft minecraft, int width, int height, int y0, int y1) {
        super(minecraft, width, height, y0, y1, 25);
        this.centerListVertically = false;
        this.setRenderSelection(false);
    }

    @Override
    public int getRowWidth() {
        return BUTTON_WIDTH;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.width / 2 + 170;
    }

    private int buttonX() {
        return this.width / 2 - BUTTON_WIDTH / 2;
    }

    @Override
    public void header(Component title) {
        Font font = this.minecraft.font;
        StringWidget widget = new StringWidget(this.buttonX(), 0, BUTTON_WIDTH, BUTTON_HEIGHT, title, font)
                .alignCenter()
                .setColor(0xFFFFFF);
        this.addEntry(new Entry(List.of(widget)));
    }

    @Override
    public void hint(Component hint) {
        Font font = this.minecraft.font;
        StringWidget widget = new StringWidget(this.buttonX(), 0, BUTTON_WIDTH, BUTTON_HEIGHT, hint, font)
                .alignCenter()
                .setColor(0xA0A0A0);
        this.addEntry(new Entry(List.of(widget)));
    }

    @Override
    public void toggle(Component label, ForgeConfigSpec.BooleanValue value, ForgeConfigSpec spec) {
        this.addEntry(new Entry(List.of(CycleButton.onOffBuilder(value.get())
                .create(this.buttonX(), 0, BUTTON_WIDTH, BUTTON_HEIGHT, label, (button, selected) -> {
                    value.set(selected);
                    save(spec);
                }))));
    }

    @Override
    public <E extends Enum<E>> void enumCycle(Component label, ForgeConfigSpec.EnumValue<E> value,
                                              Function<E, Component> names, ForgeConfigSpec spec) {
        E current = value.get();
        this.addEntry(new Entry(List.of(CycleButton.builder(names)
                .withValues(List.of(current.getDeclaringClass().getEnumConstants()))
                .withInitialValue(current)
                .create(this.buttonX(), 0, BUTTON_WIDTH, BUTTON_HEIGHT, label, (button, selected) -> {
                    value.set(selected);
                    save(spec);
                }))));
    }

    @Override
    public void intSlider(Component label, ForgeConfigSpec.IntValue value, int min, int max, ForgeConfigSpec spec) {
        this.addEntry(new Entry(List.of(
                SolConfigSlider.ints(this.buttonX(), 0, BUTTON_WIDTH, label, value, min, max, spec))));
    }

    @Override
    public void doubleSlider(Component label, ForgeConfigSpec.DoubleValue value, double min, double max,
                             int decimals, ForgeConfigSpec spec) {
        this.addEntry(new Entry(List.of(
                SolConfigSlider.decimals(this.buttonX(), 0, BUTTON_WIDTH, label, value, min, max, decimals, spec))));
    }

    @Override
    public void percentSlider(Component label, ForgeConfigSpec.DoubleValue value, ForgeConfigSpec spec) {
        this.addEntry(new Entry(List.of(
                SolConfigSlider.percent(this.buttonX(), 0, BUTTON_WIDTH, label, value, spec))));
    }

    private static void save(ForgeConfigSpec spec) {
        if (spec.isLoaded()) {
            spec.save();
        }
    }

    static final class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final List<AbstractWidget> children;

        Entry(List<AbstractWidget> children) {
            this.children = children;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            for (AbstractWidget widget : this.children) {
                widget.setY(top);
                widget.render(graphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }
}
