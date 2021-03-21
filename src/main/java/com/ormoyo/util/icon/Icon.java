package com.ormoyo.util.icon;

import com.ormoyo.util.OrmoyoUtil;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

@EventBusSubscriber
public class Icon {
	private static final FontEntry defaultFont = new FontEntry(new ResourceLocation(OrmoyoUtil.MODID, "undertale"));
	
	private static IForgeRegistry<IconEntry> ICON_REGISTRY;
	public static IForgeRegistry<IconEntry> getIconRegistry() {
		return ICON_REGISTRY;
	}
	
	private static IForgeRegistry<FontEntry> FONT_REGISTRY;
	public static IForgeRegistry<FontEntry> getFontRegistry() {
		return FONT_REGISTRY;
	}
	
	@SubscribeEvent
	public static void onNewRegistry(RegistryEvent.NewRegistry event) {
		ICON_REGISTRY = new RegistryBuilder<IconEntry>().setName(new ResourceLocation(OrmoyoUtil.MODID, "icon")).setType(IconEntry.class).setIDRange(0, 2048).create();
		FONT_REGISTRY = new RegistryBuilder<FontEntry>().setName(new ResourceLocation(OrmoyoUtil.MODID, "font")).setType(FontEntry.class).setIDRange(0, 2048).create();
	}
	
	@SubscribeEvent
	public static void registerIcons(RegistryEvent.Register<IconEntry> event) {
	}
	
	@SubscribeEvent
	public static void registerFonts(RegistryEvent.Register<FontEntry> event) {
		event.getRegistry().register(defaultFont);
	}
	
	public static FontEntry getDefaultFont() {
		return defaultFont;
	}
	
	public static class IconEntry extends Impl<IconEntry> {
		private IIcon icon;
		
		public IconEntry(IIcon icon, ResourceLocation name) {
			this.icon = icon;
			this.setRegistryName(name);
		}
		
		public IconEntry(IIcon icon, String name) {
			this.icon = icon;
			this.setRegistryName(name);
		}
		
		public IIcon getIcon() {
			return this.icon;
		}
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
}
