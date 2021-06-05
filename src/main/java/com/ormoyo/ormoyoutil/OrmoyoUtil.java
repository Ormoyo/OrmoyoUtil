package com.ormoyo.ormoyoutil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ormoyo.ormoyoutil.commands.CommandUnlockAbility;
import com.ormoyo.ormoyoutil.network.MessageGetAbilitySyncedValueOnClient;
import com.ormoyo.ormoyoutil.network.MessageGetAbilitySyncedValueOnServer;
import com.ormoyo.ormoyoutil.network.MessageInvokeGetSyncedValueEventOnClient;
import com.ormoyo.ormoyoutil.network.MessageInvokeGetSyncedValueEventOnServer;
import com.ormoyo.ormoyoutil.network.MessageInvokeMethodOnClient;
import com.ormoyo.ormoyoutil.network.MessageInvokeMethodOnServer;
import com.ormoyo.ormoyoutil.network.MessageOnAbilityKeyPress;
import com.ormoyo.ormoyoutil.network.MessageOnAbilityKeyRelease;
import com.ormoyo.ormoyoutil.network.MessageSetClientAbilityList;
import com.ormoyo.ormoyoutil.network.MessageUpdateAbilitySyncedValueOnClient;
import com.ormoyo.ormoyoutil.network.MessageUpdateAbilitySyncedValueOnServer;
import com.ormoyo.ormoyoutil.network.NetworkHandler;
import com.ormoyo.ormoyoutil.network.NetworkWrapper;
import com.ormoyo.ormoyoutil.proxy.CommonProxy;
import com.ormoyo.ormoyoutil.util.InjectRender;
import com.ormoyo.ormoyoutil.util.Utils;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = OrmoyoUtil.MODID, name = OrmoyoUtil.NAME, version = OrmoyoUtil.VERSION, acceptedMinecraftVersions = OrmoyoUtil.MINECRAFT_VERSION)
public class OrmoyoUtil {
	public static final String MODID = "ormoyoutil";
	public static final String NAME = "Ormoyo Util";
	public static final String VERSION = "1.0";
	public static final String MINECRAFT_VERSION = "[1.12.2]";
	
	@Instance
	public static OrmoyoUtil instance;
	
	public static final Logger LOGGER = LogManager.getLogger(OrmoyoUtil.MODID);
	
	@SidedProxy(serverSide = "com.ormoyo.ormoyoutil.proxy.CommonProxy", clientSide = "com.ormoyo.ormoyoutil.proxy.ClientProxy")
	public static CommonProxy proxy;
	
	@NetworkWrapper({MessageInvokeGetSyncedValueEventOnServer.class, MessageInvokeGetSyncedValueEventOnClient.class, MessageGetAbilitySyncedValueOnServer.class, MessageGetAbilitySyncedValueOnClient.class, MessageInvokeMethodOnServer.class, MessageInvokeMethodOnClient.class, MessageUpdateAbilitySyncedValueOnServer.class, MessageUpdateAbilitySyncedValueOnClient.class, MessageOnAbilityKeyPress.class, MessageOnAbilityKeyRelease.class, MessageSetClientAbilityList.class})
	public static SimpleNetworkWrapper NETWORK_WRAPPER;
	
	@EventHandler
	public static void PreInit(FMLPreInitializationEvent event) {
		for(ModContainer mod : Loader.instance().getModList()) {
			NetworkHandler.injectNetworkWrapper(mod, event.getAsmData());
			InjectRender.Handler.injectRender(mod, event.getAsmData());
		}
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
