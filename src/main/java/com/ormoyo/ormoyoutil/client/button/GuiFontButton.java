package com.ormoyo.ormoyoutil.client.button;

import java.awt.Color;

import com.ormoyo.ormoyoutil.client.font.FontHelper;
import com.ormoyo.ormoyoutil.util.Font;
import com.ormoyo.ormoyoutil.util.Font.FontEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiFontButton extends GuiButton {
	public double scale;
	public Font.FontEntry font = Font.getDefaultFont();
	public GuiFontButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		this(buttonId, x, y, widthIn, heightIn, buttonText, 1);
	}
	
	public GuiFontButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Font.FontEntry font) {
		this(buttonId, x, y, widthIn, heightIn, buttonText, 1);
		this.font = font;
	}
	
	public GuiFontButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, double scale) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.scale = scale;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		FontHelper.drawString(this.displayString, this.x, this.y, this.scale, Color.WHITE, this.font);
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
	}
}
