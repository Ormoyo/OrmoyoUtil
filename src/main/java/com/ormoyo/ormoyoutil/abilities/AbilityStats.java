package com.ormoyo.ormoyoutil.abilities;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.Maps;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.OnlyChangableForServer;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.OnlyInvokableForServer;
import com.ormoyo.ormoyoutil.config.ConfigHandler;
import com.ormoyo.ormoyoutil.event.StatsEvent;
import com.ormoyo.ormoyoutil.util.Utils;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class AbilityStats extends Ability {
	@OnlyChangableForServer
	private short level = 1;
	@OnlyChangableForServer
	private int exp;
	@OnlyChangableForServer
	private int requiredExp = 10;
	
	public AbilityStats(EntityPlayer owner) {
		super(owner);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		if(this.exp >= this.requiredExp) {
			StatsEvent.LVIncreaseEvent event = new StatsEvent.LVIncreaseEvent(this.owner, this);
			if(MinecraftForge.EVENT_BUS.post(event)) return;
			this.setEXP(MathHelper.clamp(this.exp - this.requiredExp, 0, Integer.MAX_VALUE));
			this.setLevel((short) (this.level + event.getLVIncrease()));
		}
	}
	
	private static final Map<Class<? extends EntityLivingBase>, Integer> entityToExp = Maps.newHashMap();
	
	@Override
	public void onDeathEvent(LivingDeathEvent event) {
		if(event.getSource().getTrueSource() != null) {
			if(event.getSource().getTrueSource().equals(this.owner)) {
				int exp = 1;
				if(event.getEntityLiving() instanceof IMob) {
					exp = 5;
				}
				if(entityToExp.containsKey(event.getEntityLiving().getClass())) {
					exp = entityToExp.get(event.getEntityLiving().getClass());
				}
				StatsEvent.CalculateEntityExp expEvent = new StatsEvent.CalculateEntityExp(this.owner, this, event.getEntityLiving(), exp);
				if(MinecraftForge.EVENT_BUS.post(expEvent)) exp = 0;
				exp = expEvent.getExp();
				if(!Arrays.asList(ConfigHandler.STATS.entityExpCount).isEmpty()) {
					for(String string : ConfigHandler.STATS.entityExpCount) {
						if(string.startsWith(EntityList.getKey(event.getEntityLiving()).toString())) {
							if(NumberUtils.isParsable(string.split("=")[1])) {
								exp = Integer.parseInt(string.split("=")[1]);
							}
						}
					}
				}
				
				switch(this.owner.world.getDifficulty()) {
				case EASY:
					if(this.owner.getRNG().nextDouble() > 0.01) {
						this.setEXP(this.exp + exp);
					}
					break;
				case NORMAL:
					if(this.owner.getRNG().nextDouble() > 0.1) {
						this.setEXP(this.exp + exp);
					}
					break;
				case HARD:
					if(this.owner.getRNG().nextDouble() > 0.5) {
						this.setEXP(this.exp + exp);
					}
					break;
				case PEACEFUL:
					this.setEXP(this.exp + exp);
					break;
				}
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setShort("LV", this.level);
		compound.setInteger("EXP", this.exp);
		compound.setInteger("requiredEXP", this.requiredExp);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.setLevel(compound.getShort("LV"));
		this.setEXP(compound.getInteger("EXP"));
		this.setRequiredEXP(compound.getInteger("requiredEXP"));
	}
	
	@OnlyInvokableForServer
	public void setLevel(short level) {
		this.level = (short) MathHelper.clamp(level, 1, ConfigHandler.STATS.maxLevel);
		AbilitySyncedValue.setValue(this, "level", (short)MathHelper.clamp(level, 1, ConfigHandler.STATS.maxLevel));
		int multiplayer = 1;
		switch(this.owner.world.getDifficulty()) {
		case EASY:
			multiplayer = 2;
			break;
		case NORMAL:
			multiplayer = 3;
			break;
		case HARD:
			multiplayer = 5;
			break;
		case PEACEFUL:
			multiplayer = 1;
			break;
		}
		this.setRequiredEXP(this.requiredExp + Utils.randomInt(this.level, MathHelper.clamp(multiplayer * this.level, this.level, Integer.MAX_VALUE - 1), this.owner.getRNG()));
	}
	
	public short getLV() {
		return this.level;
	}
	
	@OnlyInvokableForServer
	public void setEXP(int EXP) {
		this.exp = EXP;
		AbilitySyncedValue.setValue(this, "EXP", EXP);
	}
	
	public int getEXP() {
		return this.exp;
	}
	
	@OnlyInvokableForServer
	public void setRequiredEXP(int requiredEXP) {
		this.requiredExp = requiredEXP;
		AbilitySyncedValue.setValue(this, "requiredEXP", requiredEXP);
	}
	
	public int getRequiredEXP() {
		return this.requiredExp;
	}
	
	@Override
	public boolean isVisable() {
		return false;
	}
	
	@Override
	public int getMaxCooldown() {
		return 0;
	}
	
	@Override
	public int getRequiredLevel() {
		return 0;
	}
}
