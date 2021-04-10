package com.ormoyo.ormoyoutil.util;

import com.ormoyo.ormoyoutil.OrmoyoUtil;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;
import net.minecraftforge.registries.RegistryBuilder;

public class Font {
	private static final FontEntry defaultFont = new Font.FontEntry(new ResourceLocation(OrmoyoUtil.MODID, "undertale"));
	
	private static IForgeRegistry<FontEntry> FONT_REGISTRY;
	public static IForgeRegistry<FontEntry> getFontRegistry() {
		return FONT_REGISTRY;
	}

	public static FontEntry getDefaultFont() {
		return defaultFont;
	}
	
	public static class FontEntry extends Impl<FontEntry> {
		public FontEntry(ResourceLocation name) {
			this.setRegistryName(name);
		}
		
		public FontEntry(String name) {
			this.setRegistryName(name);
		}
		
		public ResourceLocation getResourceLocation() {
			return this.getFntResourceLocation();
		}
		
		protected ResourceLocation getFntResourceLocation() {
			return new ResourceLocation(this.getRegistryName().getResourceDomain(), "font/" + this.getRegistryName().getResourcePath() + ".fnt");
		}
		
		public ResourceLocation getTextureResourceLocation() {
			return new ResourceLocation(this.getRegistryName().getResourceDomain(), "textures/font/" + this.getRegistryName().getResourcePath() + ".png");
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
			FONT_REGISTRY = new RegistryBuilder<FontEntry>().setName(new ResourceLocation(OrmoyoUtil.MODID, "font")).setType(FontEntry.class).setIDRange(0, 2048).create();
		}
		
		@SubscribeEvent
		public static void registerFonts(RegistryEvent.Register<FontEntry> event) {
			event.getRegistry().register(defaultFont);
		}
	}
}
