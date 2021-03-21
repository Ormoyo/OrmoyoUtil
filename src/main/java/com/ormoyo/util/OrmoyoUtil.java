package com.ormoyo.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ormoyo.util.commands.CommandUnlockAbility;
import com.ormoyo.util.proxy.CommonProxy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = OrmoyoUtil.MODID, name = OrmoyoUtil.NAME, version = OrmoyoUtil.VERSION, acceptedMinecraftVersions = OrmoyoUtil.MINECRAFT_VERSION)
public class OrmoyoUtil {
	public static final String MODID = "ormoyoutil";
	public static final String NAME = "Ormoyo Util";
	public static final String VERSION = "1.0";
	public static final String MINECRAFT_VERSION = "[1.12.2]";
	
	@Instance
	public static OrmoyoUtil instance;
	
	public static Logger LOGGER = LogManager.getLogger(OrmoyoUtil.MODID);
	
	@SidedProxy(serverSide = "com.ormoyo.util.proxy.CommonProxy", clientSide = "com.ormoyo.util.proxy.ClientProxy")
	public static CommonProxy proxy;
	
	public static SimpleNetworkWrapper NETWORK_WRAPPER;
	
	@EventHandler
	public static void PreInit(FMLPreInitializationEvent event) {
		proxy.preInit();
	}
	@EventHandler
	public static void init(FMLInitializationEvent event) {
		proxy.init();
	}
	@EventHandler
	public static void PostInit(FMLPostInitializationEvent event) {
		proxy.postInit();
	}
	
	@EventHandler
	public static void ServerStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandUnlockAbility());
	}
}
