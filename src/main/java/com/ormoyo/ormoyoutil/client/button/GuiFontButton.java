package com.ormoyo.ormoyoutil.client.button;

import java.awt.Color;

import com.ormoyo.ormoyoutil.client.font.FontHelper;
import com.ormoyo.ormoyoutil.util.FontHandler;
import com.ormoyo.ormoyoutil.util.FontHandler.Font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiFontButton extends GuiButton {
	public double scale;
	public FontHandler.Font font = FontHandler.getDefaultFont();
	public Color color = Color.WHITE;
	public GuiFontButton(Font font, int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, double scale) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
		this.font = font;
		this.scale = scale;
	}
	
	public GuiFontButton(Font font, int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		this(font, buttonId, x, y, widthIn, heightIn, buttonText, 1);
	}
	
	public GuiFontButton(Font font, int buttonId, int x, int y, String buttonText, double scale) {
		this(font, buttonId, x, y, FontHelper.getWordWidth(buttonText, font, scale), FontHelper.getWordHeight(buttonText, font, scale * 1.09), buttonText, scale);
	}
	
	public GuiFontButton(Font font, int buttonId, int x, int y, String buttonText) {
		this(font, buttonId, x, y, FontHelper.getWordWidth(buttonText, font, 1), FontHelper.getWordHeight(buttonText, font, 1), buttonText);
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if(this.visible) {
			Color oldcolor = this.color;
			if(!this.enabled) {
				this.color = Color.GRAY;
			}
			FontHelper.drawString(this.font, this.displayString, this.x, this.y, this.scale, this.color);
			this.color = oldcolor;
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		}
	}
}
