package com.fanya.gamemc.minigames;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ResourceBundle;

public interface MiniGame {
    Text getMiniGameTittle();
    Screen getMiniGameScreen(Screen parent);
    Identifier getIcon();
    Text getDescription();
}
