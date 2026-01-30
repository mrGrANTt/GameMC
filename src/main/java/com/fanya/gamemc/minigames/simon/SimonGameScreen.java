package com.fanya.gamemc.minigames.simon;

import com.fanya.gamemc.data.GameRecords;
import com.fanya.gamemc.minigames.MiniGame;
import com.fanya.gamemc.screen.GameButtonWidget;
import com.fanya.gamemc.screen.GameScreen;
import net.minecraft.block.Blocks;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class SimonGameScreen extends GameScreen {
    public static class SimonMiniGame implements MiniGame {
        @Override
        public Text getMiniGameTittle() {
            return Text.translatable("game.simon.title");
        }

        @Override
        public Screen getMiniGameScreen(Screen parent) {
            return new SimonGameScreen(parent);
        }

        @Override
        public Identifier getIcon() {
            return COPPER_LAMP_LIT_TEXTURE;
        }

        @Override
        public Text getDescription() {
            return Text.translatable("game.simon.description");
        }
    }

    private static final Identifier COPPER_LAMP_TEXTURE = Identifier.ofVanilla("textures/block/copper_bulb.png");
    private static final Identifier COPPER_LAMP_LIT_TEXTURE = Identifier.ofVanilla("textures/block/copper_bulb_lit.png");

    private SimonGame game;
    private int bestScore;

    private int centerX;
    private int centerY;
    private int blockSize;
    private int spacing;

    private int lastShownStep = -1;
    private final long[] lampFlashTimes = new long[4];
    private static final long FLASH_DURATION = 400;
    private long lastFlashTriggerTime = 0;

    public SimonGameScreen(Screen parent) {
        super(Text.translatable("game.simon.title"), parent);
    }

    @Override
    protected void init() {
        super.init();
        bestScore = GameRecords.getInstance().getBestScore("simon");
        if (game == null) game = new SimonGame(3);

        calculateLayout();
    }

    private void calculateLayout() {
        blockSize = Math.max(28, Math.min(64, this.width / 8));
        spacing = Math.max(6, blockSize / 6);
        int totalWidth = blockSize * 4 + spacing * 3;
        centerX = (this.width - totalWidth) / 2;
        centerY = Math.max(80, (this.height / 2) - blockSize / 2);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (game != null) game.update();

        int pad = 6;
        int panelWidth = blockSize * 4 + spacing * 3 + pad * 2;
        int panelHeight = blockSize + pad * 2;
        int panelX = centerX - pad;
        int panelY = centerY - pad;

        context.fillGradient(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2,
                0xFF1a8c99, 0xFF0d5d66);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF0A1A1F);

        if (game.getState() == SimonGame.State.SHOWING) {
            int activeLamp = game.getCurrentlyShowingIndex();
            if (activeLamp >= 0 && activeLamp < 4) {
                long now = System.currentTimeMillis();

                if (lastShownStep != activeLamp ||
                        now - lastFlashTriggerTime > game.getShowDuration() + game.getBetweenDelay() / 2) {

                    lastShownStep = activeLamp;
                    lastFlashTriggerTime = now;
                    lampFlashTimes[activeLamp] = now; // запускаем пульсацию
                }
            }
        } else {
            lastShownStep = -1;
        }



        for (int i = 0; i < 4; i++) {
            int x = centerX + i * (blockSize + spacing);
            drawLamp(context, x, centerY, blockSize, i);
        }

        drawInfo(context);

        if (game.getState() == SimonGame.State.LOST)
            drawGameOver(context);

        this.children().forEach(child -> {
            if (child instanceof ButtonWidget b) b.render(context, mouseX, mouseY, delta);
        });
    }

    private void drawLamp(DrawContext context, int x, int y, int size, int index) {
        boolean lit = false;
        if (game.getState() == SimonGame.State.SHOWING) {
            lit = (game.getCurrentlyShowingIndex() == index);
        }

        Identifier tex = lit ? COPPER_LAMP_LIT_TEXTURE : COPPER_LAMP_TEXTURE;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, 0, 0, size, size, size, size);

        long elapsed = System.currentTimeMillis() - lampFlashTimes[index];
        if (elapsed < FLASH_DURATION) {
            float progress = elapsed / (float) FLASH_DURATION;
            float alpha = 1.0f - progress;
            float scale = 1.0f + progress * 0.5f;
            int border = (int) (6 * scale);

            int baseColor = switch (index) {
                case 0 -> 0xFFFF4444;
                case 1 -> 0xFF44FF44;
                case 2 -> 0xFF4488FF;
                case 3 -> 0xFFFFFF44;
                default -> 0xFFFFFFFF;
            };

            int color = ((int) (alpha * 255) << 24) | (baseColor & 0xFFFFFF);
            context.fill(x - border, y - border, x + size + border, y, color);
            context.fill(x - border, y + size, x + size + border, y + size + border, color);
            context.fill(x - border, y, x, y + size, color);
            context.fill(x + size, y, x + size + border, y + size, color);
        }

        assert this.client != null;
        int mx = (int) (this.client.mouse.getX() * this.width / this.client.getWindow().getFramebufferWidth());
        int my = (int) (this.client.mouse.getY() * this.height / this.client.getWindow().getFramebufferHeight());
        if (mx >= x && mx <= x + size && my >= y && my <= y + size) {
            context.fill(x, y, x + size, y + size, 0x33FFFFFF);
        }

        context.fill(x, y + size - 4, x + size, y + size - 3, 0xFF444444);
    }

    private void drawInfo(DrawContext context) {
        int centerX = this.width / 2;
        int panelY = GameButtonWidget.btnY+GameButtonWidget.btnHeight+GameButtonWidget.spacingBtn;
        String title = this.textRenderer.trimToWidth(Text.translatable("game.simon.title").getString(), 200);
        context.drawText(this.textRenderer, Text.literal(title), centerX - this.textRenderer.getWidth(title) / 2, panelY, 0xFF00FFFF, true);

        String seq = this.textRenderer.trimToWidth(Text.translatable("game.simon.sequence_length", game.getSequenceLength()).getString(), 200);
        context.drawText(this.textRenderer, Text.literal(seq), centerX - this.textRenderer.getWidth(seq) / 2, panelY + 15, 0xFFAAAAAA, true);

        String best = this.textRenderer.trimToWidth(Text.translatable("game.simon.best_score", bestScore).getString(), 200);
        context.drawText(this.textRenderer, Text.literal(best), centerX - this.textRenderer.getWidth(best) / 2, panelY + 30, 0xFFAAAAAA, true);
    }

    private void drawGameOver(DrawContext context) {
        int boxW = 260;
        int boxH = 120;
        int bx = (this.width - boxW) / 2;
        int by = (this.height - boxH) / 2;

        context.fillGradient(bx, by, bx + boxW, by + boxH, 0xD0AA0000, 0xD0550000);

        int cx = bx + boxW / 2;
        int y = by + 18;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("game.simon.lose"), cx, y, 0xFFFFFFFF);
        y += 22;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("game.simon.length", game.getSequenceLength() - 1), cx, y, 0xFFFFFF00);
        y += 22;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("game.simon.restart_hint"), cx, y, 0xFFCCCCCC);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (game == null || click.button() != 0) return super.mouseClicked(click,doubled);
        if (game.getState() != SimonGame.State.SHOWING) {

            int mx = (int) click.x();
            int my = (int) click.y();

            for (int i = 0; i < 4; i++) {
                int x = centerX + i * (blockSize + spacing);
                int y = centerY;
                if (mx >= x && mx <= x + blockSize && my >= y && my <= y + blockSize) {
                    boolean handled = game.clickButton(i);
                    lampFlashTimes[i] = System.currentTimeMillis();

                    if (game.getState() == SimonGame.State.LOST) {
                        int score = Math.max(0, game.getSequenceLength() - 1);
                        if (score > bestScore) {
                            bestScore = score;
                            GameRecords.getInstance().setBestScore("simon", bestScore);
                        }
                    }
                    return handled || super.mouseClicked(click, doubled);
                }
            }
        }
        return super.mouseClicked(click,doubled);
    }

    @Override
    protected void reset() {
        game.reset();
    }
}