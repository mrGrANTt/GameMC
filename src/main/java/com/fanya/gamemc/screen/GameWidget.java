package com.fanya.gamemc.screen;

import com.fanya.gamemc.GameMC;
import com.fanya.gamemc.minigames.MiniGame;
import com.fanya.gamemc.util.GameColorPalette;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.DrawnTextConsumer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GameWidget extends PressableWidget {
    private static final double k = 1.1;

    public static final int PANEL_WIDTH = (int) (160*k);
    public static final int PANEL_HEIGHT = (int) (64*k);
    public static final Identifier DEFAULT_ICON = Identifier.of(GameMC.MOD_ID, "textures/gui/game_icons/default.png");

    // render icon
    private static final int UP_PANEL_SPACING = (int) (10*k);
    private static final int SPACE_IMAGE_X = (int) (10*k);
    private static final int SPACE_IMAGE_Y = (int) (0*k);
    private static final int ICON_XY_SCALE = (int) (48*k);

    // render title
    private static final int GAME_TITLE_SPACING_X = (int) (10*k);
    private static final int GAME_TITLE_SPACING_Y = (int) (8*k);
    private static final int GAME_TITLE_HEIGHT = (int) (10*k);

    // render play button
    private static final int PLAY_BTN_WIDTH = (int) (60*k);
    private static final int PLAY_BTN_HEIGHT = (int) (20*k);
    private static final int END_PLAY_BTN_X = (int) (PANEL_WIDTH - 10*k);
    private static final int END_PLAY_BTN_Y = (int) (PANEL_HEIGHT - 10*k);
    private static final int START_PLAY_BTN_X = END_PLAY_BTN_X - PLAY_BTN_WIDTH;
    private static final int START_PLAY_BTN_Y = END_PLAY_BTN_Y - PLAY_BTN_HEIGHT;




    // Class variables & methods

    private final MiniGame game;
    private boolean hoveredPlayButton;
    private boolean hovered;
    private boolean selected;

    public GameWidget(int x, int y, int width, int height, MiniGame miniGame) {
        super(x,y,width,height,miniGame.getMiniGameTittle());
        game = miniGame;
        hoveredPlayButton = false;
    }

    public GameWidget(int x, int y, MiniGame miniGame) {
        this(x, y, PANEL_WIDTH, PANEL_HEIGHT, miniGame);
    }

    public MiniGame getMiniGame() {return game;}

    public boolean isOnScreen(int width, int height) {
        return getX()+PANEL_WIDTH > 0 && getY()+PANEL_HEIGHT > 0
                && getX() < width && getY() < height;
    }

    private boolean isInBoundOfButton(int mouseX, int mouseY) {
        return mouseX >= getX() + START_PLAY_BTN_X && mouseY >= getY() + START_PLAY_BTN_Y
                && mouseX <= getX() + END_PLAY_BTN_X && mouseY <= getY() + END_PLAY_BTN_Y;
    }

    private boolean isInBound(int mouseX, int mouseY) {
        return mouseX >= getX() && mouseY >= getY()
                && mouseX <= getX() + PANEL_WIDTH && mouseY <= getY() + PANEL_HEIGHT;
    }
    
    public void setSelected(boolean _selected) {
        selected = _selected;
    }

    public boolean isHoveredPlayButton() {
        return hoveredPlayButton;
    }



    // Extended methods

    @Override
    public void onPress(AbstractInput input) {
        if (MinecraftClient.getInstance() != null)
            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null)
            client.setScreen(game.getMiniGameScreen(client.currentScreen));
    }

    @Override
    protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int x = getX(),
                y = getY();

        hoveredPlayButton = isInBoundOfButton(mouseX, mouseY);
        hovered = isInBound(mouseX, mouseY);

        GameScreen.renderGround(context, x, y+UP_PANEL_SPACING, x+PANEL_WIDTH, y+PANEL_HEIGHT, hovered || selected);

        // render icon
        Identifier icon = game.getIcon();
        context.drawTexture(RenderPipelines.GUI_TEXTURED, icon == null ? DEFAULT_ICON : icon, x+SPACE_IMAGE_X, y+SPACE_IMAGE_Y, 0, 0, ICON_XY_SCALE, ICON_XY_SCALE, ICON_XY_SCALE, ICON_XY_SCALE);

        // render title
        int gameTitleX = x + SPACE_IMAGE_X + ICON_XY_SCALE + GAME_TITLE_SPACING_X;
        int gameTitleY = y + UP_PANEL_SPACING + GAME_TITLE_SPACING_Y;
        DrawnTextConsumer drawer = context.getHoverListener(this, DrawContext.HoverType.NONE);
        drawer.text(getMessage(), gameTitleX, x+PANEL_WIDTH - GAME_TITLE_SPACING_X , gameTitleY, gameTitleY+GAME_TITLE_HEIGHT);

        // render play button
        int startBtnX = x+START_PLAY_BTN_X,
                startBtnY = y+START_PLAY_BTN_Y,
                endBtnX = startBtnX + PLAY_BTN_WIDTH,
                endBtnY = startBtnY + PLAY_BTN_HEIGHT;
        context.fill(startBtnX, startBtnY, endBtnX, endBtnY, hoveredPlayButton ? GameColorPalette.PLAY_BUTTON_HOVER_COLOR : GameColorPalette.PLAY_BUTTON_COLOR);
        drawer.text(Text.translatable("menu.gamemc.play"), startBtnX, endBtnX, startBtnY, endBtnY);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        appendDefaultNarrations(builder);
    }

    @Override
    public boolean isHovered() {
        return hovered;
    }
}
