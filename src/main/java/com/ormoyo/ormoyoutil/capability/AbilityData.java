package com.ormoyo.ormoyoutil.capability;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilityStats;
import com.ormoyo.ormoyoutil.abilities.Ability.AbilityEventEntry;
import com.ormoyo.ormoyoutil.abilities.Ability.IAbilityEventInvoker;
import com.ormoyo.ormoyoutil.abilities.AbilityEventListener;
import com.ormoyo.ormoyoutil.event.AbilityEvent;
import com.ormoyo.ormoyoutil.network.MessageSetClientAbilityList;
import com.ormoyo.ormoyoutil.util.TripleKeyListMap;
import com.ormoyo.ormoyoutil.util.TripleKeyMap;
import com.ormoyo.ormoyoutil.util.TripleKeyMap.TripleKeyEntry;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.IGenericEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class AbilityData implements IAbilityData {
	private Set<Ability> unlockedAbilities = Sets.newHashSet();
	@SuppressWarnings("rawtypes")
	private static TripleKeyMap<Ability, Class<? extends Event>, IAbilityEventInvoker, AbilityEventListener> eventToListener = new TripleKeyListMap<>();
	private EntityPlayer player;
	
	public AbilityData() {
	}
	
	public AbilityData(EntityPlayer player) {
		this.player = player;
	}

	@Override
	public boolean UnlockAbility(Ability ability) {
		return this.UnlockAbility(ability, false);
	}
	
	private boolean UnlockAbility(Ability ability, boolean readFromNBT) {
		if(this.isAbilityUnlocked(ability)) return false;
		if(!readFromNBT) {
			if(MinecraftForge.EVENT_BUS.post(new AbilityEvent.OnAbilityUnlockedEvent(ability))) return false;
		}
		if(this.unlockedAbilities.add(ability)) {
			OrmoyoUtil.NETWORK_WRAPPER.sendTo(new MessageSetClientAbilityList(ability, readFromNBT), (EntityPlayerMP)player);
			registerAbilityEvents(ability, readFromNBT);
			return true;
		}
		return false;
	}

	@Override
	public boolean isAbilityUnlocked(Ability ability) {
		for(Ability ab : unlockedAbilities) {
			if(ab.equals(ability)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isAbilityUnlocked(ResourceLocation name) {
		for(Ability ability : unlockedAbilities) {
			if(ability.getRegistryName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isAbilityUnlocked(Class<? extends Ability> clazz) {
		for(Ability ability : unlockedAbilities) {
			if(ability.getClass() == clazz) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Ability getUnlockedAbility(ResourceLocation name) {
		for(Ability ability : unlockedAbilities) {
			if(ability.getRegistryName().equals(name)) {
				return ability;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Ability> T getUnlockedAbility(Class<T> clazz) {
		for(Ability ability : unlockedAbilities) {
			if(ability.getRegistryName().equals(Ability.getAbilityClassRegistryName(clazz))) {
				return (T) ability;
			}
		}
		return null;
	}
	
	public Set<Ability> getUnlockedAbilities(){
		return Collections.unmodifiableSet(unlockedAbilities);
	}
	
	@Override
	public AbilityStats getStats() {
		return (AbilityStats) this.getUnlockedAbility(Ability.getAbilityClassRegistryName(AbilityStats.class));
	}
	
	public EntityPlayer getPlayer() {
		return this.player;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerAbilityEvents(Ability ability, boolean readFromNBT) {
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
				if(readFromNBT && eventType == PlayerLoggedInEvent.class) {
					method.setAccessible(true);
					try {
						method.invoke(ability, new PlayerLoggedInEvent(player));
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
				for(AbilityEventEntry entry : Ability.getAbilityEventRegistry().getValuesCollection()) {
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
	
	@EventBusSubscriber(modid = OrmoyoUtil.MODID)
	private static class EventHandler {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@SubscribeEvent
		public static void onEvent(Event event) {
			for(TripleKeyEntry<Ability, Class<? extends Event>, IAbilityEventInvoker, AbilityEventListener> entry : eventToListener.entrySet()) {
				if(!entry.getKey1().isEnabled()) continue;
				if(entry.getKey2() == event.getClass()) {
					if(entry.getKey3().invoke(entry.getKey1(), event)) {
						entry.getValue().invoke(event);
					}
				}
			}
		}
	}
}
