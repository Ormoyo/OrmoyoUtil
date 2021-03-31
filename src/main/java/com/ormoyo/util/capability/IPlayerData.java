package com.ormoyo.util.capability;

import java.util.Set;

import com.ormoyo.util.abilities.Ability;
import com.ormoyo.util.abilities.AbilityStats;

import net.minecraft.util.ResourceLocation;

public interface IPlayerData {
	boolean UnlockAbility(Ability ability);
	boolean isAbilityUnlocked(Ability ability);
	boolean isAbilityUnlocked(ResourceLocation name);
	Ability getUnlockedAbility(ResourceLocation name);
	<T extends Ability>T getUnlockedAbility(Class<T> clazz);
	Set<Ability> getUnlockedAbilities();
	AbilityStats getStats();
}
