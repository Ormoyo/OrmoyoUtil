package com.ormoyo.util.network;

import com.ormoyo.util.abilities.Ability;
import com.ormoyo.util.abilities.AbilityEntry;
import com.ormoyo.util.capability.CapabilityHandler;
import com.ormoyo.util.capability.IPlayerData;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageOnAbilityKeyRelease extends AbstractMessage<MessageOnAbilityKeyRelease> {
	AbilityEntry entry;
	
	public MessageOnAbilityKeyRelease() {
	}
	
	public MessageOnAbilityKeyRelease(Ability ability) {
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
	public void onClientReceived(Minecraft client, MessageOnAbilityKeyRelease message, EntityPlayer player, MessageContext messageContext) {
	}

	@Override
	public void onServerReceived(MinecraftServer server, MessageOnAbilityKeyRelease message, EntityPlayer player, MessageContext messageContext) {
		IPlayerData capability = player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
		for(Ability ability : capability.getUnlockedAbilities()) {
			if(ability.getEntry().equals(message.entry)) {
				ability.onKeyRelease();
			}
		}
	}
}
