package com.fanya.gamemc.minigames.snake;

import com.fanya.gamemc.minigames.MiniGame;
import com.fanya.gamemc.screen.GameButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SnakeSizeSelectScreen extends Screen  {
    public static class SnakeMiniGame implements MiniGame {
        @Override
        public Text getMiniGameTittle() {
            return Text.translatable("game.snake.title");
        }

        @Override
        public Screen getMiniGameScreen(Screen parent) {
            return new SnakeSizeSelectScreen(parent);
        }

        @Override
        public Identifier getIcon() {
            return Identifier.ofVanilla("textures/block/slime_block.png");
        }

        @Override
        public Text getDescription() {
            return Text.translatable("game.snake.description");
        }
    }

    private final Screen parent;

    public SnakeSizeSelectScreen(Screen parent) {
        super(Text.translatable("game.snake.size_select.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 160;
        int buttonHeight = 20;
        int spacing = 8;

        int startY = this.height / 2 - (buttonHeight * 3 + spacing * 2) / 2;

        this.addDrawableChild(GameButtonWidget.createButton(this.width / 2 - buttonWidth / 2, startY, buttonWidth, buttonHeight,
                Text.translatable("game.snake.button.small"),
                button -> openSnakeGame(15, 10)));

        this.addDrawableChild(GameButtonWidget.createButton(this.width / 2 - buttonWidth / 2, startY + (buttonHeight + spacing), buttonWidth, buttonHeight,
                Text.translatable("game.snake.button.medium"),
                button -> openSnakeGame(25, 18)));

        this.addDrawableChild(GameButtonWidget.createButton(this.width / 2 - buttonWidth / 2, startY + 2*(buttonHeight + spacing), buttonWidth, buttonHeight,
                Text.translatable("game.snake.button.big"),
                button -> openSnakeGame(35, 25)));

        this.addDrawableChild(GameButtonWidget.createButton(this.width / 2 - buttonWidth / 2, startY + (buttonHeight + spacing) * 3 + 10, buttonWidth, buttonHeight,
                ScreenTexts.BACK,
                button -> this.client.setScreen(parent)));
    }

    private void openSnakeGame(int width, int height) {
        SnakeGameScreen.selectedGridWidth = width;
        SnakeGameScreen.selectedGridHeight = height;
        this.client.setScreen(new SnakeGameScreen(parent, width, height));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.translatable("game.snake.size_select.title"),
                this.width / 2,
                this.height / 2 - 60,
                0xFFFFFFFF
        );
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
}