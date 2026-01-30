package com.fanya.gamemc.minigames._2048;

import com.fanya.gamemc.data.GameRecords;
import com.fanya.gamemc.minigames.MiniGame;
import com.fanya.gamemc.screen.GameScreen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class Game2048Screen extends GameScreen {
    public static class Game2048MiniGame implements MiniGame {
        @Override
        public Text getMiniGameTittle() {
            return Text.translatable("game.2048.title");
        }

        @Override
        public Screen getMiniGameScreen(Screen parent) {
            return new Game2048Screen(parent);
        }

        @Override
        public Identifier getIcon() {
            return BLOCK_TEXTURES[4];
        }

        @Override
        public Text getDescription() {
            return Text.translatable("game.2048.description");
        }
    }

    private static final Identifier[] BLOCK_TEXTURES = buildTextures();

    private static Identifier[] buildTextures() {
        Identifier[] ids = new Identifier[12];
        ids[1] = Identifier.ofVanilla("textures/block/dirt.png");
        ids[2] = Identifier.ofVanilla("textures/block/stone.png");
        ids[3] = Identifier.ofVanilla("textures/block/iron_ore.png");
        ids[4] = Identifier.ofVanilla("textures/block/gold_ore.png");
        ids[5] = Identifier.ofVanilla("textures/block/emerald_ore.png");
        ids[6] = Identifier.ofVanilla("textures/block/diamond_ore.png");
        ids[7] = Identifier.ofVanilla("textures/block/redstone_ore.png");
        ids[8] = Identifier.ofVanilla("textures/block/lapis_ore.png");
        ids[9] = Identifier.ofVanilla("textures/block/ancient_debris.png");
        ids[10] = Identifier.ofVanilla("textures/block/end_stone.png");
        ids[11] = Identifier.ofVanilla("textures/block/beacon.png");
        return ids;
    }

    private Game2048 game;

    private int playX, playY, cellSize, spacing, playWidth, playHeight;
    private int panelX;

    private int tickCounter = 0;

    public Game2048Screen(Screen parent) {
        super(Text.translatable("game.2048.title"), parent);
        game = new Game2048();

        addUpButton(Text.translatable("game.2048.info.pause"), b -> {
            if (game != null) game.togglePause();
            assert game != null;
            b.setMessage(game.getState() == Game2048.State.PAUSED
                    ? Text.translatable("game.2048.info.resume")
                    : Text.translatable("game.2048.info.pause"));
        });
    }

    @Override
    protected void init() {
        super.init();
        cellSize = 23;
        spacing = 3;

        playWidth = game.getCols() * (cellSize + spacing) - spacing;
        playHeight = game.getRows() * (cellSize + spacing) - spacing;

        playX = (this.width - getTotalUpBtnWidth()) / 2;
        playY = (this.height - playHeight) / 2;
        panelX = playX + playWidth + 20;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (game.getState() == Game2048.State.RUNNING) {
            tickCounter++;
            int dropInterval = 50;
            if (tickCounter >= dropInterval) {
                tickCounter = 0;
                game.dropStep();
            }
        }

        context.fillGradient(playX - 4, playY - 4, playX + playWidth + 4, playY + playHeight + 4, 0xFF333333, 0xFF111111);

        int[][] board = game.getBoard();
        for (int r = 0; r < game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                int x = playX + c * (cellSize + spacing);
                int y = playY + r * (cellSize + spacing);
                context.fill(x, y, x + cellSize, y + cellSize, 0xFF0A1A1F);
                int lvl = board[r][c];
                if (lvl != 0) drawBlock(context, x, y, lvl);
            }
        }

        if (game.getState() == Game2048.State.RUNNING) {
            int x = playX + game.getCurrentX() * (cellSize + spacing);
            int y = playY + game.getCurrentY() * (cellSize + spacing);
            drawBlock(context, x, y, game.getCurrentLevel());
        }

        drawPanel(context);

        if (game.getState() == Game2048.State.GAMEOVER) {
            context.fill(playX, playY, playX + playWidth, playY + playHeight, 0xAAFF0000);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("game.2048.info.game_over"),
                    playX + playWidth / 2, playY + playHeight / 2 - 10, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("game.2048.info.score", game.getScore()),
                    playX + playWidth / 2, playY + playHeight / 2 + 10, 0xFFFFFF00);
        }

        if (game.getState() == Game2048.State.PAUSED) {
            context.fill(playX, playY, playX + playWidth, playY + playHeight, 0xAA000000); // чёрное полупрозрачное
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("game.2048.info.paused"),
                    playX + playWidth / 2, playY + playHeight / 2, 0xFFFFFFFF);
        }

        if (game.getState() == Game2048.State.VICTORY) {
            context.fill(playX, playY, playX + playWidth, playY + playHeight, 0xAA00FF00); // зеленое затемнение
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("game.2048.info.victory"),
                    playX + playWidth / 2, playY + playHeight / 2 - 10, 0xFFFFFFFF);
            context.drawCenteredTextWithShadow(textRenderer, Text.translatable("game.2048.info.score", game.getScore()),
                    playX + playWidth / 2, playY + playHeight / 2 + 10, 0xFFFFFF00);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBlock(DrawContext context, int x, int y, int lvl) {
        Identifier tex = BLOCK_TEXTURES[Math.min(lvl, 11)];
        context.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, 0, 0, cellSize, cellSize, cellSize, cellSize);

        int val = 1 << lvl;
        String s = String.valueOf(val);
        int w = textRenderer.getWidth(s);
        context.drawText(textRenderer, Text.literal(s), x + (cellSize - w) / 2, y + (cellSize - 8) / 2, 0xFFFFFFFF, true);
    }

    private void drawPanel(DrawContext context) {
        int panelTop = playY - 6;
        int panelBottom = playY + playHeight + 6;
        context.fill(panelX - 10, panelTop, panelX + 190, panelBottom, 0x88000000);

        int y = playY;

        context.drawText(textRenderer, Text.translatable("game.2048.title"), panelX, y, 0xFF00FFFF, false);
        y += 20;

        context.drawText(textRenderer, Text.translatable("game.2048.info.score", game.getScore()), panelX, y, 0xFFFFFFFF, false);
        y += 16;

        int best = GameRecords.getInstance().getBestScore("2048_blocks");
        context.drawText(textRenderer, Text.translatable("game.2048.info.best", best), panelX, y, 0xFFFFFFFF, false);
        y += 30;

        context.drawText(textRenderer, Text.translatable("game.2048.info.controls"), panelX, y, 0xFFAAAAAA, false);
        y += 14;

        context.drawText(textRenderer, Text.translatable("game.2048.info.left"), panelX, y, 0xFFCCCCCC, false);
        y += 12;

        context.drawText(textRenderer, Text.translatable("game.2048.info.right"), panelX, y, 0xFFCCCCCC, false);
        y += 12;

        context.drawText(textRenderer, Text.translatable("game.2048.info.down"), panelX, y, 0xFFCCCCCC, false);
        y += 12;

        context.drawText(textRenderer, Text.translatable("game.2048.info.drop"), panelX, y, 0xFFCCCCCC, false);
        y += 12;

        context.drawText(textRenderer, Text.translatable("game.2048.info.restart"), panelX, y, 0xFFCCCCCC, false);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (game.getState() == Game2048.State.RUNNING || game.getState() == Game2048.State.PAUSED) {
            switch (input.key()) {
                case GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_LEFT: game.move(-1); return true;
                case GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_RIGHT: game.move(1); return true;
                case GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_DOWN: game.dropStep(); return true;
                case GLFW.GLFW_KEY_SPACE: game.hardDrop(); return true;
                case GLFW.GLFW_KEY_P: game.togglePause(); return true; // кейбинд паузы
            }
        }
        return super.keyPressed(input);
    }

    @Override
    protected void reset() {
        game.reset();
    }
}
