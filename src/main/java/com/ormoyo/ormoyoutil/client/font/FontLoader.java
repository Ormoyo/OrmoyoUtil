package com.ormoyo.ormoyoutil.client.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.client.font.FontHelper.Char;
import com.ormoyo.ormoyoutil.client.font.FontHelper.Dot;
import com.ormoyo.ormoyoutil.client.font.FontHelper.Page;
import com.ormoyo.ormoyoutil.util.FontHandler.FontType;
import com.ormoyo.ormoyoutil.util.resourcelocation.AdvancedResourceLocation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class FontLoader {
	public static void loadFont(FontType type, ResourceLocation font, boolean antiAliasing) {
		switch(type) {
		case FNT:
			loadFnt(font);
			break;
		case TTF:
			loadTTF(font, antiAliasing);
			break;
		case OTF:
			break;
		}
	}
	
	private static void loadFnt(ResourceLocation font) {
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
			int i = font.getResourcePath().lastIndexOf('.');
			ResourceLocation texture = new ResourceLocation(font.getResourceDomain(), "textures/" + font.getResourcePath().substring(0, i > 0 ? i : font.getResourcePath().length()) + ".png");
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
					int index = texture.getResourcePath().lastIndexOf('/');
					ResourceLocation l = new ResourceLocation(texture.getResourceDomain(), texture.getResourcePath().substring(0, index > -1 ? index + 1 : texture.getResourcePath().length()) + pageFileName);
					InputStream im = Minecraft.getMinecraft().getResourceManager().getResource(l).getInputStream();
					BufferedImage image = ImageIO.read(im);
					FontHelper.indexToPage.put(font, pageId, new Page(l, image.getWidth(), image.getHeight()));
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
				if(id > 0 && x >= 0 && y >= 0 && width > 0 && height > 0 && charPageId >= 0 && FontHelper.indexToPage.containsKey2(charPageId)) {
					FontHelper.charToChar.put(font, (char)id, new Char((char)id, x, y, width, height, xoffset, yoffset, charPageId));
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
				FontHelper.fontToDot.put(font, new Dot(dot, dotX, dotY, dotWidth, dotHeight, dotXOffset, dotYOffset));
			}
			if(isDefaultDot) {
				AdvancedResourceLocation defaultDot = new AdvancedResourceLocation(OrmoyoUtil.MODID, "textures/font/dot/default.png", 0, 3, 16, 10, 16, 16);
				FontHelper.fontToDot.put(font, new Dot(defaultDot, defaultDot.getU(), defaultDot.getV(), 
						defaultDot.getWidth(), defaultDot.getHeight(), 0, 0));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			IOUtils.closeQuietly(stream);
		}
	}
	
	private static void loadTTF(ResourceLocation font, boolean antiAliasing) {
		InputStream stream = null;
		try {
			stream = Minecraft.getMinecraft().getResourceManager().getResource(font).getInputStream();
			Font f = Font.createFont(Font.TRUETYPE_FONT, stream);
			f = f.deriveFont(32f);
	        try {
	            int rowHeight = 0;
	            int positionX = 0;
	            int positionY = 0;
	            Map<BufferedImage, Vector2f> positions = Maps.newHashMap();
	            for (char c = 0; c < Character.MAX_VALUE; c++) {
	            	if(!f.canDisplay(c)) continue;
	                BufferedImage fontImage = getFontImage(f, (char) c, false);
	                fontImage = crop(fontImage);
	                if (positionX + fontImage.getWidth() >= 256) {
	                    positionX = 0;
	                    positionY += rowHeight + 5;
	                    rowHeight = 0;
	                }
	                if (fontImage.getHeight() > rowHeight) {
	                    rowHeight = fontImage.getHeight();
	                }
	                FontHelper.charToChar.put(font, c, new Char(c, positionX, positionY, fontImage.getWidth(), fontImage.getHeight(), -2, 0, 0));
	                positions.put(fontImage, new Vector2f(positionX, positionY));
	                positionX += fontImage.getWidth() + 5;
	                fontImage = null;
	            }
	            List<Integer> intlist = Lists.newArrayList();
	            for(Char c : FontHelper.charToChar.values()) {
	            	intlist.add(c.height);
	            }
	            int popular = getPopularElement(intlist.stream().mapToInt(Integer::intValue).toArray());
	            for(Char c : FontHelper.charToChar.values()) {
	            	c.yoffset = popular - c.height;
	            }
	            BufferedImage img = new BufferedImage(256, positionY > 0 ? positionY : f.getSize(), BufferedImage.TYPE_INT_ARGB);
	            Graphics g = img.getGraphics();
	            g.setColor(new Color(1,1,1,1));
	            // Draw it here
	            positions.forEach((i, vec) -> g.drawImage(i, (int)vec.x, (int)vec.y, null));
	            FontHelper.ttfToImage.put(font, img);
	        } catch (Exception e) {
	            System.err.println("Failed to create font.");
	            e.printStackTrace();
	        }
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}finally {
			IOUtils.closeQuietly(stream);
		}
	}
	
	private static BufferedImage getFontImage(java.awt.Font font, char ch, boolean antiAliasing) {
		//Create a temporary image to extract font size
		BufferedImage tempfontImage = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D)tempfontImage.getGraphics();
        //// Add AntiAliasing /////
		if(antiAliasing) {
	        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
		}
        ///////////////////////////
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		int charwidth = fm.charWidth(ch);
		if (charwidth <= 0) {
			charwidth = 1;
		}
		int charheight = fm.getHeight();
		if (charheight <= 0) {
			charheight = font.getSize();
		}
		//Create another image for texture creation
		BufferedImage fontImage = new BufferedImage(charwidth,charheight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D gt = (Graphics2D)fontImage.getGraphics();
        //// Add AntiAliasing /////
		if(antiAliasing) {
	        gt.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                RenderingHints.VALUE_ANTIALIAS_ON);
		}
        ///////////////////////////
		gt.setFont(font);

		//// Uncomment these to fill in the texture with a background color
		//// (used for debugging)
		//gt.setColor(Color.RED);
		//gt.fillRect(0, 0, charwidth, fontsize);
		
		gt.setColor(Color.WHITE);
		int charx = 0;
		int chary = 0;
		gt.drawString(String.valueOf(ch), (charx), (chary) + fm.getAscent());
		return fontImage;
	}
	
	private static int getPopularElement(int[] a)
	{
	  int count = 1, tempCount;
	  int popular = a[0];
	  int temp = 0;
	  for (int i = 0; i < (a.length - 1); i++)
	  {
	    temp = a[i];
	    tempCount = 0;
	    for (int j = 1; j < a.length; j++)
	    {
	      if (temp == a[j])
	        tempCount++;
	    }
	    if (tempCount > count)
	    {
	      popular = temp;
	      count = tempCount;
	    }
	  }
	  return popular;
	}
	
	public static BufferedImage crop(BufferedImage image) {
	    int minY = 0, maxY = 0, minX = Integer.MAX_VALUE, maxX = 0;
	    boolean isBlank, minYIsDefined = false;
	    Raster raster = image.getRaster();
	    int[][] pixels = new int[image.getWidth()][image.getHeight()];

	    for( int i = 0; i < image.getWidth(); i++ )
	        for( int j = 0; j < image.getHeight(); j++ )
	            pixels[i][j] = image.getRGB( i, j );
	    List<Integer> pixel = Lists.newArrayList();
	    for(int[] i : pixels) {
	    	for(int il : i) {
	    		pixel.add(il);
	    	}
	    }
	    boolean isAllZero = true;
	    for(int p : pixel) {
	    	if(isAllZero) {
	    		isAllZero = p == 0;
	    	}
	    }
	    if(isAllZero) return image;
	    for (int y = 0; y < image.getHeight(); y++) {
	        isBlank = true;

	        for (int x = 0; x < image.getWidth(); x++) {
	            //Change condition to (raster.getSample(x, y, 3) != 0) 
	            //for better performance
	            if (raster.getPixel(x, y, (int[]) null)[3] != 0) {
	                isBlank = false;

	                if (x < minX) minX = x;
	                if (x > maxX) maxX = x;
	            }
	        }

	        if (!isBlank) {
	            if (!minYIsDefined) {
	                minY = y;
	                minYIsDefined = true;
	            } else {
	                if (y > maxY) maxY = y;
	            }
	        }
	    }

	    return image.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
	}
}
