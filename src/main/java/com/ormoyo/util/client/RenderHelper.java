package com.ormoyo.util.client;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.ormoyo.util.OrmoyoUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderHelper {
	public static final ResourceLocation WHITE = new ResourceLocation(OrmoyoUtil.MODID, "textures/white.png");
	public static void drawTexturedRect(ResourceLocation texture, double x, double y, int u, int v, int width, int height, int imageWidth, int imageHeight, double scale) {
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		double minU = (double)u / (double)imageWidth;
		double maxU = (double)(u + width) / (double)imageWidth;
		double minV = (double)v / (double)imageHeight;
		double maxV = (double)(v + height) / (double)imageHeight;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x + scale*(double)width, y + scale*(double)height, 0).tex(maxU, maxV).endVertex();
		buffer.pos(x + scale*(double)width, y, 0).tex(maxU, minV).endVertex();
		buffer.pos(x, y, 0).tex(minU, minV).endVertex();
		buffer.pos(x, y + scale*(double)height, 0).tex(minU, maxV).endVertex();
		tessellator.draw();
	}
	
	public static void drawTexturedRect(ResourceLocation texture, double x, double y, int u, int v, int width, int height, int imageWidth, int imageHeight, double scale, Color color) {
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		double minU = (double)u / (double)imageWidth;
		double maxU = (double)(u + width) / (double)imageWidth;
		double minV = (double)v / (double)imageHeight;
		double maxV = (double)(v + height) / (double)imageHeight;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		buffer.pos(x + scale*(double)width, y + scale*(double)height, 0).tex(maxU, maxV).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(x + scale*(double)width, y, 0).tex(maxU, minV).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(x, y, 0).tex(minU, minV).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		buffer.pos(x, y + scale*(double)height, 0).tex(minU, maxV).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
		tessellator.draw();
	}
	
	public static void drawTexturedRect(ResourceLocation texture, double x, double y, int u, int v, int width, int height, int imageWidth, int imageHeight, double scaleX, double scaleY) {
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		double minU = (double)u / (double)imageWidth;
		double maxU = (double)(u + width) / (double)imageWidth;
		double minV = (double)v / (double)imageHeight;
		double maxV = (double)(v + height) / (double)imageHeight;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x + scaleX*(double)width, y + scaleY*(double)height, 0).tex(maxU, maxV).endVertex();
		buffer.pos(x + scaleX*(double)width, y, 0).tex(maxU, minV).endVertex();
		buffer.pos(x, y, 0).tex(minU, minV).endVertex();
		buffer.pos(x, y + scaleY*(double)height, 0).tex(minU, maxV).endVertex();
		tessellator.draw();
	}
	
	public static void drawTexturedRect(ResourceLocation texture, double x, double y, int u, int v, int uWidth, int vHeight, int width, int height, int imageWidth, int imageHeight, double scaleX, double scaleY) {
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		double minU = (double)u / (double)imageWidth;
		double maxU = (double)(u + uWidth) / (double)imageWidth;
		double minV = (double)v / (double)imageHeight;
		double maxV = (double)(v + vHeight) / (double)imageHeight;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x + scaleX*(double)width, y + scaleY*(double)height, 0).tex(maxU, maxV).endVertex();
		buffer.pos(x + scaleX*(double)width, y, 0).tex(maxU, minV).endVertex();
		buffer.pos(x, y, 0).tex(minU, minV).endVertex();
		buffer.pos(x, y + scaleY*(double)height, 0).tex(minU, maxV).endVertex();
		tessellator.draw();
	}
	
	public static void drawButtonHitBox(GuiButton button) {
		setupOpacity();
		Minecraft.getMinecraft().renderEngine.bindTexture(WHITE);
		int opacity = 80;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(button.x + button.width, button.y + button.height, 0).color(0, 255, 0, opacity).endVertex();
		buffer.pos(button.x + button.width, button.y, 0).color(0, 255, 0, opacity).endVertex();
		buffer.pos(button.x, button.y, 0).color(0, 255, 0, opacity).endVertex();
		buffer.pos(button.x, button.y + button.height, 0).color(0, 255, 0, opacity).endVertex();
		tessellator.draw();
		revertOpacity();
	}
	
	public static void drawButtonHitBox(GuiButton button, Color color) {
		setupOpacity();
		Minecraft.getMinecraft().renderEngine.bindTexture(WHITE);
		int opacity = 80;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(button.x + button.width, button.y + button.height, 0).color(color.getRed(), color.getGreen(), color.getBlue(), opacity).endVertex();
		buffer.pos(button.x + button.width, button.y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), opacity).endVertex();
		buffer.pos(button.x, button.y, 0).color(color.getRed(), color.getGreen(), color.getBlue(), opacity).endVertex();
		buffer.pos(button.x, button.y + button.height, 0).color(color.getRed(), color.getGreen(), color.getBlue(), opacity).endVertex();
		tessellator.draw();
		revertOpacity();
	}
	
    public static void setupOpacity() {
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
    }

    public static void revertOpacity() {
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    }
}