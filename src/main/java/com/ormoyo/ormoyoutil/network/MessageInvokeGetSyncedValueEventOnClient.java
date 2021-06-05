package com.ormoyo.ormoyoutil.network;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.AbilitySyncedValueParserEntry;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser.Reader;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser.Writer;
import com.ormoyo.ormoyoutil.event.AbilityEvent;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageInvokeGetSyncedValueEventOnClient extends AbstractMessage<MessageInvokeGetSyncedValueEventOnClient> {
	EntityPlayer player;
	ResourceLocation ability;
	Object value;
	UUID id;
	public MessageInvokeGetSyncedValueEventOnClient() {
	}
	
	public MessageInvokeGetSyncedValueEventOnClient(EntityPlayer player, Ability ability, Object value, UUID id) {
		this.player = player;
		this.ability = ability.getRegistryName();
		this.value = value;
		this.id = id;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.id = UUID.fromString(ByteBufUtils.readUTF8String(buf));
		this.ability = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
		try {
			String value = ByteBufUtils.readUTF8String(buf);
			if(value.isEmpty()) return;
			Class<?> c = Class.forName(value);
			this.player = OrmoyoUtil.proxy.getPlayerByUsername(ByteBufUtils.readUTF8String(buf));
			if(AbilitySyncedValue.getEntries().containsKey(c)) {
				this.value = AbilitySyncedValue.getEntries().get(c).getParser().read(new Reader(buf), this.player);
				Object obj = OrmoyoUtil.proxy.readToAbility(AbilitySyncedValue.getEntries().get(c).getParser(), new Reader(buf), this.player);
				if(obj != null) {
					this.value = obj;
				}
			}else {
				boolean isParserExists = false;
				for(Class<?> clazz : AbilitySyncedValue.getEntries().keySet()) {
					if(clazz.isAssignableFrom(c)) {
						isParserExists = true;
						this.value = AbilitySyncedValue.getEntries().get(clazz).getParser().read(new Reader(buf), this.player);
						Object obj = OrmoyoUtil.proxy.readToAbility(AbilitySyncedValue.getEntries().get(clazz).getParser(), new Reader(buf), this.player);
						if(obj != null) {
							this.value = obj;
						}
						break;
					}
				}
				if(!isParserExists) {
					OrmoyoUtil.LOGGER.error("Cannot find parser for object " + this.value);
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.id.toString());
		ByteBufUtils.writeUTF8String(buf, this.ability.toString());
		if(this.value != null) {ByteBufUtils.writeUTF8String(buf, this.value.getClass().getName());}else {ByteBufUtils.writeUTF8String(buf, ""); return;}
		ByteBufUtils.writeUTF8String(buf, this.player.getName());
		if(AbilitySyncedValue.getEntries().containsKey(this.value.getClass())) {
			AbilitySyncedValueParserEntry s = AbilitySyncedValue.getEntries().get(this.value.getClass());
			try {
				Method m = s.getParser().getClass().getDeclaredMethod("write", Writer.class, EntityPlayer.class, this.value.getClass());
				m.setAccessible(true);
				m.invoke(s.getParser(), new Writer(buf), this.player, this.value);
				OrmoyoUtil.proxy.writeToAbility(s.getParser(), new Writer(buf), this.value, this.value.getClass(), this.player);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
			}
		}else {
			for(Class<?> clazz : AbilitySyncedValue.getEntries().keySet()) {
				if(clazz.isAssignableFrom(this.value.getClass())) {
					AbilitySyncedValueParserEntry s = AbilitySyncedValue.getEntries().get(clazz);
					try {
						Method m = s.getParser().getClass().getDeclaredMethod("write", Writer.class, EntityPlayer.class, clazz);
						m.setAccessible(true);
						m.invoke(s.getParser(), new Writer(buf), this.player, this.value);
						OrmoyoUtil.proxy.writeToAbility(s.getParser(), new Writer(buf), this.value, clazz, this.player);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| SecurityException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
					}
				}
			}
		}
	}

	@Override
	public void onClientReceived(Minecraft client, MessageInvokeGetSyncedValueEventOnClient message, EntityPlayer player, MessageContext messageContext) {
	}

	@Override
	public void onServerReceived(MinecraftServer server, MessageInvokeGetSyncedValueEventOnClient message, EntityPlayer player, MessageContext messageContext) {
		Set<Ability> set = OrmoyoUtil.proxy.getUnlockedAbilities(player);
		for(Ability ability : set) {
			if(ability.getRegistryName().equals(message.ability)) {
				MinecraftForge.EVENT_BUS.post(new AbilityEvent.AbilityGetSyncedValueEvent(ability, this.value, this.id));
			}
		}
	}
}
