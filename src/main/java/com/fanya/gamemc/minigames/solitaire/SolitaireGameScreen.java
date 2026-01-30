package com.fanya.gamemc.minigames.solitaire;

import com.fanya.gamemc.GameMC;
import com.fanya.gamemc.data.GameRecords;
import com.fanya.gamemc.minigames.MiniGame;
import com.fanya.gamemc.minigames.solitaire.subclass.SolitaireCard;
import com.fanya.gamemc.screen.GameButtonWidget;
import com.fanya.gamemc.screen.GameScreen;
import com.fanya.gamemc.util.CustomSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SolitaireGameScreen extends GameScreen {
    public static class SolitaireMiniGame implements MiniGame {
        @Override
        public Text getMiniGameTittle() {
            return Text.translatable("game.solitaire.title");
        }

        @Override
        public Screen getMiniGameScreen(Screen parent) {
            return new SolitaireGameScreen(parent);
        }

        @Override
        public Identifier getIcon() {
            return Identifier.of(GameMC.MOD_ID, "textures/gui/game_icons/solitaire.png");
        }

        @Override
        public Text getDescription() {
            return Text.translatable("game.solitaire.description");
        }
    }

    private static final Identifier[][] CARD_TEXTURES = parsTextures();
    private static final Identifier BACK_CARD = Identifier.of(GameMC.MOD_ID, "textures/gui/suits/card.png");
    private static boolean PLAY_SOUND = true;

    private static Identifier[][] parsTextures() {
        Identifier[][] arr = new Identifier[4][13];
        for(SolitaireCard.Suits suit : SolitaireCard.Suits.values())
            for(SolitaireCard.Denominations denomination : SolitaireCard.Denominations.values())
                arr[suit.ordinal()][denomination.ordinal()] = Identifier.of(GameMC.MOD_ID, SolitaireCard.getTexturePath(suit, denomination));
        return arr;
    }

    private GameButtonWidget soundButton;
    private SolitaireGame game;

    private int cardSizeX, cardSizeY, spacingX, spacingY, playWidth, playHeight, playStartX, playStartY;

    private int moveCounter = 0,
            bestScore;
    private SolitaireCard selectedCard = null;
    private double deltaX = 0, deltaY = 0;

    public SolitaireGameScreen(Screen parent) {
        super(Text.translatable("game.solitaire.title"), parent);

        soundButton = addUpButton(Text.translatable("game.solitaire.info.silent", Text.translatable("game.solitaire.info.silent.off")), b -> {
            PLAY_SOUND = !PLAY_SOUND;
            b.setMessage(Text.translatable("game.solitaire.info.silent",
                    PLAY_SOUND ? Text.translatable("game.solitaire.info.silent.off") : Text.translatable("game.solitaire.info.silent.on")));
        });
    }

    @Override
    protected void init() {
        soundButton.setText(Text.translatable("game.solitaire.info.silent",
                Text.translatable(
                        PLAY_SOUND ? "game.solitaire.info.silent.off" :  "game.solitaire.info.silent.on")));
        super.init();
        bestScore = GameRecords.getInstance().getBestScore("solitaire");
        if(game == null) game = new SolitaireGame();

        cardSizeX = 32;
        cardSizeY = 48;
        spacingX = 10;
        spacingY = 7;

        playWidth = 7 * (cardSizeX + spacingX) + spacingX; // 304
        playHeight = 22 * spacingY + 2 * cardSizeY; // 316

        playStartX = Math.max((width - playWidth) / 2, 0);
        playStartY = Math.min((height - playHeight - GameButtonWidget.btnHeight - GameButtonWidget.spacingBtn) * 2 / 3 + GameButtonWidget.btnHeight + GameButtonWidget.spacingBtn, height - playHeight);
    }

    @Override
    protected void reset() {
        playSound(CustomSounds.SWAP_GAME_CARD, 1f, 1f);
        game.reset();
        moveCounter = 0;
        selectedCard = null;
        deltaX = 0;
        deltaY = 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        renderUI(context);
        // for debugging
        //renderSelectBorder(context, mouseX, mouseY, 0x5FFFFF00);
        renderCards(context);
        if(game.getState() == SolitaireGame.State.VICTORY) {
            if(bestScore <= 0 || bestScore > moveCounter) {
                GameRecords.getInstance().setBestScore("solitaire", moveCounter);
                bestScore = moveCounter;
            }
            renderWin(context);
        }
    }

    private void renderSelectBorder(DrawContext context, int mouseX, int mouseY, int selectColor) {
        for(int n = 0; n < 7; n++) {
            int i = playStartX+spacingX+(cardSizeX+spacingX)*n;
            int x = mouseX-i;
            if(x >= 0 && x <= cardSizeX) {
                if(mouseY >= playStartY+spacingY && mouseY <= playStartY+spacingY+cardSizeY) {
                    if(n != 2) {
                        context.fill(i - 1, playStartY + spacingY - 1, i + cardSizeX + 1, playStartY + spacingY + cardSizeY + 1, selectColor);
                    }
                }
                int maxM = game.getColonCount(n);
                for(int m = 0; m < maxM; m++) {
                    int j = playStartY+cardSizeY+spacingY*(2+m);
                    int y = mouseY-j;
                    if(y >= 0 && y <= (m == maxM-1 ? cardSizeY : spacingY)) {
                        context.fill(i-1,j-1,i+cardSizeX+1,j+cardSizeY+1, selectColor);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void renderWin(DrawContext context) {
        context.fill(playStartX,playStartY, playStartX+playWidth, playStartY+playHeight, 0x7F00FF00);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("game.solitaire.info.victory"),
                playStartX + playWidth / 2, playStartY + playHeight / 2 - 10, 0xFFFFFFFF);
    }

    private void renderUI(DrawContext context) {
        // main panel
        context.fillGradient(playStartX-2, playStartY-2, playStartX + playWidth+2, playStartY + playHeight+2, 0xFF1a8c99, 0xFF0d5d66);
        context.fill(playStartX, playStartY, playStartX + playWidth, playStartY + playHeight, 0xFF0A1A1F);

        // deck panel
        context.fillGradient(playStartX+spacingX-1, playStartY+spacingY-1, playStartX+spacingX+cardSizeX+1, playStartY+spacingY+cardSizeY+1, 0x881a8c99, 0x880d5d66);
        context.fillGradient(playStartX+spacingX*2+cardSizeX-1, playStartY+spacingY-1, playStartX+(spacingX+cardSizeX)*2+1, playStartY+spacingY+cardSizeY+1, 0x881a8c99, 0x880d5d66);
        context.fill(playStartX+spacingX, playStartY+spacingY, playStartX+spacingX+cardSizeX, playStartY+spacingY+cardSizeY, 0xFF0A1A1F);
        context.fill(playStartX+spacingX*2+cardSizeX, playStartY+spacingY, playStartX+(spacingX+cardSizeX)*2, playStartY+spacingY+cardSizeY, 0xFF0A1A1F);

        // base panel
        for(int x = playStartX+(spacingX+cardSizeX)*3+spacingX; x < playStartX + playWidth; x += spacingX+cardSizeX) {
            context.fillGradient(x - 1, playStartY+spacingY-1, x + cardSizeX + 1, playStartY+spacingY+cardSizeY+1, 0x881a8c99, 0x880d5d66);
            context.fill(x, playStartY+spacingY, x + cardSizeX, playStartY+spacingY+cardSizeY, 0xFF0A1A1F);
        }
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.translatable("game.solitaire.info.score", moveCounter),
                playStartX+2, playStartY+playHeight-10, 0xFFAAAAAA, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.translatable("game.solitaire.info.best_score", bestScore),
                playStartX+2, playStartY+playHeight-19, 0xFFAAAAAA, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.translatable("game.solitaire.info.control"),
                playStartX+2, playStartY+playHeight-39, 0xFFAAAAAA, false);
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("by mr_GrANTt"),
                playStartX+playWidth-70, playStartY+playHeight-10, 0x7FAAAAAA, false);
    }

    private void renderCards(DrawContext context) {
        if(!game.isDeckEmpty()) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, BACK_CARD,playStartX+spacingX, playStartY+spacingY,0,0,cardSizeX,cardSizeY,cardSizeX,cardSizeY);
        }

        SolitaireCard card = game.getGameDeck();
        if(card != null) {
            int x = playStartX + spacingX * 2 + cardSizeX,
                y = playStartY + spacingY;
            if(card == selectedCard) {
                if(card.getPrevious() != card) {
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, CARD_TEXTURES[card.getPrevious().getSuit().ordinal()][card.getPrevious().getDenomination().ordinal()],
                            x, y, 0, 0, cardSizeX, cardSizeY, cardSizeX, cardSizeY);
                }
            } else {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, CARD_TEXTURES[card.getSuit().ordinal()][card.getDenomination().ordinal()],
                        x, y, 0, 0, cardSizeX, cardSizeY, cardSizeX, cardSizeY);
            }
        }
        for(int i = 0; i < 4; i ++) renderBase(context, i);
        for(int i = 0; i < 7; i ++) renderColon(context, i);
        renderSelectedCard(context, selectedCard == game.getGameDeck());
    }

    private void renderSelectedCard(DrawContext context, boolean isDeck) {
        if(selectedCard != null) {
            int x = (int) deltaX - cardSizeX / 2,
                y = (int) deltaY - cardSizeY / 5;
            SolitaireCard card = selectedCard;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, CARD_TEXTURES[card.getSuit().ordinal()][card.getDenomination().ordinal()],
                    x, y, 0, 0, cardSizeX, cardSizeY, cardSizeX, cardSizeY);
            if(!isDeck) {
                while (card.getNext() != null) {
                    card = card.getNext();
                    y += spacingY;
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, CARD_TEXTURES[card.getSuit().ordinal()][card.getDenomination().ordinal()],
                            x, y, 0, 0, cardSizeX, cardSizeY, cardSizeX, cardSizeY);
                }
            }
        }
    }

    private void renderColon(DrawContext context, int num) {
        SolitaireCard card = game.getColon(num);
        int y = playStartY+spacingY*2+cardSizeY;
        while (card != null) {
            if(card == selectedCard) break;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, card.isShown() ? CARD_TEXTURES[card.getSuit().ordinal()][card.getDenomination().ordinal()] : BACK_CARD,
                    playStartX+spacingX*(1+num)+cardSizeX*num, y,0,0,cardSizeX,cardSizeY,cardSizeX,cardSizeY);
            card = card.getNext();
            y += spacingY;
        }
    }

    private void renderBase(DrawContext context, int num) {
        SolitaireCard card = game.getBase(num);
        if(card != null) {
            if (card == selectedCard) {
                if (card.getPrevious() != null)
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, CARD_TEXTURES[card.getPrevious().getSuit().ordinal()][card.getPrevious().getDenomination().ordinal()],
                            playStartX + spacingX * (4 + num) + cardSizeX * (3 + num), playStartY + spacingY, 0, 0, cardSizeX, cardSizeY, cardSizeX, cardSizeY);
            } else context.drawTexture(RenderPipelines.GUI_TEXTURED, CARD_TEXTURES[card.getSuit().ordinal()][card.getDenomination().ordinal()],
                    playStartX + spacingX * (4 + num) + cardSizeX * (3 + num), playStartY + spacingY, 0, 0, cardSizeX, cardSizeY, cardSizeX, cardSizeY);
        }
    }

    private void playSound(SoundEvent sound, float pitch, float volume) {
        if(PLAY_SOUND) {
            MinecraftClient.getInstance().getSoundManager().play(
                    PositionedSoundInstance.master(sound, pitch, volume)
            );
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if(game.getState() == SolitaireGame.State.RUNNING) {
            if (click.button() == 1) {
                int n = game.checkWin();
                moveCounter += n;
                if(n > 2) {
                    playSound(CustomSounds.SWAP_GAME_CARD, 1f, 1f);
                } else if(n > 0) {
                    playSound(CustomSounds.PUT_GAME_CARD, 1f, 1f);
                }
                return true;
            } else if (click.button() == 0) {
                int cardPos = getCardByMouse(click.x(), click.y());
                if (cardPos == -1) return super.mouseClicked(click, doubled);
                int cartX = cardPos % 10,
                        cartY = (cardPos / 10) - 1;
                if (cardPos == 10) {
                    if (game.nextDeckCard()) {
                        playSound(CustomSounds.GET_GAME_CARD, 1f, 1f);
                        moveCounter++;
                    }
                } else {
                    playSound(CustomSounds.GET_GAME_CARD, 1f, 1f);
                    selectedCard = game.getCardAt(cartX, cartY);
                    if (selectedCard != null && !selectedCard.isShown() && cartY > 0) {
                        selectedCard = null;
                    }
                    deltaX = click.x();
                    deltaY = click.y();
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if(game.getState() == SolitaireGame.State.RUNNING) {
            deltaX += offsetX;
            deltaY += offsetY;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) { // unclick mouse
        if(game.getState() == SolitaireGame.State.RUNNING) {
            if(click.button() == 0 && selectedCard != null) {
                playSound(CustomSounds.PUT_GAME_CARD, 1f, 1f);
                int cardPos = getCardByMouse(click.x(), click.y());
                if(cardPos != -1) {
                    int cartX = cardPos % 10,
                            cartY = (cardPos / 10) - 1;
                    if ((cartY == 0 && cartX > 2 && game.tryToMoveInBase(selectedCard))
                            || (cartY > 0 && game.tryToMoveInTable(selectedCard, cartX))) {
                        moveCounter++;
                    }
                }
            }
        }
        selectedCard = null;
        deltaX = 0;
        deltaY = 0;
        return super.mouseReleased(click);
    }

    public int getCardByMouse(double mouseX, double mouseY) {
        for(int n = 0; n < 7; n++) {
            int i = playStartX+spacingX+(cardSizeX+spacingX)*n;
            int x = (int) mouseX-i;
            if(x >= 0 && x <= cardSizeX) {
                if(mouseY >= playStartY+spacingY && mouseY <= playStartY+spacingY+cardSizeY) {
                    return 10+n;
                }
                int maxM = game.getColonCount(n);
                if(maxM == 0) return 20+n;
                for(int m = 0; m < maxM; m++) {
                    int j = playStartY+cardSizeY+spacingY*(2+m);
                    int y = (int) mouseY-j;
                    if(y >= 0 && y <= (m == maxM-1 ? cardSizeY : spacingY)) {
                        return (m+2)*10+n;
                    }
                }
                break;
            }
        }
        return -1;
    }
}
