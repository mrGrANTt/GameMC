package com.fanya.gamemc.screen;

import com.fanya.gamemc.minigames.MiniGame;
import com.fanya.gamemc.util.VersionChecker;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class GameSelectionScreen_old extends Screen {
    private static final List<MiniGame> GAME_BUTTONS = new ArrayList<>();
    public static void pushMiniGame(MiniGame game) {if (!GAME_BUTTONS.contains(game)) GAME_BUTTONS.add(game);}
    public static void popMiniGame(MiniGame game) {GAME_BUTTONS.remove(game);}

    private final Screen parent;
    private final List<ButtonWidget> gameButtons = new ArrayList<>();
    private ButtonWidget backButton;
    private int scrollOffset = 0;

    private int panelX;
    private int panelY;
    private final int panelWidth = 350;
    private final int panelHeight = 220;
    private int listAreaX, listAreaY, listAreaWidth, listAreaHeight;

    private final int buttonWidth = 200;
    private final int buttonHeight = 20;
    private final int buttonGap = 8;

    private final VersionChecker versionChecker = new VersionChecker();

    public GameSelectionScreen_old(Screen parent) {
        super(Text.translatable("menu.gamemc.select"));
        this.parent = parent;
        for(MiniGame game : GAME_BUTTONS) {
            gameButtons.add(GameButtonWidget.createButton(0, 0, buttonWidth, buttonHeight,
                    game.getMiniGameTittle(),
                    button -> setScreenIfPresent(game.getMiniGameScreen(this))
            ));
        }
    }

    @Override
    protected void init() {
        super.init();

        versionChecker.fetchLatestVersionAsync();

        panelX = this.width / 2 - panelWidth / 2;
        panelY = this.height / 2 - panelHeight / 2;

        listAreaX = panelX + 16;
        listAreaY = panelY + 58;
        listAreaWidth = panelWidth - 32;

        listAreaHeight = panelHeight - 76 - buttonHeight - 12;

        int backBtnX = this.width / 2 - buttonWidth / 2;
        int backBtnY = panelY + panelHeight - 18 - buttonHeight;
        backButton = GameButtonWidget.createButton(backBtnX, backBtnY, buttonWidth, buttonHeight ,ScreenTexts.BACK,
                button -> setScreenIfPresent(this.parent));
    }

    private void setScreenIfPresent(Screen scr) {
        if (this.client != null)
            this.client.setScreen(scr);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderPanoramaBackground(context, delta);

        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);

        context.fill(panelX + 4, panelY + 4, panelX + panelWidth + 4, panelY + panelHeight + 4, 0x80000000);
        context.fillGradient(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE0101010, 0xE0202020);

        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFF1a8c99);
        context.fill(panelX, panelY + panelHeight - 2, panelX + panelWidth, panelY + panelHeight, 0xFF1a8c99);
        context.fill(panelX, panelY, panelX + 2, panelY + panelHeight, 0xFF1a8c99);
        context.fill(panelX + panelWidth - 2, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF1a8c99);

        context.fill(panelX + 40, panelY + 45, panelX + panelWidth - 40, panelY + 47, 0x60FFFFFF);

        String title = this.textRenderer.trimToWidth(Text.translatable("menu.gamemc.title").getString(), panelWidth);
        context.drawText(this.textRenderer, Text.literal(title),
                this.width / 2 - this.textRenderer.getWidth(title) / 2, panelY + 15, 0xFF00FFFF, true);

        String desc = this.textRenderer.trimToWidth(Text.translatable("menu.gamemc.description").getString(), panelWidth);
        context.drawText(this.textRenderer, Text.literal(desc),
                this.width / 2 - this.textRenderer.getWidth(desc) / 2, panelY + 30, 0xFFAAAAAA, true);


        if (versionChecker.isReady() && versionChecker.isUpdateAvailable()) {
            String latest = versionChecker.extractModVersion(versionChecker.getLatestVersion());

            int x = this.width / 2 - this.textRenderer.getWidth(Text.translatable("menu.gamemc.gui.update", latest)) / 2;
            int y = panelY - 12;

            context.drawText(
                    this.textRenderer,
                    Text.translatable("menu.gamemc.gui.update", latest),
                    x,
                    y,
                    0xFFFFA500,
                    true
            );
        }


        MinecraftClient mc = MinecraftClient.getInstance();
        double sf = mc.getWindow().getScaleFactor();
        int scX = (int) (listAreaX * sf);
        int scY = (int) (mc.getWindow().getHeight() - (listAreaY + listAreaHeight) * sf);
        int scW = (int) (listAreaWidth * sf);
        int scH = (int) (listAreaHeight * sf);
        RenderSystem.enableScissorForRenderTypeDraws(scX, scY, scW, scH);

        int y = listAreaY - scrollOffset;
        for (ButtonWidget btn : gameButtons) {
            btn.setX(this.width / 2 - buttonWidth / 2);
            btn.setY(y);
            if (y + buttonHeight > listAreaY && y < listAreaY + listAreaHeight) {
                btn.render(context, mouseX, mouseY, delta);
            }
            y += buttonHeight + buttonGap;
        }
        RenderSystem.disableScissorForRenderTypeDraws();

        backButton.render(context, mouseX, mouseY, delta);

        // скроллбар
        int totalBtnsHeight = (buttonHeight + buttonGap) * gameButtons.size();
        if (totalBtnsHeight > listAreaHeight) {
            int barX = listAreaX + listAreaWidth + 2;
            int barY = listAreaY;
            int barWidth = 6;
            int barHeight = listAreaHeight;
            context.fill(barX, barY, barX + barWidth, barY + barHeight, 0x40111111);

            int handleHeight = Math.max(16, (int) ((float) barHeight * barHeight / totalBtnsHeight));
            int handleY = (int) (barY + ((float) scrollOffset / (totalBtnsHeight - listAreaHeight)) * (barHeight - handleHeight));
            context.fill(barX, handleY, barX + barWidth, handleY + handleHeight, 0xFF888888);
        }
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int totalBtnsHeight = (buttonHeight + buttonGap) * gameButtons.size();
        int maxScroll = Math.max(0, totalBtnsHeight - listAreaHeight);
        if (mouseX > listAreaX && mouseX < listAreaX + listAreaWidth &&
                mouseY > listAreaY && mouseY < listAreaY + listAreaHeight && maxScroll > 0) {
            scrollOffset = clamp(scrollOffset - (int) (verticalAmount * 20), maxScroll);
            return true;
        }
        return false;
    }

    private int clamp(int val, int max) {
        return Math.max(0, Math.min(max, val));
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int y = listAreaY - scrollOffset;
        for (ButtonWidget btn : gameButtons) {
            if (y + buttonHeight > listAreaY && y < listAreaY + listAreaHeight) {
                if (btn.mouseClicked(click,doubled)) return true;
            }
            y += buttonHeight + buttonGap;
        }

        if (backButton.mouseClicked(click,doubled)) return true;

        return super.mouseClicked(click,doubled);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (client != null) {
                client.setScreen(parent);
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}