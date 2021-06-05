package com.ormoyo.ormoyoutil.capability;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilityEntry;
import com.ormoyo.ormoyoutil.util.DoubleKeyListMap;
import com.ormoyo.ormoyoutil.util.DoubleKeyMap;
import com.ormoyo.ormoyoutil.util.DoubleKeyMap.DoubleKeyEntry;
import com.ormoyo.ormoyoutil.util.Utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@EventBusSubscriber(modid = OrmoyoUtil.MODID)
public class CapabilityHandler {
	@CapabilityInject(IAbilityData.class)
	public static Capability<IAbilityData> CAPABILITY_PLAYER_DATA = null;
	private static final DoubleKeyMap<IAbilityData, NBTTagCompound, Ability> ab = new DoubleKeyListMap<>();
	
	public static void registerCapabilities() {
		CapabilityManager.INSTANCE.register(IAbilityData.class, new AbiltyDataStorage(), AbilityData::new);
	}
	
	@SubscribeEvent
	public static void AttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if(event.getObject() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getObject();
			event.addCapability(new ResourceLocation(OrmoyoUtil.MODID, "ability_data"), new AbilityDataProvider<>(CAPABILITY_PLAYER_DATA, null, player));
		}
	}
	
	@SubscribeEvent
	public static void PlayerData(PlayerLoggedInEvent event) {
		if(event.player instanceof EntityPlayerMP) {
			Utils.performConsumerAfterAmountOfTicks(n -> {
				EntityPlayer player = event.player;
				IAbilityData c = player.getCapability(CAPABILITY_PLAYER_DATA, null);
				if(c instanceof AbilityData) {
					for(DoubleKeyEntry<IAbilityData, NBTTagCompound, Ability> entry : ab.entrySet()) {
						if(entry.getKey1() == c) {
							try {
								Method m = AbilityData.class.getDeclaredMethod("UnlockAbility", Ability.class, boolean.class);
								m.setAccessible(true);
								m.invoke(c, entry.getValue(), true);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
								e.printStackTrace();
							}
							entry.getValue().readFromNBT(entry.getKey2());
						}
					}
					for(AbilityEntry entry : Ability.getAbilityRegistry()) {
						if(c.isAbilityUnlocked(entry.getAbilityClass())) continue;
						Ability ability = entry.newInstance(player);
						if(ability.getRequiredLevel() <= 1) {
							c.UnlockAbility(ability);
							ability.onUnlocked();
						}
					}
				}
			}, null, 2);
		}
	}
	
	public static class AbiltyDataStorage implements Capability.IStorage<IAbilityData> {
		public NBTBase writeNBT(Capability<IAbilityData> capability, IAbilityData instance, EnumFacing side) {
			NBTTagCompound nbt = new NBTTagCompound();
			Set<Ability> unlockedAbilities = instance.getUnlockedAbilities();
			for(Ability ability : unlockedAbilities) {
				NBTTagList list = new NBTTagList();
				NBTTagCompound compound = new NBTTagCompound();
				compound.setString("id", ability.getName());
				ability.writeToNBT(compound);
				list.appendTag(compound);
				nbt.setTag("abilities", list);
			}
			return nbt;
		}

		public void readNBT(Capability<IAbilityData> capability, IAbilityData instance, EnumFacing side, NBTBase nbt) {
			NBTTagCompound tag = (NBTTagCompound)nbt;
			EntityPlayer player = ((AbilityData)instance).getPlayer();
			NBTTagList list = tag.getTagList("abilities", Constants.NBT.TAG_COMPOUND);
			for(NBTBase n : list) {
				NBTTagCompound compound = (NBTTagCompound) n;
				String id = compound.getString("id");
				AbilityEntry entry = Ability.getAbilityRegistry().getValue(new ResourceLocation(id));
				if(entry != null) {
					Ability ability = entry.newInstance(player);
					ab.put(instance, tag, ability);
				}
			}
		}
	}
}
