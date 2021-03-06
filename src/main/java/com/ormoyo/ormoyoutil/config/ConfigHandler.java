package com.ormoyo.ormoyoutil.config;

import com.ormoyo.ormoyoutil.OrmoyoUtil;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = OrmoyoUtil.MODID)
public class ConfigHandler {
	public static Stats STATS = new Stats();
	public static class Stats {
		@Config.Name("Max Level")
		@Config.RangeInt(min = 1, max = 999)
		public short maxLevel = 20;
		
		@Config.Name("Entity exp count")
		@Config.Comment("The amount of exp a player will get if he kills the entity")
		public String[] entityExpCount = {"minecraft:ender_dragon=100"};
	}
	
    @Mod.EventBusSubscriber(modid = OrmoyoUtil.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(OrmoyoUtil.MODID)) {
                ConfigManager.sync(OrmoyoUtil.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
