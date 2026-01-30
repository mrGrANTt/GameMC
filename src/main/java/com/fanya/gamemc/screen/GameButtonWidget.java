package com.fanya.gamemc.screen;

import com.fanya.gamemc.util.GameColorPalette;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class GameButtonWidget {
    public static final int btnWidth = 100;
    public static final int btnHeight = 20;
    public static final int btnY = 3;
    public static final int spacingBtn = 10;

    // button creators
    public static ButtonWidget createButton(int x, int y, int width, int height, Text text, ButtonWidget.PressAction action, ButtonWidget.NarrationSupplier narrationSupplier, int hoverColor, int btnColor) {
        return new ButtonWidget(x,y,width,height,text,action, narrationSupplier) {
            @Override
            protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
                renderButton(context);
                this.drawLabel(context.getHoverListener(this, DrawContext.HoverType.NONE));
            }

            private void renderButton(DrawContext context) {
                context.fill(this.getX(), this.getY(), this.getX()+this.width, this.getY()+this.height, isHovered() ? hoverColor : btnColor);
            }
        };
    }
    public static ButtonWidget createButton(int x, int y, int width, int height, Text text, ButtonWidget.PressAction action, ButtonWidget.NarrationSupplier narrationSupplier) {
        return createButton(x, y, width, height, text, action, narrationSupplier, GameColorPalette.BUTTON_HOVER_COLOR, GameColorPalette.BUTTON_COLOR);
    }
    public static ButtonWidget createButton(int x, int y, int width, int height, Text text, ButtonWidget.PressAction action) {
        return createButton(x, y, width, height, text, action, Supplier::get);
    }
    public static ButtonWidget createButton(int x, int y, Text text, ButtonWidget.PressAction action) {
        return createButton(x, y, GameButtonWidget.btnWidth, GameButtonWidget.btnHeight, text, action);
    }
    public static ButtonWidget createButton(int x, int y, int width, int height, Text text, ButtonWidget.PressAction action, int hoverColor, int btnColor) {
        return createButton(x, y, width, height, text, action, Supplier::get, hoverColor, btnColor);
    }
    public static ButtonWidget createButton(int x, int y, Text text, ButtonWidget.PressAction action, int hoverColor, int btnColor) {
        return createButton(x, y, GameButtonWidget.btnWidth, GameButtonWidget.btnHeight, text, action, hoverColor, btnColor);
    }


    public GameButtonWidget(Text text, ButtonWidget.PressAction onPress) {
        _text = text;
        _onPress = onPress;
    }

    private Text _text;
    private ButtonWidget.PressAction _onPress;

    // Getters/Setters
    public void setText(Text text) {_text=text;}
    public void setOnPress(ButtonWidget.PressAction onPress) {_onPress=onPress;}
    public Text getText() {return _text;}
    public ButtonWidget.PressAction getOnPress() {return _onPress;}

    public ButtonWidget build(int x, int y, int width, int height) {
        return createButton(x,y,width,height,_text,_onPress);
    }
}