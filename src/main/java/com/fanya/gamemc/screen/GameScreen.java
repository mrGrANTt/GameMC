package com.fanya.gamemc.screen;

import com.fanya.gamemc.util.GameColorPalette;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public abstract class GameScreen extends Screen {
    public static void renderGround(DrawContext context, int x1, int y1, int x2, int y2, boolean focused) {
        context.fill(x1, y1, x2, y2, focused ? GameColorPalette.BACK_PANEL_HOVER_COLOR : GameColorPalette.BACK_PANEL);
    }
    public static void renderGround(DrawContext context, int x1, int y1, int x2, int y2) {
        renderGround(context, x1, y1, x2, y2, false);
    }

    protected Screen parent;
    private final List<GameButtonWidget> upButtons;

    protected GameScreen(Text title, Screen parent) {
        super(title);
        this.parent = parent;
        upButtons = new ArrayList<>();

        addUpButton(ScreenTexts.BACK, b -> {
            if (client != null) client.setScreen(parent);
        });
        addUpButton(Text.translatable("menu.gamemc.info.new_game"), b -> reset());
    }

    protected GameButtonWidget addUpButton(Text text, ButtonWidget.PressAction onPress) {
        GameButtonWidget btn = new GameButtonWidget(text, onPress);
        upButtons.add(btn);
        return btn;
    }

    public int getTotalUpBtnWidth() {return GameButtonWidget.btnWidth * upButtons.size() + GameButtonWidget.spacingBtn * (upButtons.size()-1);}
    public int getTotalUpBtnHeight() {return GameButtonWidget.btnHeight;}

    // render up buttons
    protected void generateDefaultElement() {
        int totalBtnWidth = getTotalUpBtnWidth();
        int startX = (this.width - totalBtnWidth) / 2;

        for(GameButtonWidget btn : upButtons) {
            addDrawableChild(btn.build(startX, GameButtonWidget.btnY, GameButtonWidget.btnWidth, GameButtonWidget.btnHeight));
            startX += GameButtonWidget.btnWidth + GameButtonWidget.spacingBtn;
        }
    }

    // R - reset; ESC - exit;
    @Override
    public boolean keyPressed(KeyInput input) {
        switch (input.key()) {
            case GLFW.GLFW_KEY_R -> {
                reset();
                return true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                if (client != null) {
                    client.setScreen(parent);
                    return true;
                }
            }
        }
        return super.keyPressed(input);
    }

    @Override
    protected void init() {
        super.init();
        generateDefaultElement();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean shouldPause() {
        return false; // игра не ставится на паузу при открытии меню
    }

    protected abstract void reset();// функйия перезапуска игры
}
