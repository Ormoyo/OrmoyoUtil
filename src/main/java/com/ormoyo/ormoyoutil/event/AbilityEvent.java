package com.ormoyo.ormoyoutil.event;

import java.util.UUID;

import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * AbilityEvent is fired when an event involving abilities occurs
 *
 */
public class AbilityEvent extends PlayerEvent {
	protected final Ability ability;
	
	public AbilityEvent(Ability ability) {
		super(ability.getOwner());
		this.ability = ability;
	}
	
	/**
	 * OnAbilityUnlockedEvent is fired when a player unlocks an ability
	 */
	@Cancelable
	public static class OnAbilityUnlockedEvent extends AbilityEvent {
		public OnAbilityUnlockedEvent(Ability ability) {
			super(ability);
		}
	}
	
	/**
	 * This event will fire when the method {@link AbilitySyncedValue#getValue} has been invoked and is ready to return it's value
	 */
	public static class AbilityGetSyncedValueEvent extends AbilityEvent {
		private final Object value;
		private final UUID id;
		public AbilityGetSyncedValueEvent(Ability ability, Object value, UUID id) {
			super(ability);
			this.value = value;
			this.id = id;
		}
		
		public Ability getAbility() {
			return ability;
		}
		
		public UUID getEventId() {
			return this.id;
		}
		
		public Object getValue() {
			return this.value;
		}
	}
	
	public Ability getAbility() {
		return this.ability;
	}
}
