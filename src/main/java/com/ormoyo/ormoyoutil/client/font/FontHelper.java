package com.ormoyo.ormoyoutil.client.font;

import java.awt.Color;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.common.collect.Maps;
import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.client.RenderHelper;
import com.ormoyo.ormoyoutil.event.FontRenderEvent;
import com.ormoyo.ormoyoutil.util.DoubleKeyMap;
import com.ormoyo.ormoyoutil.util.FontHandler.Font;
import com.ormoyo.ormoyoutil.util.DoubleKeyListMap;
import com.ormoyo.ormoyoutil.util.ListHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class FontHelper {
	static final ListHashMap<ResourceLocation, Dot> fontToDot = new ListHashMap<>();
	static final DoubleKeyMap<ResourceLocation, Integer, Page> indexToPage = new DoubleKeyListMap<>();
	static final DoubleKeyMap<ResourceLocation, Character, Char> charToChar = new DoubleKeyListMap<>();
	static final Map<ResourceLocation, BufferedImage> ttfToImage = Maps.newHashMap();
	static final Map<ResourceLocation, ResourceLocation> fontToTTF = Maps.newHashMap();
	
	public static FontRenderer createFontRendererFromFont(Font font) {
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
				FontHelper.drawString(font, text, x, y, 0.5, new Color(red, blue, green, alpha));
				return (int) x;
			}
		};
		return renderer;
	}
	
	public static void drawString(Font font, String text, double posX, double posY, double scale, Color color) {
		FontRenderEvent.Pre event = new FontRenderEvent.Pre(text, font);
		if(MinecraftForge.EVENT_BUS.post(event)) return;
		text = event.getText();
		font = event.getFont();
		double cursorX = 0;
		double d = 0;
		for(char character : text.toCharArray()) {
			Char chara = charToChar.get(font.getResourceLocation(), character);
			if(chara == null && !charToChar.containsKey2(character)) OrmoyoUtil.LOGGER.error("The font " + font.getRegistryName() + " doesn't have the character " + character + " in it's character set");
			if(chara != null) {
				if(character == ' ') {
					cursorX += 2 * scale;
				}
				
				GlStateManager.enableBlend();
				RenderHelper.setupOpacity();
				switch(font.getType()) {
				case FNT:
					int height = getWordHeight(text, font, scale);
					Page page = indexToPage.get(font.getResourceLocation(), chara.page);
					RenderHelper.drawTexturedRect(page.texture, posX + cursorX - 4 * scale, posY + chara.yoffset * scale - 6 * scale + (height - chara.height) * scale, chara.x, chara.y, chara.width, chara.height, page.width, page.height, scale, color);
					cursorX += (chara.width - chara.xoffset - 7) * scale;
					break;
				case TTF:
					int height1 = getWordHeight(text, font, 1);
					double h = (height1 - chara.height) * scale;
					if(h != 0 && d < h) {
						d = h;
					}
					ResourceLocation location = fontToTTF.getOrDefault(font.getResourceLocation(), TextureManager.RESOURCE_LOCATION_EMPTY);
					BufferedImage image = ttfToImage.get(font.getResourceLocation());
					if(location == TextureManager.RESOURCE_LOCATION_EMPTY) {
						if(image != null) {
							DynamicTexture tex = new DynamicTexture(image);
							location = Minecraft.getMinecraft().getRenderManager().renderEngine.getDynamicTextureLocation(font.getResourceLocation().toString(), tex);
							fontToTTF.put(font.getResourceLocation(), location);
						}
					}
					RenderHelper.drawTexturedRect(location, posX + cursorX, posY + chara.yoffset * scale + d * scale, chara.x, chara.y, chara.width, chara.height, image.getWidth(), image.getHeight(), scale, color);
					cursorX += (chara.width - chara.xoffset) * scale;
					break;
				case OTF:
					int height2 = getWordHeight(text, font, scale);
					double h1 = height2 - chara.height * scale;
					if(h1 != 0 && d < h1) {
						d = h1;
					}
					ResourceLocation location1 = fontToTTF.getOrDefault(font.getResourceLocation(), TextureManager.RESOURCE_LOCATION_EMPTY);
					BufferedImage image1 = ttfToImage.get(font.getResourceLocation());
					if(location1 == TextureManager.RESOURCE_LOCATION_EMPTY) {
						if(image1 != null) {
							DynamicTexture tex = new DynamicTexture(image1);
							location1 = Minecraft.getMinecraft().getRenderManager().renderEngine.getDynamicTextureLocation(font.getResourceLocation().toString(), tex);
							fontToTTF.put(font.getResourceLocation(), location1);
						}
					}
					RenderHelper.drawTexturedRect(location1, posX + cursorX, posY + chara.yoffset * scale + d * scale, chara.x, chara.y, chara.width, chara.height, image1.getWidth(), image1.getHeight(), scale, color);
					cursorX += (chara.width - chara.xoffset) * scale;
					break;
				}
				GlStateManager.disableBlend();
			}
		}
		MinecraftForge.EVENT_BUS.post(new FontRenderEvent.Post(text, font));
	}
	
	public static void drawString(String text, double posX, double posY, double scale, Color color, Font font, double lineWidth) {
		FontRenderEvent.Pre event = new FontRenderEvent.Pre(text, font);
		if(MinecraftForge.EVENT_BUS.post(event)) return;
		text = event.getText();
		font = event.getFont();
		double cursorX = 0;
		double cursorY = 0;
		String[] words = text.split(" ");
		for(String word : words) {
			if(cursorX + getWordWidth(word, font, scale) > lineWidth) {
				if(word.length() < 5) {
					cursorX = 0;
					cursorY += 25;
				}
			}
			for(char character : word.toCharArray()) {
				Char chara = charToChar.get(font.getResourceLocation(), character);
				if(chara == null && !charToChar.containsKey2(character)) OrmoyoUtil.LOGGER.error("The font " + font.getRegistryName() + " doesn't have the character " + character + " in it's character set");
				if(chara != null) {
					if(character == ' ') {
						cursorX += 2 * scale;
					}
					
					GlStateManager.enableBlend();
					RenderHelper.setupOpacity();
					switch(font.getType()) {
					case FNT:
						int height = getWordHeight(word, font, scale);
						Page page = indexToPage.get(font.getResourceLocation(), chara.page);
						RenderHelper.drawTexturedRect(page.texture, posX + cursorX - 4 * scale, posY + chara.yoffset * scale - 6 * scale + (height - chara.height) * scale, chara.x, chara.y, chara.width, chara.height, page.width, page.height, scale, color);
						break;
					case TTF:
						int height1 = getWordHeight(word, font, scale);
						ResourceLocation location = fontToTTF.getOrDefault(font.getResourceLocation(), TextureManager.RESOURCE_LOCATION_EMPTY);
						BufferedImage image = ttfToImage.get(font.getResourceLocation());
						if(location == TextureManager.RESOURCE_LOCATION_EMPTY) {
							if(image != null) {
								DynamicTexture tex = new DynamicTexture(image);
								location = Minecraft.getMinecraft().getRenderManager().renderEngine.getDynamicTextureLocation(font.getResourceLocation().toString(), tex);
								fontToTTF.put(font.getResourceLocation(), location);
							}
						}
						RenderHelper.drawTexturedRect(location, posX + cursorX - 4 * scale, posY + cursorY * scale + chara.yoffset * scale - 6 * scale + (height1 - chara.height) * scale, chara.x, chara.y, chara.width, chara.height, image.getWidth(), image.getHeight(), scale, color);
						break;
					case OTF:
						int height2 = getWordHeight(word, font, scale);
						ResourceLocation location1 = fontToTTF.getOrDefault(font.getResourceLocation(), TextureManager.RESOURCE_LOCATION_EMPTY);
						BufferedImage image1 = ttfToImage.get(font.getResourceLocation());
						if(location1 == TextureManager.RESOURCE_LOCATION_EMPTY) {
							if(image1 != null) {
								DynamicTexture tex = new DynamicTexture(image1);
								location1 = Minecraft.getMinecraft().getRenderManager().renderEngine.getDynamicTextureLocation(font.getResourceLocation().toString(), tex);
								fontToTTF.put(font.getResourceLocation(), location1);
							}
						}
						RenderHelper.drawTexturedRect(location1, posX + cursorX - 4 * scale, posY + cursorY * scale + chara.yoffset * scale - 6 * scale + (height2 - chara.height) * scale, chara.x, chara.y, chara.width, chara.height, image1.getWidth(), image1.getHeight(), scale, color);
						break;
					}
					GlStateManager.disableBlend();
					cursorX += (chara.width - chara.xoffset) * scale;
				}
			}
			cursorX += 4 * scale;
		}
		MinecraftForge.EVENT_BUS.post(new FontRenderEvent.Post(text, font));
	}
	
	public static int getWordWidth(String word, Font font, double scale) {
		int width = 0;
		for(int i = 0; i < word.toCharArray().length; i++) {
			char character = word.charAt(i);
			Char chara = charToChar.get(font.getResourceLocation(), character);
			if(chara == null && !charToChar.containsKey2(character)) OrmoyoUtil.LOGGER.error("The font " + font.getRegistryName() + " doesn't have the character " + character + " in it's character set");
			if(chara != null) {
				if(word.toCharArray().length == 1) {
					width += chara.width * scale;
				}else {
					if(i == word.toCharArray().length - 1) {
						width += (chara.width - chara.xoffset) * scale - 2;
					}else {
						width += Math.round((chara.width - chara.xoffset) * scale);
					}
				}
			}
		}
		return width;
	}
	
	public static int getWordHeight(String word, Font font, double scale) {
		int height = 0;
		for(char character : word.toCharArray()) {
			Char chara = charToChar.get(font.getResourceLocation(), character);
			if(chara == null && !charToChar.containsKey2(character)) OrmoyoUtil.LOGGER.error("The font " + font.getRegistryName() + " doesn't have the character " + character + " in it's character set");
			if(chara != null) {
				if(height < chara.height) {
					height = (int) Math.round(chara.height * scale);
				}
			}
		}
		return height;	
	}
	
	public static boolean drawDot(Font font, int posX, int posY, double scale) {
		Dot dot = fontToDot.get(font.getResourceLocation());
		if(dot != null) {
			RenderHelper.drawTexturedRect(dot.location, posX + dot.xoffset, posY + dot.yoffset, dot.x, dot.y, dot.width, dot.height, dot.width, dot.height, scale);
			return true;
		}
		return false;
	}
	
	static class Char {
		final char ch;
		final int x;
		final int y;
		final int width;
		final int height;
		int xoffset;
		int yoffset;
		final int page;
		
		Char(char ch, int x, int y, int width, int height, int xoffset, int yoffset, int pageIndex) {
			this.ch = ch;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.xoffset = xoffset;
			this.yoffset = yoffset;
			this.page = pageIndex;
		}
		
		public void setYOffset(int yoffset) {
			this.yoffset = yoffset;
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
	
	static class Dot {
		private final int x;
		private final int y;
		private final int width;
		private final int height;
		private final int xoffset;
		private final int yoffset;
		private final ResourceLocation location;
		Dot(ResourceLocation location, int x, int y, int width, int height, int xoffset, int yoffset) {
			this.location = location;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.xoffset = xoffset;
			this.yoffset = yoffset;
		}
	}
	
	static class Page {
		final ResourceLocation texture;
		final int width;
		final int height;
		public Page(ResourceLocation texture, int width, int height) {
			this.texture = texture;
			this.width = width;
			this.height = height;
		}
	}
}
