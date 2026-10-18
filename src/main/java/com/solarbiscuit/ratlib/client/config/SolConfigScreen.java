package com.solarbiscuit.ratlib.client.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class SolConfigScreen extends Screen {
    private static final int FOOTER_HEIGHT = 36;
    @SuppressWarnings("removal")
    private static final ResourceLocation FOOTER_SEPARATOR =
            new ResourceLocation("minecraft", "textures/gui/footer_separator.png");

    @Nullable
    private final Screen last;
    @Nullable
    private final String focusTabId;
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    @Nullable
    private TabNavigationBar tabNavigationBar;
    @Nullable
    private Button doneButton;
    @Nullable
    private SolConfigOptionList list;

    public SolConfigScreen(@Nullable Screen last) {
        this(last, null);
    }

    public SolConfigScreen(@Nullable Screen last, @Nullable String focusTabId) {
        super(Component.translatable("screen.solconfig.title"));
        this.last = last;
        this.focusTabId = focusTabId;
    }

    public static SolConfigScreen open(@Nullable Screen last) {
        return new SolConfigScreen(last);
    }

    public static SolConfigScreen open(@Nullable Screen last, @Nullable String focusTabId) {
        return new SolConfigScreen(last, focusTabId);
    }

    @Override
    protected void init() {
        List<SolConfigTab> tabs = SolConfigTabs.all();
        this.doneButton = Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build();
        this.doneButton.setTabOrderGroup(1);
        this.addRenderableWidget(this.doneButton);

        if (!tabs.isEmpty()) {
            HubTab[] hubTabs = new HubTab[tabs.size()];
            int selected = 0;
            for (int i = 0; i < tabs.size(); i++) {
                SolConfigTab tab = tabs.get(i);
                hubTabs[i] = new HubTab(tab);
                if (tab.id().equals(this.focusTabId)) {
                    selected = i;
                }
            }
            this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width)
                    .addTabs(hubTabs)
                    .build();
            this.addRenderableWidget(this.tabNavigationBar);
            this.tabNavigationBar.selectTab(selected, false);
        }

        this.repositionElements();
    }

    @Override
    public void repositionElements() {
        if (this.doneButton == null) {
            return;
        }
        if (this.tabNavigationBar != null) {
            this.tabNavigationBar.setWidth(this.width);
            this.tabNavigationBar.arrangeElements();
        }
        this.doneButton.setPosition(this.width / 2 - 100, this.height - 27);
        int top = this.tabNavigationBar != null ? this.tabNavigationBar.getRectangle().bottom() : 32;
        int bottom = this.height - FOOTER_HEIGHT;
        ScreenRectangle area = new ScreenRectangle(0, top, this.width, Math.max(0, bottom - top));
        this.tabManager.setTabArea(area);
        if (this.tabNavigationBar == null) {
            this.bindList(null, area);
        }
    }

    @Override
    public void tick() {
        this.tabManager.tickCurrent();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.tabNavigationBar != null && this.tabNavigationBar.keyPressed(keyCode)
                || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        if (this.list != null) {
            this.list.render(graphics, mouseX, mouseY, partialTick);
        }
        graphics.blit(FOOTER_SEPARATOR, 0, Mth.roundToward(this.height - FOOTER_HEIGHT - 2, 2),
                0.0F, 0.0F, this.width, 2, 32, 2);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.tabNavigationBar == null) {
            graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
            graphics.drawCenteredString(this.font, Component.translatable("screen.solconfig.empty"),
                    this.width / 2, this.height / 2 - 10, 0xA0A0A0);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.last);
    }

    private void bindList(@Nullable SolConfigTab tab, ScreenRectangle area) {
        if (this.list != null) {
            this.removeWidget(this.list);
            this.list = null;
        }
        int y0 = Math.max(area.top(), 24);
        int y1 = Math.max(y0 + 25, area.bottom());
        this.list = new SolConfigOptionList(this.minecraft, this.width, this.height, y0, y1);
        if (tab != null) {
            tab.contents().addEntries(this.list);
        }
        this.addWidget(this.list);
    }

    private final class HubTab implements Tab {
        private final SolConfigTab spec;

        private HubTab(SolConfigTab spec) {
            this.spec = spec;
        }

        @Override
        public Component getTabTitle() {
            return this.spec.title();
        }

        @Override
        public void visitChildren(Consumer<AbstractWidget> consumer) {
        }

        @Override
        public void doLayout(ScreenRectangle rectangle) {
            SolConfigScreen.this.bindList(this.spec, rectangle);
        }
    }
}
