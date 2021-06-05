package com.ormoyo.ormoyoutil.network;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilityEntry;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.AbilitySyncedValueParserEntry;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser.Reader;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser.Writer;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageInvokeMethodOnClient extends AbstractMessage<MessageInvokeMethodOnClient> {
	ResourceLocation ability;
	String methodName;
	EntityPlayer player;
	Object[] args;
	
	public MessageInvokeMethodOnClient() {
	}
	
	public MessageInvokeMethodOnClient(Ability ability, String methodName, EntityPlayer player, Object...args) {
		this.ability = ability.getRegistryName();
		this.methodName = methodName;
		this.player = player;
		this.args = args;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.ability = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
		this.methodName = ByteBufUtils.readUTF8String(buf);
		int argsSize = buf.readInt();
		this.player = OrmoyoUtil.proxy.getPlayerByUsername(ByteBufUtils.readUTF8String(buf));
		this.args = new Object[argsSize];
		for(int i = 0; i < argsSize; i++) {
			try {
				String v = ByteBufUtils.readUTF8String(buf);
				if(v.isEmpty()) continue;
				Class<?> c = Class.forName(v);
				if(AbilitySyncedValue.getEntries().containsKey(c)) {
					this.args[i] = AbilitySyncedValue.getEntries().get(c).getParser().read(new Reader(buf), this.player);
					Object obj = OrmoyoUtil.proxy.readToAbility(AbilitySyncedValue.getEntries().get(c).getParser(), new Reader(buf), this.player);
					if(obj != null) {
						this.args[i] = obj;
					}
				}else {
					for(Class<?> clazz : AbilitySyncedValue.getEntries().keySet()) {
						if(clazz.isAssignableFrom(c)) {
							this.args[i] = AbilitySyncedValue.getEntries().get(clazz).getParser().read(new Reader(buf), this.player);
							Object obj = OrmoyoUtil.proxy.readToAbility(AbilitySyncedValue.getEntries().get(clazz).getParser(), new Reader(buf), this.player);
							if(obj != null) {
								this.args[i] = obj;
							}
						}
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, this.ability.toString());
		ByteBufUtils.writeUTF8String(buf, this.methodName);
		buf.writeInt(this.args.length);
		ByteBufUtils.writeUTF8String(buf, this.player.getName());
		for(Object value : this.args) {
			if(value != null) {ByteBufUtils.writeUTF8String(buf, value.getClass().getName());}else {ByteBufUtils.writeUTF8String(buf, ""); continue;}
			if(AbilitySyncedValue.getEntries().containsKey(value.getClass())) {
				AbilitySyncedValueParserEntry s = AbilitySyncedValue.getEntries().get(value.getClass());
				try {
					Method m = s.getParser().getClass().getDeclaredMethod("write", Writer.class, EntityPlayer.class, value.getClass());
					m.setAccessible(true);
					m.invoke(s.getParser(), new Writer(buf), this.player, value);
					OrmoyoUtil.proxy.writeToAbility(s.getParser(), new Writer(buf), value, value.getClass(), this.player);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
				}
			}else {
				for(Class<?> clazz : AbilitySyncedValue.getEntries().keySet()) {
					if(clazz.isAssignableFrom(value.getClass())) {
						AbilitySyncedValueParserEntry s = AbilitySyncedValue.getEntries().get(clazz);
						try {
							Method m = s.getParser().getClass().getDeclaredMethod("write", Writer.class, EntityPlayer.class, clazz);
							m.setAccessible(true);
							m.invoke(s.getParser(), new Writer(buf), this.player, value);
							OrmoyoUtil.proxy.writeToAbility(s.getParser(), new Writer(buf), value, clazz, this.player);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
								| SecurityException e) {
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
						}
					}
				}
			}
		}
	}

	@Override
	public void onClientReceived(Minecraft client, MessageInvokeMethodOnClient message, EntityPlayer player, MessageContext messageContext) {
		if(message.ability != null) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(player)) {
				if(ability.getRegistryName().equals(message.ability)) {
					try {
						Class<?>[] array = new Class<?>[message.args.length];
						for(int i = 0; i < message.args.length; i++) {
							array[i] = message.args[i].getClass();
						}
						Method method = ability.getClass().getDeclaredMethod(message.methodName, array);
						if(method.isAnnotationPresent(AbilitySyncedValue.OnlyInvokableForClient.class) || !method.isAnnotationPresent(AbilitySyncedValue.InvokableMethod.class) && !method.isAnnotationPresent(AbilitySyncedValue.OnlyInvokableForServer.class)) return;
						method.setAccessible(true);
						method.invoke(ability, message.args);
					} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void onServerReceived(MinecraftServer server, MessageInvokeMethodOnClient message, EntityPlayer player, MessageContext messageContext) {
	}
}
