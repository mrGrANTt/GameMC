package com.fanya.gamemc.screen;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class ScrollbarWidget extends ClickableWidget {
    private static final int MINIMAL_SCROLLBAR_HEIGHT = 4;


    private int baseColor, scrollerColor;
    private double maxValue, minValue;
    private double value;

    private int scrollbarHeight;
    private double pixelPerValue;


    public ScrollbarWidget(int x, int y, int width, int height, float maxValue, float minValue, int baseColor, int scrollerColor) {
        super(x, y, width, height, Text.empty());
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.baseColor = baseColor;
        this.scrollerColor = scrollerColor;
        this.value = this.minValue;

        scrollbarHeight = Math.max((int) (height/(maxValue-minValue)), MINIMAL_SCROLLBAR_HEIGHT);
        pixelPerValue = (height-scrollbarHeight)/(maxValue-minValue);
    }

    public double getMaxValue() { return maxValue; }
    public void setMaxValue(double value) { maxValue = value; }

    public double getMinValue() { return minValue; }
    public void setMinValue(double value) { minValue = value; }

    public double getValue() { return value; }
    public void setValue(double value) { if (value >= minValue && value <= maxValue) this.value = value; }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int x = getX(),
                y = getY();
        int width = getWidth(),
                height = getHeight();

        scrollbarHeight = Math.max((int) (height/(maxValue-minValue)), MINIMAL_SCROLLBAR_HEIGHT);
        pixelPerValue = (height-scrollbarHeight)/(maxValue-minValue);
        int scrollbarY = (int) (pixelPerValue * value + y);

        context.fill(x, y, x+width, y+height, baseColor);
        context.fill(x, scrollbarY, x+width, scrollbarY+scrollbarHeight, scrollerColor);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        double newValue = (click.y()-getY()-scrollbarHeight/2)/pixelPerValue;
        value = Math.min(maxValue, Math.max(minValue, newValue));
        super.onClick(click, doubled);
    }

    @Override
    protected void onDrag(Click click, double offsetX, double offsetY) {
        double change = offsetY/pixelPerValue;
        value = Math.min(maxValue, Math.max(minValue, value+change));
        super.onDrag(click, offsetX, offsetY);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) { appendDefaultNarrations(builder); }
}
