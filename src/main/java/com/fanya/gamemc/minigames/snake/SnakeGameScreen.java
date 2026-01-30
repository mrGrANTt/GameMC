package com.fanya.gamemc.minigames.snake;

import com.fanya.gamemc.screen.GameScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.PositionedSoundInstance;

import net.minecraft.text.Text;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;
import com.fanya.gamemc.data.GameRecords;

import java.util.List;

public class SnakeGameScreen extends GameScreen {
    private int bestScore;
    private SnakeGame game;

    private static final int MIN_CELL_SIZE = 8;
    private static final int MAX_CELL_SIZE = 28;

    public static int selectedGridWidth = 25;
    public static int selectedGridHeight = 18;

    private static final Identifier SLIME = Identifier.ofVanilla("textures/block/slime_block.png");
    private static final Identifier EMERALD = Identifier.ofVanilla("textures/block/emerald_block.png");

    private int cellSize;
    private int gridOffsetX;
    private int gridOffsetY;

    public SnakeGameScreen(Screen parent, int gridWidth, int gridHeight) {
        super(Text.translatable("game.snake.title"), parent);
        game = new SnakeGame(gridWidth, gridHeight);
        reset();

        addUpButton(Text.translatable("game.snake.button.select_size"), button -> {
            assert this.client != null;
            this.client.setScreen(new SnakeSizeSelectScreen(parent));
        });
    }

    @Override
    protected void init() {
        super.init();
        bestScore = GameRecords.getInstance().getBestScore("snake");
        calculateGridSize();

        game.setOnFoodEaten((foodConfig) -> {
            // звук при съедании любой еды
            MinecraftClient.getInstance().getSoundManager().play(
                    PositionedSoundInstance.master(SoundEvents.ENTITY_GENERIC_EAT, 1.0F)
            );
        });
    }

    private void recalcGridLayout() {
        calculateGridSize();
    }

    private void calculateGridSize() {
        int gridW = getDisplayGridWidth();
        int gridH = getDisplayGridHeight();

        int reservedWidth = 160;
        int reservedHeight = 200;

        int availableWidth = Math.max(this.width - reservedWidth, 200);
        int availableHeight = Math.max(this.height - reservedHeight, 150);

        int cellWidthBased = availableWidth / gridW;
        int cellHeightBased = availableHeight / gridH;

        cellSize = Math.min(cellWidthBased, cellHeightBased);
        cellSize = Math.max(MIN_CELL_SIZE, Math.min(MAX_CELL_SIZE, cellSize));

        int totalGridWidth = gridW * cellSize;
        int totalGridHeight = gridH * cellSize;

        gridOffsetX = (this.width - totalGridWidth) / 2;
        gridOffsetY = (this.height - totalGridHeight) / 2;

        if (gridOffsetX < 15) gridOffsetX = 15;
        if (gridOffsetY < 40) gridOffsetY = 40;

        int maxGridBottom = this.height - 35;
        if (gridOffsetY + totalGridHeight > maxGridBottom) {
            gridOffsetY = Math.max(40, maxGridBottom - totalGridHeight);
        }
    }

    private int getDisplayGridWidth() {
        return (game != null) ? game.getGridWidth() : selectedGridWidth;
    }

    private int getDisplayGridHeight() {
        return (game != null) ? game.getGridHeight() : selectedGridHeight;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int borderSize = Math.max(2, cellSize / 8);
        context.fillGradient(
                gridOffsetX - borderSize,
                gridOffsetY - borderSize,
                gridOffsetX + getDisplayGridWidth() * cellSize + borderSize,
                gridOffsetY + getDisplayGridHeight() * cellSize + borderSize,
                0xFF1a8c99, 0xFF0d5d66
        );

        context.fill(gridOffsetX, gridOffsetY,
                gridOffsetX + getDisplayGridWidth() * cellSize,
                gridOffsetY + getDisplayGridHeight() * cellSize,
                0xFF0a1a1f);

        drawGrid(context);

        if (game != null && !game.isGameOver()) {
            game.update();
        }

        drawGame(context);

        drawInfoPanel(context);

        if (game != null && game.isGameOver()) {
            if (game.getScore() > bestScore) {
                bestScore = game.getScore();
                GameRecords.getInstance().setBestScore("snake", bestScore);
            }
            drawGameOverScreen(context);
        }
        if (game != null && game.isGameWon()) {
            drawGameWonScreen(context);
        }

        this.children().forEach(child -> {
            if (child instanceof ButtonWidget button) {
                button.render(context, mouseX, mouseY, delta);
            }
        });
    }

