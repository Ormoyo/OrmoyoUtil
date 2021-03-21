package com.ormoyo.util.abilities;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.Maps;
import com.ormoyo.util.Utils;
import com.ormoyo.util.config.ConfigHandler;
import com.ormoyo.util.event.StatsEvent;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class AbilityStats extends Ability {
	private short LV = 1;
	private int EXP;
	private int requiredEXP = 10;
	
	public AbilityStats(EntityPlayer owner) {
		super(owner);
	}
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		if(this.EXP >= this.requiredEXP) {
			StatsEvent.LVIncreaseEvent event = new StatsEvent.LVIncreaseEvent(this.owner, this);
			if(MinecraftForge.EVENT_BUS.post(event)) return;
			this.setEXP(MathHelper.clamp(this.EXP - this.requiredEXP, 0, Integer.MAX_VALUE));
			this.setLevel((short) (this.LV + event.getLVIncrease()));
		}
	}
	
	private static Map<Class<? extends EntityLivingBase>, Integer> entityToExp = Maps.newHashMap();
	
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
						this.setEXP(this.EXP + exp);
					}
					break;
				case NORMAL:
					if(this.owner.getRNG().nextDouble() > 0.1) {
						this.setEXP(this.EXP + exp);
					}
					break;
				case HARD:
					if(this.owner.getRNG().nextDouble() > 0.5) {
						this.setEXP(this.EXP + exp);
					}
					break;
				case PEACEFUL:
					this.setEXP(this.EXP + exp);
					break;
				}
			}
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setShort("LV", this.LV);
		compound.setInteger("EXP", this.EXP);
		compound.setInteger("requiredEXP", this.requiredEXP);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.setLevel(compound.getShort("LV"));
		this.setEXP(compound.getInteger("EXP"));
		this.setRequiredEXP(compound.getInteger("requiredEXP"));
	}
	
	public void setLevel(short LV) {
		if(this.owner.world.isRemote) return;
		this.LV = (short) MathHelper.clamp(LV, 1, ConfigHandler.STATS.maxLevel);
		AbilitySyncedValue.setValue(this, "LV", (short)MathHelper.clamp(LV, 1, ConfigHandler.STATS.maxLevel));
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
		this.setRequiredEXP(this.requiredEXP + Utils.randomInt(MathHelper.clamp(multiplayer * this.LV, this.LV, Integer.MAX_VALUE - 1), Integer.MAX_VALUE, this.owner.getRNG()));
	}
	
	public short getLV() {
		return this.LV;
	}
	
	public void setEXP(int EXP) {
		if(this.owner.world.isRemote) return;
		this.EXP = EXP;
		AbilitySyncedValue.setValue(this, "EXP", EXP);
	}
	
	public int getEXP() {
		return this.EXP;
	}
	
	public void setRequiredEXP(int requiredEXP) {
		if(this.owner.world.isRemote) return;
		this.requiredEXP = requiredEXP;
		AbilitySyncedValue.setValue(this, "requiredEXP", requiredEXP);
	}
	
	public int getRequiredEXP() {
		return this.requiredEXP;
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
