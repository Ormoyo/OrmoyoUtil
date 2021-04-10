package com.ormoyo.ormoyoutil.event;

import java.util.Set;

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
	
	public static class InitAbilityEvent extends AbilityEvent {
		private final Set<Ability> abilitySet;
		public InitAbilityEvent(EntityPlayer player, Set<Ability> abilitySet) {
			super(player);
			this.abilitySet = abilitySet;
		}
		
		public Set<Ability> getAbilityList() {
			return this.abilitySet;
		}
		
		@Cancelable
		public static class Pre extends InitAbilityEvent {
			public Pre(EntityPlayer player, Set<Ability> abilitySet) {
				super(player, abilitySet);
			}
		}
		
		public static class Post extends InitAbilityEvent {
			public Post(EntityPlayer player, Set<Ability> abilitySet) {
				super(player, abilitySet);
			}
		}
	}
}
