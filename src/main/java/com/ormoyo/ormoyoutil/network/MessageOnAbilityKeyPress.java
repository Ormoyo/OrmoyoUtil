package com.ormoyo.ormoyoutil.network;

import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilityEntry;
import com.ormoyo.ormoyoutil.capability.CapabilityHandler;
import com.ormoyo.ormoyoutil.capability.IAbiltyData;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageOnAbilityKeyPress extends AbstractMessage<MessageOnAbilityKeyPress> {
	AbilityEntry entry;

	public MessageOnAbilityKeyPress() {
	}
	
	public MessageOnAbilityKeyPress(Ability ability) {
		this.entry = ability.getEntry();
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.entry = Ability.getRegistry().getValue(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, entry.toString());
	}

	@Override
	public void onClientReceived(Minecraft client, MessageOnAbilityKeyPress message, EntityPlayer player, MessageContext messageContext) {
		
	}

	@Override
	public void onServerReceived(MinecraftServer server, MessageOnAbilityKeyPress message, EntityPlayer player, MessageContext messageContext) {
		IAbiltyData capability = player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
		for(Ability ability : capability.getUnlockedAbilities()) {
			if(ability.getEntry().equals(message.entry)) {
				ability.onKeyPress();
			}
		}
	}
}
