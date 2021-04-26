package com.ormoyo.ormoyoutil.event;

import com.ormoyo.ormoyoutil.abilities.Ability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * AbilityEvent is fired when an event involving abilities occurs
 *
 */
public class AbilityEvent extends PlayerEvent {
	
	public AbilityEvent(EntityPlayer player) {
		super(player);
	}
	
	/**
	 * OnAbilityUnlockedEvent is fired when a player unlocks an ability
	 */
	@Cancelable
	public static class OnAbilityUnlockedEvent extends AbilityEvent {
		private final Ability ability;
		public OnAbilityUnlockedEvent(EntityPlayer player, Ability ability) {
			super(player);
			this.ability = ability;
		}
		
		public Ability getAbility() {
			return ability;
		}
	}
}
