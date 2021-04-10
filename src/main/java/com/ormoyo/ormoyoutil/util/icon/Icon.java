package com.ormoyo.ormoyoutil.util.icon;

import com.ormoyo.ormoyoutil.OrmoyoUtil;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

@EventBusSubscriber(modid = OrmoyoUtil.MODID)
public class Icon {
	private static IForgeRegistry<IconEntry> ICON_REGISTRY;
	public static IForgeRegistry<IconEntry> getIconRegistry() {
		return ICON_REGISTRY;
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
	
	@EventBusSubscriber(modid = OrmoyoUtil.MODID)
	private static class EventHandler {
		@SubscribeEvent
		public static void onNewRegistry(RegistryEvent.NewRegistry event) {
			ICON_REGISTRY = new RegistryBuilder<IconEntry>().setName(new ResourceLocation(OrmoyoUtil.MODID, "icon")).setType(IconEntry.class).setIDRange(0, 2048).create();
		}
	}
}
