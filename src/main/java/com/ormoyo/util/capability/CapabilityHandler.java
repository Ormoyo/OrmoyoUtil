package com.ormoyo.util.capability;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ormoyo.util.OrmoyoUtil;
import com.ormoyo.util.abilities.Ability;
import com.ormoyo.util.resourcelocation.ResourceLocationGsonAdapter;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = OrmoyoUtil.MODID)
public class CapabilityHandler {
	@CapabilityInject(IPlayerData.class)
	public static Capability<IPlayerData> CAPABILITY_PLAYER_DATA = null;
	
	public static void registerCapabilities() {
		CapabilityManager.INSTANCE.register(IPlayerData.class, new PlayerDataStorage(), PlayerData::new);
	}
	
	@SubscribeEvent
	public static void AttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if(event.getObject() instanceof EntityPlayer) {
			event.addCapability(new ResourceLocation(OrmoyoUtil.MODID, "playerdata"), new PlayerDataProvider<>(CAPABILITY_PLAYER_DATA, null));
		}
	}
	
	public static class PlayerDataStorage implements Capability.IStorage<IPlayerData> {
		public NBTBase writeNBT(Capability<IPlayerData> capability, IPlayerData instance, EnumFacing side) {
			NBTTagCompound nbt = new NBTTagCompound();
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().registerTypeAdapter(ResourceLocation.class, new ResourceLocationGsonAdapter()).create();
			Type resourcelocationStringMap = new TypeToken<Set<ResourceLocation>>() {}.getType();
			Set<Ability> unlockedAbilities = instance.getUnlockedAbilities();
			Set<ResourceLocation> set = ObfuscationReflectionHelper.getPrivateValue(PlayerData.class, (PlayerData)instance, "termUnlockedAbilities");
			for(Ability ability : unlockedAbilities) {
				NBTTagCompound compound = new NBTTagCompound();
				ability.writeToNBT(compound);
				nbt.setTag(ability.getEntry().getRegistryName().toString() + ".abilityNBT", compound);
			}
			nbt.setString("unlockedAbilities", gson.toJson(set, resourcelocationStringMap));
			return nbt;
		}

		public void readNBT(Capability<IPlayerData> capability, IPlayerData instance, EnumFacing side, NBTBase nbt) {
			NBTTagCompound tag = (NBTTagCompound)nbt;
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().registerTypeAdapter(ResourceLocation.class, new ResourceLocationGsonAdapter()).create();
			Type resourcelocationStringSet = new TypeToken<Set<ResourceLocation>>() {}.getType();
			Set<ResourceLocation> set = gson.fromJson(tag.getString("unlockedAbilities"), resourcelocationStringSet);
			Map<ResourceLocation, NBTTagCompound> map = Maps.newHashMap();
			for(ResourceLocation location : set) {
				NBTTagCompound compound = tag.getCompoundTag(location + ".abilityNBT");
				map.put(location, compound);
			}
			ObfuscationReflectionHelper.setPrivateValue(PlayerData.class, (PlayerData)instance, map, "abilityToTag");
			ObfuscationReflectionHelper.setPrivateValue(PlayerData.class, (PlayerData)instance, set, "termUnlockedAbilities");
		}
	}
}
