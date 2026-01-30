package com.fanya.gamemc;

import com.fanya.gamemc.minigames._2048.Game2048Screen;
import com.fanya.gamemc.minigames.simon.SimonGameScreen;
import com.fanya.gamemc.minigames.snake.SnakeSizeSelectScreen;
import com.fanya.gamemc.minigames.solitaire.SolitaireGameScreen;
import com.fanya.gamemc.screen.GameSelectionScreen;
import com.fanya.gamemc.util.CustomSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMC implements ClientModInitializer {
    public static final String MOD_ID = "gamemc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final KeyBinding.Category GAME_MC_CATEGORY =
            KeyBinding.Category.create(Identifier.of(MOD_ID, "keys"));

    private static KeyBinding openScreenKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("GameMC initialized!");
        CustomSounds.initialize();

        GameSelectionScreen.pushMiniGame(new SnakeSizeSelectScreen.SnakeMiniGame());
        GameSelectionScreen.pushMiniGame(new Game2048Screen.Game2048MiniGame());
        GameSelectionScreen.pushMiniGame(new SimonGameScreen.SimonMiniGame());
        GameSelectionScreen.pushMiniGame(new SolitaireGameScreen.SolitaireMiniGame());

        openScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gamemc.open_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_HOME,
                GAME_MC_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreenKey.wasPressed()) {
                if (client.player != null) {
                    client.execute(() -> client.setScreen(new GameSelectionScreen(client.currentScreen)));
                }
            }
        });
    }
}