    @Override
    protected void reset() {
        game.reset();
    }

    private void drawGrid(DrawContext context) {
        int gridColor = 0x20FFFFFF;

        int gridW = getDisplayGridWidth();
        int gridH = getDisplayGridHeight();

        for (int x = 0; x <= gridW; x++) {
            context.fill(
                    gridOffsetX + x * cellSize,
                    gridOffsetY,
                    gridOffsetX + x * cellSize + 1,
                    gridOffsetY + gridH * cellSize,
                    gridColor
            );
        }

        for (int y = 0; y <= gridH; y++) {
            context.fill(
                    gridOffsetX,
                    gridOffsetY + y * cellSize,
                    gridOffsetX + gridW * cellSize,
                    gridOffsetY + y * cellSize + 1,
                    gridColor
            );
        }
    }

    private void drawGame(DrawContext context) {
        if (game == null) return;

        int padding = Math.max(1, cellSize / 10);

        // Рисуем все еды
        List<SnakeGame.Position> foods = game.getFoods();
        for (SnakeGame.Position foodPos : foods) {
            SnakeGame.FoodConfig cfg = game.getFoodConfigAt(foodPos);
            Identifier foodTexture = cfg != null ? cfg.getTexture() : null;
            if (foodTexture != null) {
                try {
                    int foodSize = Math.max(1, cellSize - padding * 2);
                    if (foodSize > 2) {
                        context.drawTexture(RenderPipelines.GUI_TEXTURED, foodTexture,
                                gridOffsetX + foodPos.x * cellSize + padding,
                                gridOffsetY + foodPos.y * cellSize + padding,
                                0, 0,
                                foodSize, foodSize,
                                foodSize, foodSize);
                    }
                } catch (Exception ignored) { }
            }
        }

        long now = System.currentTimeMillis();
        float t = Math.min(1f, (now - game.getLastMoveTime()) / (float) game.getMoveDelay());

        for (int i = 0; i < game.getSnake().size(); i++) {
            SnakeGame.RenderSegment segment = game.getSnake().get(i);
            segment.updatePosition(t);

            Identifier texture = (i == 0) ? EMERALD : SLIME;
            int segmentPadding = Math.max(1, cellSize / 12);
            int segmentSize = Math.max(1, cellSize - segmentPadding * 2);

            if (segmentSize > 2) {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, texture,
                        gridOffsetX + (int)(segment.x * cellSize) + segmentPadding,
                        gridOffsetY + (int)(segment.y * cellSize) + segmentPadding,
                        0, 0,
                        segmentSize, segmentSize,
                        segmentSize, segmentSize);
            }
        }
    }


    private void drawInfoPanel(DrawContext context) {
        int panelHeight = Math.max(24, Math.min(32, cellSize + 8));
        int panelY = Math.max(5, gridOffsetY - panelHeight - 4);

        int panelWidth = getDisplayGridWidth() * cellSize;

        context.fillGradient(
                gridOffsetX - 3, panelY,
                gridOffsetX + panelWidth + 3, panelY + panelHeight,
                0xD0101010, 0xE0202020
        );

        context.fill(
                gridOffsetX - 3, panelY,
                gridOffsetX + panelWidth + 3, panelY + 2,
                0xFF1a8c99
        );

        int textSize = this.textRenderer.fontHeight;
        int textY1 = panelY + 4;
        int textY2 = panelY + panelHeight - textSize - 2;

        String scoreText = Text.translatable("game.snake.score", game != null ? game.getScore() : 0).getString();
        context.drawTextWithShadow(this.textRenderer, scoreText,
                gridOffsetX + 5, textY1, 0xFFFFD700);

        String lengthText = Text.translatable("game.snake.length", game != null ? game.getSnake().size() : 0).getString();
        context.drawTextWithShadow(this.textRenderer, lengthText,
                gridOffsetX + 5, textY2, 0xFF00FF00);

        String bestText = Text.translatable("game.snake.best_score", bestScore).getString();
        float scale = Math.max(1.0f, cellSize / 18f);

        int textWidth = (int)(this.textRenderer.getWidth(bestText) * scale);
        int textHeight = (int)(this.textRenderer.fontHeight * scale);

        float x = gridOffsetX + panelWidth / 2f - textWidth / 2f;
        float y = panelY + panelHeight / 2f - textHeight / 2f;

        context.drawText(
                this.textRenderer,
                Text.literal(bestText),
                (int)x,
                (int)y,
                0xFF87CEEB,
                true
        );

        String controls = "WASD | R";
        int controlsWidth = this.textRenderer.getWidth(controls);
        if (controlsWidth < panelWidth - 60) {
            context.drawTextWithShadow(this.textRenderer, controls,
                    gridOffsetX + panelWidth - controlsWidth,
                    panelY + panelHeight / 2 - textSize / 2, 0xFFAAAAAA);
        }
    }

    private void drawGameOverScreen(DrawContext context) {
        context.fillGradient(gridOffsetX, gridOffsetY,
                gridOffsetX + getDisplayGridWidth() * cellSize,
                gridOffsetY + getDisplayGridHeight() * cellSize,
                0xD0AA0000, 0xD0550000);

        int centerX = gridOffsetX + (getDisplayGridWidth() * cellSize) / 2;
        int centerY = gridOffsetY + (getDisplayGridHeight() * cellSize) / 2;

        int lineHeight = this.textRenderer.fontHeight + 4;

        Text gameOverText = Text.translatable("game.snake.lose");
        context.drawCenteredTextWithShadow(this.textRenderer, gameOverText,
                centerX, centerY - lineHeight * 2, 0xFFFFFFFF);

        Text scoreText = Text.translatable("game.snake.score", game != null ? game.getScore() : 0);
        context.drawCenteredTextWithShadow(this.textRenderer, scoreText,
                centerX, centerY - lineHeight / 2, 0xFFFFFF00);

        Text lengthText = Text.translatable("game.snake.length", game != null ? game.getSnake().size() : 0);
        context.drawCenteredTextWithShadow(this.textRenderer, lengthText,
                centerX, centerY + lineHeight / 2, 0xFF00FF00);

        Text bestScoreText = Text.translatable("game.snake.best_score", bestScore);
        context.drawCenteredTextWithShadow(this.textRenderer, bestScoreText,
                centerX, centerY + lineHeight * 3, 0xFF87CEEB);

        Text restartText = Text.translatable("game.snake.restart_hint");
        context.drawCenteredTextWithShadow(this.textRenderer, restartText,
                centerX, centerY + lineHeight * 2, 0xFFCCCCCC);
    }

    private void drawGameWonScreen(DrawContext context) {
        context.fillGradient(gridOffsetX, gridOffsetY,
                gridOffsetX + getDisplayGridWidth() * cellSize,
                gridOffsetY + getDisplayGridHeight() * cellSize,
                0xD000AA00, 0xD055FF00);

        int centerX = gridOffsetX + (getDisplayGridWidth() * cellSize) / 2;
        int centerY = gridOffsetY + (getDisplayGridHeight() * cellSize) / 2;

        Text wonText = Text.translatable("game.snake.won");
        context.drawCenteredTextWithShadow(this.textRenderer, wonText,
                centerX, centerY, 0xFFFFFFFF);
    }


    @Override
    public boolean keyPressed(KeyInput input) {
        if (game == null) return super.keyPressed(input);

        switch (input.key()) {
            case GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_UP -> {
                game.setDirection(Direction.NORTH);
                return true;
            }
            case GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_DOWN -> {
                game.setDirection(Direction.SOUTH);
                return true;
            }
            case GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_LEFT -> {
                game.setDirection(Direction.WEST);
                return true;
            }
            case GLFW.GLFW_KEY_D, GLFW.GLFW_KEY_RIGHT -> {
                game.setDirection(Direction.EAST);
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        recalcGridLayout();
    }
}