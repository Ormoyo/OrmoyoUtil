package com.ormoyo.util.network;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ormoyo.util.OrmoyoUtil;
import com.ormoyo.util.abilities.Ability;
import com.ormoyo.util.abilities.AbilitySyncedValue;
import com.ormoyo.util.abilities.AbilitySyncedValue.AbilitySyncedValueParserEntry;
import com.ormoyo.util.abilities.AbilitySyncedValue.ISyncedValueParser.Reader;
import com.ormoyo.util.abilities.AbilitySyncedValue.ISyncedValueParser.Writer;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpdateAbilitySyncedValueToClient extends AbstractMessage<MessageUpdateAbilitySyncedValueToClient> {
	ResourceLocation ability;
	String name;
	Object value;
	Class<?> superClass;
	EntityPlayer player;
	
	public MessageUpdateAbilitySyncedValueToClient() {
	}
	
	public MessageUpdateAbilitySyncedValueToClient(@Nonnull Ability ability, @Nonnull String fieldName, @Nullable Object value, EntityPlayer player) {
		this.ability = ability.getEntry().getRegistryName();
		this.name = fieldName;
		this.value = value;
		this.player = player;
	}
	
	public MessageUpdateAbilitySyncedValueToClient(@Nonnull Ability ability, @Nullable Class<?> superClass, @Nonnull String fieldName, @Nullable Object value, EntityPlayer player) {
		this.ability = ability.getEntry().getRegistryName();
		this.name = fieldName;
		this.value = value;
		this.superClass = superClass;
		this.player = player;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.ability = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
		this.name = ByteBufUtils.readUTF8String(buf);
		try {
			String value = ByteBufUtils.readUTF8String(buf);
			if(value.isEmpty()) return;
			Class<?> c = Class.forName(value);
			String superClass = ByteBufUtils.readUTF8String(buf);
			this.player = OrmoyoUtil.proxy.getPlayerByUsername(ByteBufUtils.readUTF8String(buf));
			if(!superClass.isEmpty()) {
				this.superClass = Class.forName(superClass);
			}
			if(AbilitySyncedValue.getEntries().containsKey(c)) {
				this.value = AbilitySyncedValue.getEntries().get(c).getParser().read(new Reader(buf), this.player);
				Object obj = OrmoyoUtil.proxy.readToAbility(AbilitySyncedValue.getEntries().get(c).getParser(), new Reader(buf), this.player);
				if(obj != null) {
					this.value = obj;
				}
			}else {
				for(Class<?> clazz : AbilitySyncedValue.getEntries().keySet()) {
					if(clazz.isAssignableFrom(c)) {
						this.value = AbilitySyncedValue.getEntries().get(clazz).getParser().read(new Reader(buf), this.player);
						Object obj = OrmoyoUtil.proxy.readToAbility(AbilitySyncedValue.getEntries().get(clazz).getParser(), new Reader(buf), this.player);
						if(obj != null) {
							this.value = obj;
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.ability.toString());
		ByteBufUtils.writeUTF8String(buf, this.name);
		if(this.value != null) {ByteBufUtils.writeUTF8String(buf, this.value.getClass().getName());}else {ByteBufUtils.writeUTF8String(buf, ""); return;}
		if(this.superClass != null) {ByteBufUtils.writeUTF8String(buf, this.superClass.getName());}else {ByteBufUtils.writeUTF8String(buf, "");}
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
	public void onClientReceived(Minecraft client, MessageUpdateAbilitySyncedValueToClient message, EntityPlayer player, MessageContext messageContext) {
		Set<Ability> set = OrmoyoUtil.proxy.getUnlockedAbilities(player);
		for(Ability ability : set) {
			if(ability.getEntry().getRegistryName().equals(message.ability)) {
				if(message.superClass == null) {
					try {
						Field field = ability.getClass().getDeclaredField(message.name);
						field.setAccessible(true);
						field.set(ability, message.value);
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}else {
					if(superClass.isAssignableFrom(ability.getClass())) {
						try {
							Field field = superClass.getDeclaredField(message.name);
							field.setAccessible(true);
							field.set(ability, message.value);
						} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	@Override
	public void onServerReceived(MinecraftServer server, MessageUpdateAbilitySyncedValueToClient message, EntityPlayer player, MessageContext messageContext) {
	}
}
