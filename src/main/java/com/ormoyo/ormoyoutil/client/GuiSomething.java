package com.ormoyo.ormoyoutil.client;

import java.awt.Color;

import com.ormoyo.ormoyoutil.client.button.GuiFontButton;
import com.ormoyo.ormoyoutil.util.FontHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

public class GuiSomething extends GuiScreen {
	
	@Override
	public void initGui() {
		super.initGui();
        int i = (this.width - 160) / 2;
        int j = (this.height - 160) / 2;
		this.buttonList.add(new GuiFontButton(FontHandler.fnf, 0, i, j, "ABCD", 1.3));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int i = (this.width - 160) / 2;
        int j = (this.height - 160) / 2;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.drawTexturedRect(RenderHelper.WHITE, i, j, 0, 0, 16, 16, 16, 16, 10, Color.BLUE);
        super.drawScreen(mouseX, mouseY, partialTicks);
        RenderHelper.drawButtonHitBox(this.buttonList.get(0));
	}
}
