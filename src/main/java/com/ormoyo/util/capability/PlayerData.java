package com.ormoyo.util.capability;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ormoyo.util.OrmoyoUtil;
import com.ormoyo.util.abilities.Ability;
import com.ormoyo.util.abilities.AbilityStats;
import com.ormoyo.util.event.AbilityEvent;
import com.ormoyo.util.network.MessageSetClientAbilityList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class PlayerData implements IPlayerData {
	private Set<Ability> unlockedAbilities = Sets.newHashSet();
	private Set<ResourceLocation> termUnlockedAbilities = Sets.newHashSet();
	@SuppressWarnings("unused")
	private Map<ResourceLocation, NBTTagCompound> abilityToTag = Maps.newHashMap();
	private EntityPlayer player;

	@Override
	public boolean UnlockAbility(Ability ability) {
		if(this.isAbilityUnlocked(ability)) return false;
		if(MinecraftForge.EVENT_BUS.post(new AbilityEvent.OnAbilityUnlockedEvent(player, ability))) return false;
		
		if(this.unlockedAbilities.add(ability)) {
			this.termUnlockedAbilities.add(ability.getEntry().getRegistryName());
			OrmoyoUtil.NETWORK_WRAPPER.sendTo(new MessageSetClientAbilityList(ability), (EntityPlayerMP)player);
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
			if(ability.getEntry().getRegistryName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Ability getUnlockedAbility(ResourceLocation name) {
		for(Ability ability : unlockedAbilities) {
			if(ability.getEntry().getRegistryName().equals(name)) {
				return ability;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Ability> T getUnlockedAbility(Class<T> clazz) {
		for(Ability ability : unlockedAbilities) {
			if(ability.getEntry().getRegistryName().equals(Ability.getAbilityClassRegistryName(clazz))) {
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
}
