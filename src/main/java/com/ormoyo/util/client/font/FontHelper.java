package com.ormoyo.util.client.font;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.ormoyo.util.OrmoyoUtil;
import com.ormoyo.util.client.RenderHelper;
import com.ormoyo.util.event.FontRenderEvent;
import com.ormoyo.util.icon.Icon;
import com.ormoyo.util.icon.Icon.FontEntry;
import com.ormoyo.util.resourcelocation.AdvancedResourceLocation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class FontHelper {
	private static Map<ResourceLocation, ResourceLocation> fontToTexture;
	private static Map<ResourceLocation, Dot> fontToDot;
	private static Map<ResourceLocation, Map<Integer, Char>> fontMap = new HashMap<>();
	
	public static void loadFonts() {
		fontToTexture = new HashMap<>(Icon.getFontRegistry().getValuesCollection().size());
		fontToDot = new HashMap<>(Icon.getFontRegistry().getValuesCollection().size());
		BufferedReader reader = null;
		try {
			for(FontEntry entry : Icon.getFontRegistry().getValuesCollection()) {
				ResourceLocation location = entry.getResourceLocation();
				Map<Integer, Char> map = new HashMap<>();
				InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
				ResourceLocation tex = entry.getTextureResourceLocation();
				Minecraft.getMinecraft().getResourceManager().getResource(tex);
				fontToTexture.put(location, tex);
				int dotX = -1;
				int dotY = -1;
				int dotWidth = -1;
				int dotHeight = -1;
				int dotXOffset = -1;
				int dotYOffset = -1;
				boolean isDefaultDot = false;
				reader = new BufferedReader(new InputStreamReader(stream));
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
							if(string.startsWith("id=")) {
								id = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("x=")) {
								x = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("y=")) {
								y = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("width=")) {
								width = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("height=")) {
								height = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("xoffset=")) {
								xoffset = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("yoffset=")) {
								yoffset = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("xadvance=")) {
								xadvance = Integer.parseInt(string.split("=")[1]);
							}
						}
					}
					
					if(id > 0 && x >= 0 && y >= 0 && width > 0 && height > 0) {
						map.put(id, new Char(x, y, width, height, xoffset, yoffset, xadvance));
					}
					
					if(line.startsWith("dot")) {
						for(String string : line.split(" ")) {
							if(string.startsWith("x=")) {
								dotX = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("y=")) {
								dotY = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("width=")) {
								dotWidth = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("height=")) {
								dotHeight = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("xoffset=")) {
								dotXOffset = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("yoffset=")) {
								dotYOffset = Integer.parseInt(string.split("=")[1]);
							}else if(string.startsWith("default")) {
								isDefaultDot = true;
							}
						}
					}
				}
				fontMap.put(location, map);
				ResourceLocation dot = new ResourceLocation(location.getResourceDomain(), "textures/font/dot/" + entry.getRegistryName().getResourcePath() + ".png");
				if(dotX >= 0 && dotY >= 0 && dotWidth > 0 && dotHeight > 0 && !isDefaultDot) {
					Minecraft.getMinecraft().getResourceManager().getResource(dot);
					fontToDot.put(location, new Dot(dot, dotX, dotY, dotWidth, dotHeight, dotXOffset, dotYOffset));
				}
				if(isDefaultDot) {
					AdvancedResourceLocation defaultDot = new AdvancedResourceLocation(OrmoyoUtil.MODID, "textures/font/dot/default.png", 0, 3, 16, 10, 16, 16);
					fontToDot.put(location, new Dot(defaultDot, defaultDot.getU(), defaultDot.getV(), 
							defaultDot.getWidth(), defaultDot.getHeight(), 0, 0));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void drawString(String text, double posX, double posY, double scale, Color color, FontEntry font) {
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
	
	public static void drawString(String text, double posX, double posY, double scale, Color color, FontEntry font, double lineWidth) {
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
	
	public static int getWordWidth(String word, FontEntry font, double scale) {
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
	
	public static boolean drawDot(FontEntry font, int posX, int posY, double scale) {
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
