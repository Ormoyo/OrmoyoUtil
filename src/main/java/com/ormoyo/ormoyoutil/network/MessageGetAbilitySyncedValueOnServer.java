package com.ormoyo.ormoyoutil.network;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.AbilitySyncedValueParserEntry;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser.Reader;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser.Writer;
import com.ormoyo.ormoyoutil.capability.CapabilityHandler;
import com.ormoyo.ormoyoutil.capability.IAbilityData;
import com.ormoyo.ormoyoutil.event.AbilityEvent;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageGetAbilitySyncedValueOnServer extends AbstractMessage<MessageGetAbilitySyncedValueOnServer> {
	ResourceLocation ability;
	String name;
	Class<?> superClass;
	UUID id;
	EntityPlayer player;
	
	public MessageGetAbilitySyncedValueOnServer() {
	}
	
	public MessageGetAbilitySyncedValueOnServer(Ability ability, String fieldName, UUID id, EntityPlayer player) {
		this.ability = ability.getRegistryName();
		this.name = fieldName;
		this.id = id;
		this.player = player;
	}
	
	public MessageGetAbilitySyncedValueOnServer(Ability ability, Class<?> superClass, String fieldName, UUID id, EntityPlayer player) {
		this.ability = ability.getRegistryName();
		this.name = fieldName;
		this.superClass = superClass;
		this.player = player;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.ability = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
		this.name = ByteBufUtils.readUTF8String(buf);
		try {
			String superClass = ByteBufUtils.readUTF8String(buf);
			this.id = UUID.fromString(ByteBufUtils.readUTF8String(buf));
			this.player = OrmoyoUtil.proxy.getPlayerByUsername(ByteBufUtils.readUTF8String(buf));
			if(!superClass.isEmpty()) {
				this.superClass = Class.forName(superClass);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.ability.toString());
		ByteBufUtils.writeUTF8String(buf, this.name);
		if(this.superClass != null) {ByteBufUtils.writeUTF8String(buf, this.superClass.getName());}else {ByteBufUtils.writeUTF8String(buf, "");}
		ByteBufUtils.writeUTF8String(buf, this.id.toString());
		ByteBufUtils.writeUTF8String(buf, this.player.getName());
	}

	@Override
	public void onClientReceived(Minecraft client, MessageGetAbilitySyncedValueOnServer message, EntityPlayer player, MessageContext messageContext) {
	}

	@Override
	public void onServerReceived(MinecraftServer server, MessageGetAbilitySyncedValueOnServer message, EntityPlayer player, MessageContext messageContext) {
		Set<Ability> set = OrmoyoUtil.proxy.getUnlockedAbilities(player);
		for(Ability ability : set) {
			if(ability.getRegistryName().equals(message.ability)) {
				if(message.superClass == null) {
					try {
						Field field = ability.getClass().getDeclaredField(message.name);
						field.setAccessible(true);
						OrmoyoUtil.NETWORK_WRAPPER.sendTo(new MessageInvokeGetSyncedValueEventOnClient(player, ability, field.get(ability), message.id), (EntityPlayerMP) player);
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}else {
					if(message.superClass.isAssignableFrom(ability.getClass())) {
						try {
							Field field = message.superClass.getDeclaredField(message.name);
							field.setAccessible(true);
							OrmoyoUtil.NETWORK_WRAPPER.sendTo(new MessageInvokeGetSyncedValueEventOnClient(player, ability, field.get(ability), message.id), (EntityPlayerMP) player);
						} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
