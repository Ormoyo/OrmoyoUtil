package com.ormoyo.ormoyoutil.client.font;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.io.IOUtils;

import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.client.RenderHelper;
import com.ormoyo.ormoyoutil.event.FontRenderEvent;
import com.ormoyo.ormoyoutil.util.DoubleKeyMap;
import com.ormoyo.ormoyoutil.util.Font.FontEntry;
import com.ormoyo.ormoyoutil.util.ListDoubleKeyMap;
import com.ormoyo.ormoyoutil.util.ListHashMap;
import com.ormoyo.ormoyoutil.util.resourcelocation.AdvancedResourceLocation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class FontHelper {
	private static final ListHashMap<ResourceLocation, Dot> fontToDot = new ListHashMap<>();
	private static final DoubleKeyMap<ResourceLocation, Integer, ResourceLocation> indexToPage = new ListDoubleKeyMap<>();
	private static final DoubleKeyMap<ResourceLocation, Character, Char> charToChar = new ListDoubleKeyMap<>();
	
	public static void loadFont(ResourceLocation font, ResourceLocation texture) {
		InputStream stream = null;
		try {
			stream = Minecraft.getMinecraft().getResourceManager().getResource(font).getInputStream();
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
				String pageFileName = "";
				int pageId = -1;
				int id = -1;
				int x = -1;
				int y = -1;
				int width = -1;
				int height = -1;
				int xoffset = -1;
				int yoffset = -1;
				int charPageId = -1;
				if(line.startsWith("page")) {
					for(String string : line.split(" ")) {
						if(string.startsWith("id=")) {
							pageId = Integer.parseInt(string.split("=")[1]);
						}else if(string.startsWith("file=")) {
							pageFileName = string.split("=")[1].replace("\"", "");
						}
					}
				}
				if(pageId > -1 && !pageFileName.isEmpty()) {
					ResourceLocation l = new ResourceLocation(texture.getResourceDomain(), texture.getResourcePath().substring(0, texture.getResourcePath().lastIndexOf('/') + 1) + pageFileName);
					Minecraft.getMinecraft().getResourceManager().getResource(l);
					indexToPage.put(font, pageId, l);
				}
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
						}else if(string.startsWith("page=")) {
							charPageId = Integer.parseInt(string.split("=")[1]);
						}
					}
				}
				if(id > 0 && x >= 0 && y >= 0 && width > 0 && height > 0 && charPageId >= 0 && indexToPage.containsKey2(charPageId)) {
					charToChar.put(font, (char)id, new Char((char)id, x, y, width, height, xoffset, yoffset, charPageId));
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
	
	public static FontRenderer createFontRendererFromFont(FontEntry font) {
		FontRenderer renderer = new FontRenderer(null, null, null, false) {
			@Override
			public int drawString(String text, float x, float y, int color, boolean dropShadow) {
		        int i;
		        if (dropShadow)
		        {
		            i = this.renderString(text, x + 1.0F, y + 1.0F, color, true);
		            i = Math.max(i, this.renderString(text, x, y, color, false));
		        }
		        else
		        {
		            i = this.renderString(text, x, y, color, false);
		        }

		        return i;
			}
			
			private int renderString(String text, float x, float y, int color, boolean dropShadow) {
	            if ((color & -67108864) == 0)
	            {
	                color |= -16777216;
	            }
	            
	            if (dropShadow)
	            {
	                color = (color & 16579836) >> 2 | color & -16777216;
	            }

	            float red = (float)(color >> 16 & 255) / 255.0F;
	            float blue = (float)(color >> 8 & 255) / 255.0F;
	            float green = (float)(color & 255) / 255.0F;
	            float alpha = (float)(color >> 24 & 255) / 255.0F;
				FontHelper.drawString(text, x, y, 0.5, new Color(red, blue, green, alpha), font);
				return (int) x;
			}
		};
		return renderer;
	}
	
	public static void drawString(String text, double posX, double posY, double scale, Color color, FontEntry font) {
		FontRenderEvent.Pre event = new FontRenderEvent.Pre(text, font);
		if(MinecraftForge.EVENT_BUS.post(event)) return;
		text = event.getText();
		font = event.getFont();
		double cursorX = 0;
		for(char character : text.toCharArray()) {
			Char chara = charToChar.get(font.getResourceLocation(), character);
			if(chara != null && chara.ch == character) {
				if(character == ' ') {
					cursorX += 2 * scale;
				}
				
				RenderHelper.drawTexturedRect(indexToPage.get(font.getResourceLocation(), chara.page), posX + cursorX - 4 * scale, posY + chara.yoffset * scale - 6 * scale, chara.x, chara.y, chara.width, chara.height, 256, 256, scale, color);
				cursorX += (chara.width - chara.xoffset - 9) * scale;
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
				Char chara = charToChar.get(font.getResourceLocation(), character);
				if(chara != null) {
					if(cursorX + chara.width * scale > lineWidth) {
						cursorX = 0;
						cursorY += 25;
					}
					RenderHelper.drawTexturedRect(indexToPage.get(font.getResourceLocation(), chara.page), posX + cursorX - 4 * scale, (posY + chara.yoffset * scale - 6 * scale) + cursorY * scale, chara.x, chara.y, chara.width, chara.height, 256, 256, scale, color);
					cursorX += (chara.width - chara.xoffset - 9) * scale;
				}
			}
			cursorX += 4 * scale;
		}
		MinecraftForge.EVENT_BUS.post(new FontRenderEvent.Post(text, font));
	}
	
	public static int getWordWidth(String word, FontEntry font, double scale) {
		int width = 0;
		for(char character : word.toCharArray()) {
			Char chara = charToChar.get(font.getResourceLocation(), character);
			if(character == ' ') {
				width += 4 * scale;
			}
			if(chara != null) {
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
		private final char ch;
		private final int x;
		private final int y;
		private final int width;
		private final int height;
		private final int xoffset;
		private final int yoffset;
		private final int page;
		
		private Char(char ch, int x, int y, int width, int height, int xoffset, int yoffset, int pageIndex) {
			this.ch = ch;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.xoffset = xoffset;
			this.yoffset = yoffset;
			this.page = pageIndex;
		}
		
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append("char=");
			b.append((int)this.ch);
			b.append(" ");
			b.append("x=");
			b.append(this.x);
			b.append(" ");
			b.append("y=");
			b.append(this.y);
			b.append(" ");
			b.append("width=");
			b.append(this.width);
			b.append(" ");
			b.append("height=");
			b.append(this.height);
			b.append(" ");
			b.append("xoffset=");
			b.append(this.xoffset);
			b.append(" ");
			b.append("yoffset=");
			b.append(this.yoffset);
			b.append(" ");
			b.append("page=");
			b.append(this.page);
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
