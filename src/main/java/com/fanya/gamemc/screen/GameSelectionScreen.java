package com.fanya.gamemc.screen;

import com.fanya.gamemc.GameMC;
import com.fanya.gamemc.minigames.MiniGame;
import com.fanya.gamemc.util.GameColorPalette;
import com.fanya.gamemc.util.VersionChecker;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GameSelectionScreen extends Screen {
    public static final Identifier GAME_MC_LOGO = Identifier.of(GameMC.MOD_ID, "textures/gui/game_mc.png");
    private static final List<MiniGame> GAME_BUTTONS = new ArrayList<>();

    public static void pushMiniGame(MiniGame game) {if (!GAME_BUTTONS.contains(game)) GAME_BUTTONS.add(game);}
    public static void popMiniGame(MiniGame game) {GAME_BUTTONS.remove(game);}

    private final Screen parent;
    private final List<GameWidget> games;
    private ButtonWidget backButton, randomGameButton;
    private ScrollbarWidget scrollbar;
    private double scrollingListY = 0;
    private double maxScrollingListY;
    private GameWidget selected = null;
    private GameWidget hovered = null;

    private final VersionChecker versionChecker = new VersionChecker();

    public GameSelectionScreen(Screen parent) {
        super(Text.translatable("menu.gamemc.select"));
        this.parent = parent;
        games = new ArrayList<>();

        for(MiniGame game : GAME_BUTTONS) {
            GameWidget gameWidget = new GameWidget(0,0,game);
            games.add(gameWidget);
        }
        versionChecker.fetchLatestVersionAsync();
    }

    @Override
    protected void init() {
        super.init();
        scrollingListY = 0;

        //addDrawableChild(GameButtonWidget.createButton(0,0,10,10, Text.of("o"), (b) -> client.setScreen(new GameSelectionScreen_old(this))));
        backButton = GameButtonWidget.createButton(0,0, ScreenTexts.BACK, (b) -> client.setScreen(parent));
        randomGameButton = GameButtonWidget.createButton(0,0, Text.translatable("menu.gamemc.random"),
                (b) -> games.get(Random.create().nextBetweenExclusive(0, games.size())).onPress(null));
        scrollbar = new ScrollbarWidget(0,0, 8, 101, 100, 0, GameColorPalette.BACK_PANEL, GameColorPalette.BACK_PANEL_HOVER_COLOR);
        addSelectableChild(backButton);
        addSelectableChild(randomGameButton);
        addSelectableChild(scrollbar);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        hovered = null;

        int scrollingSpeed = 20;
        int spacingX = 10,
                spacingY = 20;

        int borderSpacingX = 10;
        int borderSpacingY = 140;
        int listStartY = borderSpacingY + (int) scrollingListY* scrollingSpeed;
        int countInLien = (width-GameButtonWidget.btnWidth-spacingX-borderSpacingX*2) / (GameWidget.PANEL_WIDTH + spacingX);
        if (countInLien > games.size()) countInLien = games.size();
        borderSpacingX = (width-GameButtonWidget.btnWidth-spacingX - countInLien*(GameWidget.PANEL_WIDTH+spacingX))/2;
        int liensCount = 0;
        int columnCount = 0;
        int startX = borderSpacingX+GameButtonWidget.btnWidth+spacingX;

        // Logo
        int textureWidth = 443,
                textureHeight = 130;
        double scaleFactor = 0.7;
        int logoX = (width - startX - borderSpacingX - (int) (textureWidth*scaleFactor)) / 2,
                logoY = listStartY - (int) (textureHeight*scaleFactor) - spacingY;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, GAME_MC_LOGO, startX+logoX, logoY, 0, 0, (int) (textureWidth*scaleFactor), (int) (textureHeight*scaleFactor), (int) (textureWidth*scaleFactor), (int) (textureHeight*scaleFactor));


        // Game list
        for(GameWidget game : games) {
            game.setPosition(startX + (spacingX + GameWidget.PANEL_WIDTH) * liensCount, listStartY + (spacingY + GameWidget.PANEL_HEIGHT) * columnCount);
            if(game.isOnScreen(width,height)) {
                game.setSelected(selected == game);
                if (game.isHovered()) hovered = game;
                game.render(context, mouseX, mouseY, deltaTicks);
            }
            if(++liensCount >= countInLien) {
                columnCount++;
                liensCount = 0;
            }
        }

        maxScrollingListY = (double) ((columnCount + 1) * (GameWidget.PANEL_HEIGHT + spacingY) + borderSpacingY - height) / scrollingSpeed;

        // scrollbar
        if(maxScrollingListY*scrollingSpeed > height/4) {
            int scrollbarSpacingY = 10;
            int scrollbarWidth = scrollbar.getWidth();
            int scrollbarHeight = height-scrollbarSpacingY*2;
            int scrollbarStartX = width - scrollbarWidth - (borderSpacingX + scrollbarWidth) / 2;

            scrollbar.setMaxValue(maxScrollingListY);
            scrollingListY = -scrollbar.getValue();
            scrollbar.setDimensionsAndPosition(scrollbarWidth, scrollbarHeight, scrollbarStartX, scrollbarSpacingY);
            scrollbar.render(context, mouseX, mouseY, deltaTicks);
        }


        // buttons
        int buttonSpacingY = 1;
        int buttonsY = height-GameButtonWidget.btnHeight*3-buttonSpacingY;
        int buttonsX = (borderSpacingX+spacingX)/2;

        randomGameButton.setPosition(buttonsX, buttonsY);
        randomGameButton.render(context, mouseX, mouseY, deltaTicks);
        backButton.setPosition(buttonsX, buttonsY+buttonSpacingY+GameButtonWidget.btnHeight);
        backButton.render(context, mouseX, mouseY, deltaTicks);

        // Info panel TODO: text scrolling maybe
        int infoPanelStartX = buttonsX;
        int infoPanelStartY = spacingY;
        int infoPanelEndX = buttonsX + GameButtonWidget.btnWidth;
        int infoPanelEndY = buttonsY-spacingY;

        Text text;
        if (selected != null) text = selected.getMiniGame().getDescription();
        else if (hovered != null) text = hovered.getMiniGame().getDescription();
        else text = Text.translatable("menu.gamemc.description");

        GameScreen.renderGround(context, infoPanelStartX, infoPanelStartY, infoPanelEndX,infoPanelEndY);
        drawWrappedText(context, textRenderer, StringVisitable.plain(text.getString()),
                infoPanelStartX+1, infoPanelStartY, infoPanelEndX-infoPanelStartX, infoPanelEndY-infoPanelStartY, 0xFFFFFFFF,false);

        // Version check
        if (versionChecker.isReady() && versionChecker.isUpdateAvailable()) {
            String latest = versionChecker.extractModVersion(versionChecker.getLatestVersion());

            int updateAlertX = this.width / 2 - this.textRenderer.getWidth(Text.translatable("menu.gamemc.gui.update", latest)) / 2;
            int updateAlertY = listStartY - this.textRenderer.fontHeight*2;

            context.drawText(this.textRenderer, Text.translatable("menu.gamemc.gui.update", latest), updateAlertX, updateAlertY, GameColorPalette.UPDATE_ALERT_COLOR, true);
        }
    }

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

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        for(GameWidget game : games) {
            if (game.isHoveredPlayButton()) {
                game.onPress(click);
                return true;
            }
            if(game.isHovered()) {
                selected = selected == game ? null : game;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollingListY = Math.min(0, Math.max(scrollingListY+verticalAmount, -maxScrollingListY));
        scrollbar.setValue(-scrollingListY);
        return true;
    }

    private void drawWrappedText(DrawContext context, TextRenderer textRenderer, StringVisitable text, int x, int y, int width, int height, int color, boolean shadow) {
        int startY = y;
        for (OrderedText orderedText : textRenderer.wrapLines(text, width)) {
            if(y-startY+textRenderer.fontHeight > height) return;
            context.drawText(textRenderer, orderedText, x, y, color, shadow);
            y += 9;
        }
    }
}