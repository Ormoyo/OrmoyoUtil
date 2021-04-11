package com.ormoyo.ormoyoutil.client.font;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;
import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.client.RenderHelper;
import com.ormoyo.ormoyoutil.event.FontRenderEvent;
import com.ormoyo.ormoyoutil.util.Font;
import com.ormoyo.ormoyoutil.util.Font.FontEntry;
import com.ormoyo.ormoyoutil.util.icon.Icon;
import com.ormoyo.ormoyoutil.util.resourcelocation.AdvancedResourceLocation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

public class FontHelper {
	private static Map<ResourceLocation, ResourceLocation> fontToTexture = Maps.newHashMap();
	private static Map<ResourceLocation, Dot> fontToDot = Maps.newHashMap();
	private static Map<ResourceLocation, Map<Integer, Char>> fontMap = Maps.newHashMap();
	
	public static void loadFont(ResourceLocation font, ResourceLocation texture) {
		InputStream stream = null;
		try {
			Map<Integer, Char> map = new HashMap<>();
			stream = Minecraft.getMinecraft().getResourceManager().getResource(font).getInputStream();
			Minecraft.getMinecraft().getResourceManager().getResource(texture);
			fontToTexture.put(font, texture);
			int dotX = -1;
			int dotY = -1;
			int dotWidth = -1;
			int dotHeight = -1;
			int dotXOffset = -1;
			int dotYOffset = -1;
			boolean isDefaultDot = false;
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			while((line=reader.readLine()) != null) {
				int id = -1;
				int x = -1;
				int y = -1;
				int width = -1;
				int height = -1;
				int xoffset = -1;
				int yoffset = -1;
				int xadvance = -1;
				if(line.startsWith("char")) {
					for(String string : line.split(" ")) {
						for(String s : string.split("=")) {
							switch(s) {
							case "id":
								id = Integer.parseInt(string.split("=")[1]);
							case "x":
								x = Integer.parseInt(string.split("=")[1]);
							case "y":
								y = Integer.parseInt(string.split("=")[1]);
							case "width":
								width = Integer.parseInt(string.split("=")[1]);
							case "height":
								height = Integer.parseInt(string.split("=")[1]);
							case "xoffset":
								xoffset = Integer.parseInt(string.split("=")[1]);
							case "yoffset":
								yoffset = Integer.parseInt(string.split("=")[1]);
							case "xadvance":
								xadvance = Integer.parseInt(string.split("=")[1]);
							}
						}
					}
				}
				if(id > 0 && x >= 0 && y >= 0 && width > 0 && height > 0) {
					map.put(id, new Char(x, y, width, height, xoffset, yoffset, xadvance));
				}
				
				if(line.startsWith("dot")) {
					for(String string : line.split(" ")) {
						for(String s : string.split("=")) {
							switch(s) {
							case "x":
								dotX = Integer.parseInt(string.split("=")[1]);
							case "y":
								dotY = Integer.parseInt(string.split("=")[1]);
							case "width":
								dotWidth = Integer.parseInt(string.split("=")[1]);
							case "height":
								dotHeight = Integer.parseInt(string.split("=")[1]);
							case "xoffset":
								dotXOffset = Integer.parseInt(string.split("=")[1]);
							case "yoffset":
								dotYOffset = Integer.parseInt(string.split("=")[1]);
							case "default":
								isDefaultDot = true;
							}
						}
					}
				}
			}
			fontMap.put(font, map);
			ResourceLocation dot = new ResourceLocation(texture.getResourceDomain(), texture.getResourcePath().substring(0, texture.getResourcePath().lastIndexOf('/') + 1) + "dot/" + ".png");
			if(dotX >= 0 && dotY >= 0 && dotWidth > 0 && dotHeight > 0 && !isDefaultDot) {
				Minecraft.getMinecraft().getResourceManager().getResource(dot);
				fontToDot.put(font, new Dot(dot, dotX, dotY, dotWidth, dotHeight, dotXOffset, dotYOffset));
			}
			if(isDefaultDot) {
				AdvancedResourceLocation defaultDot = new AdvancedResourceLocation(OrmoyoUtil.MODID, "textures/font/dot/default.png", 0, 3, 16, 10, 16, 16);
				fontToDot.put(font, new Dot(defaultDot, defaultDot.getU(), defaultDot.getV(), 
						defaultDot.getWidth(), defaultDot.getHeight(), 0, 0));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			IOUtils.closeQuietly(stream);
		}
	}
	
	public static void drawString(String text, double posX, double posY, double scale, Color color, Font.FontEntry font) {
		FontRenderEvent.Pre event = new FontRenderEvent.Pre(text, font);
		if(MinecraftForge.EVENT_BUS.post(event)) return;
		text = event.getText();
		font = event.getFont();
		double cursorX = 0;
		for(char character : text.toCharArray()) {
			Map<Integer, Char> c = fontMap.get(font.getResourceLocation());
			if(c != null) {
				Char chara = c.get((int)character);
				if(chara != null) {
					if(character == ' ') {
						cursorX += 2 * scale;
					}
					RenderHelper.drawTexturedRect(fontToTexture.get(font.getResourceLocation()), posX + cursorX - 4 * scale, posY + chara.yoffset * scale - 6 * scale, chara.x, chara.y, chara.width, chara.height, 256, 256, scale, color);
					cursorX += (chara.width - chara.xoffset - 9) * scale;
				}
			}
		}
		MinecraftForge.EVENT_BUS.post(new FontRenderEvent.Post(text, font));
	}
	
	public static void drawString(String text, double posX, double posY, double scale, Color color, Font.FontEntry font, double lineWidth) {
		FontRenderEvent.Pre event = new FontRenderEvent.Pre(text, font);
		if(MinecraftForge.EVENT_BUS.post(event)) return;
		text = event.getText();
		font = event.getFont();
		double cursorX = 0;
		double cursorY = 0;
		String[] words = text.split(" ");
		for(String word : words) {
			if(cursorX + getWordWidth(word, font, scale) > lineWidth) {
				if(word.length() <= 5) {
					cursorX = 0;
					cursorY += 25;
				}
			}
			for(char character : word.toCharArray()) {
				Map<Integer, Char> c = fontMap.get(font.getResourceLocation());
				if(c != null) {
					Char chara = c.get((int)character);
					if(chara != null) {
						if(cursorX + chara.width * scale > lineWidth) {
							cursorX = 0;
							cursorY += 25;
						}
						RenderHelper.drawTexturedRect(fontToTexture.get(font.getResourceLocation()), posX + cursorX - 4 * scale, (posY + chara.yoffset * scale - 6 * scale) + cursorY * scale, chara.x, chara.y, chara.width, chara.height, 256, 256, scale, color);
						cursorX += (chara.width - chara.xoffset - 9) * scale;
					}
				}
			}
			cursorX += 4 * scale;
		}
		MinecraftForge.EVENT_BUS.post(new FontRenderEvent.Post(text, font));
	}
	
	public static int getWordWidth(String word, Font.FontEntry font, double scale) {
		int width = 0;
		for(char character : word.toCharArray()) {
			Map<Integer, Char> c = fontMap.get(font.getResourceLocation());
			if(character == ' ') {
				width += 4 * scale;
			}
			if(c != null) {
				Char chara = c.get((int)character);
				if(chara != null) {
					width += (chara.width - chara.xoffset - 9) * scale;
				}
			}
		}
		return width;
	}
	
	public static boolean drawDot(Font.FontEntry font, int posX, int posY, double scale) {
		Dot dot = fontToDot.get(font.getResourceLocation());
		if(dot != null) {
			RenderHelper.drawTexturedRect(dot.location, posX + dot.xoffset, posY + dot.yoffset, dot.x, dot.y, dot.width, dot.height, dot.width, dot.height, scale);
			return true;
		}
		return false;
	}
	
	private static class Char {
		private final int x;
		private final int y;
		private final int width;
		private final int height;
		private final int xoffset;
		private final int yoffset;
		private final int xadvance;
		
		private Char(int x, int y, int width, int height, int xoffset, int yoffset, int xadvance) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.xoffset = xoffset;
			this.yoffset = yoffset;
			this.xadvance = xadvance;
		}
		
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(x);
			b.append(" ");
			b.append(y);
			b.append(" ");
			b.append(width);
			b.append(" ");
			b.append(height);
			b.append(" ");
			b.append(xoffset);
			b.append(" ");
			b.append(yoffset);
			b.append(" ");
			b.append(xadvance);
			return b.toString();
		}
	}
	
	private static class Dot {
		private final int x;
		private final int y;
		private final int width;
		private final int height;
		private final int xoffset;
		private final int yoffset;
		private final ResourceLocation location;
		private Dot(ResourceLocation location, int x, int y, int width, int height, int xoffset, int yoffset) {
			this.location = location;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.xoffset = xoffset;
			this.yoffset = yoffset;
		}
	}
}
