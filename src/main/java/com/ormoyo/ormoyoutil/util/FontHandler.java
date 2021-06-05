package com.ormoyo.ormoyoutil.util;

import com.ormoyo.ormoyoutil.OrmoyoUtil;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;
import net.minecraftforge.registries.RegistryBuilder;

public class FontHandler {
	private static final Font defaultFont = new FontHandler.Font(new ResourceLocation(OrmoyoUtil.MODID, "undertale"), FontType.FNT);
	public static final Font fnf = new Font(new ResourceLocation(OrmoyoUtil.MODID, "vcr"), FontType.TTF);
	
	private static IForgeRegistry<Font> FONT_REGISTRY;
	public static IForgeRegistry<Font> getFontRegistry() {
		return FONT_REGISTRY;
	}

	public static Font getDefaultFont() {
		return defaultFont;
	}
	
	public static enum FontType {
		TTF,
		OTF,
		FNT;
	}
	
	public static class Font extends Impl<Font> {
		private final FontType type;
		private final boolean antiAliasing;
		public Font(ResourceLocation name, FontType type) {
			this.setRegistryName(name);
			this.type = type;
			this.antiAliasing = true;
		}
		
		public Font(String name, FontType type) {
			this.setRegistryName(name);
			this.type = type;
			this.antiAliasing = true;
		}
		
		public Font(ResourceLocation name, FontType type, boolean antiAliasing) {
			this.setRegistryName(name);
			this.type = type;
			this.antiAliasing = true;
		}
		
		public Font(String name, FontType type, boolean antiAliasing) {
			this.setRegistryName(name);
			this.type = type;
			this.antiAliasing = antiAliasing;
		}
		
		public ResourceLocation getResourceLocation() {
			int i = this.getRegistryName().getResourcePath().lastIndexOf('.');
			return new ResourceLocation(this.getRegistryName().getResourceDomain(), "font/" + this.getRegistryName().getResourcePath().substring(0, i > -1 ? i : this.getRegistryName().getResourcePath().length()) + "." + this.type.name().toLowerCase());
		}
		
		public boolean isAntiAliasing() {
			return this.antiAliasing;
		}
		
		public FontType getType() {
			return this.type;
		}
		
		@Override
		public String toString() {
			return this.getRegistryName().toString();
		}
	}
	
	@EventBusSubscriber(modid = OrmoyoUtil.MODID)
	private static class EventHandler {
		@SubscribeEvent
		public static void onNewRegistry(RegistryEvent.NewRegistry event) {
			FONT_REGISTRY = new RegistryBuilder<Font>().setName(new ResourceLocation(OrmoyoUtil.MODID, "font")).setType(Font.class).setIDRange(0, 2048).create();
		}
		
		@SubscribeEvent
		public static void registerFonts(RegistryEvent.Register<Font> event) {
			event.getRegistry().register(defaultFont);
			event.getRegistry().register(fnf);
		}
	}
	
	
}
