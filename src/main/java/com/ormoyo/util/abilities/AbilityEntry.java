package com.ormoyo.util.abilities;

import java.lang.reflect.InvocationTargetException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

public class AbilityEntry extends Impl<AbilityEntry> {
	private Class<? extends Ability> clazz;
	
	public AbilityEntry(Class<? extends Ability> clazz, ResourceLocation name) {
		this.clazz = clazz;
		this.setRegistryName(name);
	}
	
	public AbilityEntry(Class<? extends Ability> clazz, String name) {
		this.clazz = clazz;
		this.setRegistryName(name);
	}
	
	public Class<? extends Ability> getAbilityClass() {
		return this.clazz;
	}
	
	public Ability newInstance(EntityPlayer player) {
		try {
			Ability ability = ObfuscationReflectionHelper.findConstructor(clazz, EntityPlayer.class).newInstance(player);
			ObfuscationReflectionHelper.setPrivateValue(Ability.class, ability, this, "entry");
			return ability;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return this.getRegistryName().toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj instanceof AbilityEntry) {
			AbilityEntry entry = (AbilityEntry) obj;
			if(entry != null) {
				if(this.getRegistryName().equals(entry.getRegistryName())) {
					return true;
				}
			}
		}
		return false;
	}
}
