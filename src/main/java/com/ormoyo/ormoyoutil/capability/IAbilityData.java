package com.ormoyo.ormoyoutil.capability;

import java.util.Set;

import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilityStats;

import net.minecraft.util.ResourceLocation;

public interface IAbilityData {
	boolean UnlockAbility(Ability ability);
	boolean isAbilityUnlocked(Ability ability);
	boolean isAbilityUnlocked(ResourceLocation name);
	boolean isAbilityUnlocked(Class<? extends Ability> clazz);
	Ability getUnlockedAbility(ResourceLocation name);
	<T extends Ability>T getUnlockedAbility(Class<T> clazz);
	Set<Ability> getUnlockedAbilities();
	AbilityStats getStats();
}
