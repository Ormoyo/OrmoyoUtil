package com.ormoyo.ormoyoutil.network;

import java.util.HashSet;
import java.util.Set;

import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilityEntry;
import com.ormoyo.ormoyoutil.event.AbilityEvent;
import com.ormoyo.ormoyoutil.proxy.ClientProxy;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSetClientAbilityList extends AbstractMessage<MessageSetClientAbilityList> {
	AbilityEntry abilityEntry;

	public MessageSetClientAbilityList() {
	}
	
	public MessageSetClientAbilityList(Ability ability) {
		this.abilityEntry = ability.getEntry();
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.abilityEntry = Ability.getRegistry().getValue(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, abilityEntry.getRegistryName().toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClientReceived(Minecraft client, MessageSetClientAbilityList message, EntityPlayer player, MessageContext messageContext) {
		try {
			Set<Ability> set = (Set<Ability>)ObfuscationReflectionHelper.findField(ClientProxy.class, "unlockedAbilities").get(null);
			Ability ability = message.abilityEntry.newInstance(player);
			if(MinecraftForge.EVENT_BUS.post(new AbilityEvent.OnAbilityUnlockedEvent(player, ability))) return;
			set.add(ability);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onServerReceived(MinecraftServer server, MessageSetClientAbilityList message, EntityPlayer player, MessageContext messageContext) {
		
	}
}
