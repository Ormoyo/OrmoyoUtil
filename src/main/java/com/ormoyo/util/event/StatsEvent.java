package com.ormoyo.util.event;

import com.ormoyo.util.abilities.AbilityStats;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

public class StatsEvent extends PlayerEvent {
	private final AbilityStats stats;
	
	public StatsEvent(EntityPlayer player, AbilityStats stats) {
		super(player);
		this.stats = stats;
	}
	
	@Cancelable
	public static class LVIncreaseEvent extends StatsEvent {
		private short LV = 1;
		public LVIncreaseEvent(EntityPlayer player, AbilityStats stats) {
			super(player, stats);
		}
		
		public void setLVIncrease(short LV) {
			this.LV = LV;
		}
		
		public short getLVIncrease() {
			return this.LV;
		}
	}
	
	/**
	 * Called when calculating the exp of an entity that was killed by a player
	 */
	@Cancelable
	public static class CalculateEntityExp extends StatsEvent {
		private EntityLivingBase entity;
		private int exp;
		
		public CalculateEntityExp(EntityPlayer player, AbilityStats stats, EntityLivingBase entity, int EXP) {
			super(player, stats);
			this.entity = entity;
			this.exp = EXP;
		}
		
		public void setExp(int EXP) {
			this.exp = EXP;
		}
		
		public int getExp() {
			return this.exp;
		}
		
		public EntityLivingBase getEntity() {
			return this.entity;
		}
		
		public EntityLivingBase getEntityLiving() {
			return this.entity;
		}
	}
	
	public AbilityStats getStats() {
		return this.stats;
	}
}
