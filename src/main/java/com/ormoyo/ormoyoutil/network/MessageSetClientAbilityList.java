package com.ormoyo.ormoyoutil.network;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;

import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilityEntry;
import com.ormoyo.ormoyoutil.abilities.AbilityEventListener;
import com.ormoyo.ormoyoutil.abilities.Ability.AbilityEventEntry;
import com.ormoyo.ormoyoutil.abilities.Ability.IAbilityEventInvoker;
import com.ormoyo.ormoyoutil.event.AbilityEvent;
import com.ormoyo.ormoyoutil.proxy.ClientProxy;
import com.ormoyo.ormoyoutil.util.DoubleKeyMap;
import com.ormoyo.ormoyoutil.util.TripleKeyMap;
import com.ormoyo.ormoyoutil.util.TripleKeyMap.TripleKeyEntry;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.IGenericEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSetClientAbilityList extends AbstractMessage<MessageSetClientAbilityList> {
	AbilityEntry abilityEntry;
	boolean readFromNBT;

	public MessageSetClientAbilityList() {
	}
	
	public MessageSetClientAbilityList(Ability ability, boolean readFromNBT) {
		this.abilityEntry = Ability.getAbilityRegistry().getValue(ability.getRegistryName());
		this.readFromNBT = readFromNBT;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.abilityEntry = Ability.getAbilityRegistry().getValue(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
		this.readFromNBT = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, abilityEntry.getRegistryName().toString());
		buf.writeBoolean(this.readFromNBT);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClientReceived(Minecraft client, MessageSetClientAbilityList message, EntityPlayer player, MessageContext messageContext) {
		try {
			Ability ability = message.abilityEntry.newInstance(player);
			if(MinecraftForge.EVENT_BUS.post(new AbilityEvent.OnAbilityUnlockedEvent(ability))) return;
			Set<Ability> set = (Set<Ability>)ObfuscationReflectionHelper.findField(ClientProxy.class, "unlockedAbilities").get(null);
			set.add(ability);
			registerAbilityEvents(ability, message);
			if(!message.readFromNBT) {
				ability.onUnlocked();
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onServerReceived(MinecraftServer server, MessageSetClientAbilityList message, EntityPlayer player, MessageContext messageContext) {
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerAbilityEvents(Ability ability, MessageSetClientAbilityList message) {
		TripleKeyMap<Ability, Class<? extends Event>, IAbilityEventInvoker, AbilityEventListener> eventToListener = null;
		try {
			eventToListener = (TripleKeyMap<Ability, Class<? extends Event>, IAbilityEventInvoker, AbilityEventListener>) ObfuscationReflectionHelper.findField(ClientProxy.class, "eventToListener").get(null);
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		if(eventToListener == null) return;
		for(Method method : ability.getClass().getMethods()) {
			if(Modifier.isStatic(method.getModifiers())) continue;
			if(method.isAnnotationPresent(SubscribeEvent.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1)
                {
                    throw new IllegalArgumentException(
                        "Method " + method + " has @SubscribeEvent annotation, but requires " + parameterTypes.length +
                        " arguments.  Event handler methods must require a single argument."
                    );
                }

                Class<?> eventT = parameterTypes[0];

                if (!Event.class.isAssignableFrom(eventT))
                {
                    throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventT);
                }
                Class<? extends Event> eventType = (Class<? extends Event>) eventT;
				if(message.readFromNBT && eventType == EntityJoinWorldEvent.class) {
					method.setAccessible(true);
					try {
						method.invoke(ability, new EntityJoinWorldEvent(ability.getOwner(), ability.getOwner().getEntityWorld()));
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
				for(AbilityEventEntry entry : Ability.getAbilityEventRegistry()) {
					if(eventType.isAssignableFrom(entry.getEventClass()) || entry.getEventClass().isAssignableFrom(eventType)) {
						try {
							for(Iterator<TripleKeyEntry<Ability, Class<? extends Event>, IAbilityEventInvoker, AbilityEventListener>> iterator = eventToListener.entrySet().iterator(); iterator.hasNext();) {
								TripleKeyEntry<Ability, Class<? extends Event>, IAbilityEventInvoker, AbilityEventListener> e = iterator.next();
								if(e.getKey2().isAssignableFrom(eventType) && e.getValue().getMethod().equals(method)) {
									iterator.remove();
								}
							}
							eventToListener.put(ability, eventType, entry.getEventInvoker(), new AbilityEventListener(ability, method, Loader.instance().activeModContainer(), IGenericEvent.class.isAssignableFrom(eventType)));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
