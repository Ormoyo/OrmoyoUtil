package com.ormoyo.util.network;

import com.ormoyo.util.OrmoyoUtil;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
	public static void registerMessages() {
		int id = 0;
		OrmoyoUtil.NETWORK_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(OrmoyoUtil.MODID);
		OrmoyoUtil.NETWORK_WRAPPER.registerMessage(MessageInvokeMethodOnClient.class, MessageInvokeMethodOnClient.class, id++, Side.CLIENT);
		OrmoyoUtil.NETWORK_WRAPPER.registerMessage(MessageInvokeMethodOnServer.class, MessageInvokeMethodOnServer.class, id++, Side.SERVER);
		OrmoyoUtil.NETWORK_WRAPPER.registerMessage(MessageOnAbilityKeyPress.class, MessageOnAbilityKeyPress.class, id++, Side.SERVER);
		OrmoyoUtil.NETWORK_WRAPPER.registerMessage(MessageOnAbilityKeyRelease.class, MessageOnAbilityKeyRelease.class, id++, Side.SERVER);
		OrmoyoUtil.NETWORK_WRAPPER.registerMessage(MessageSetClientAbilityList.class, MessageSetClientAbilityList.class, id++, Side.CLIENT);
		OrmoyoUtil.NETWORK_WRAPPER.registerMessage(MessageUpdateAbilitySyncedValueToClient.class, MessageUpdateAbilitySyncedValueToClient.class, id++, Side.CLIENT);
	}
}
