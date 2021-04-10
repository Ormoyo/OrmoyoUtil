package com.ormoyo.ormoyoutil.capability;

import java.util.Set;

import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilityEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = OrmoyoUtil.MODID)
public class CapabilityHandler {
	@CapabilityInject(IAbiltyData.class)
	public static Capability<IAbiltyData> CAPABILITY_PLAYER_DATA = null;
	
	public static void registerCapabilities() {
		CapabilityManager.INSTANCE.register(IAbiltyData.class, new PlayerDataStorage(), AbilityData::new);
	}
	
	@SubscribeEvent
	public static void AttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if(event.getObject() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getObject();
			event.addCapability(new ResourceLocation(OrmoyoUtil.MODID, "ability_data"), new AbilityDataProvider<>(CAPABILITY_PLAYER_DATA, null, player));
		}
	}
	
	public static class PlayerDataStorage implements Capability.IStorage<IAbiltyData> {
		public NBTBase writeNBT(Capability<IAbiltyData> capability, IAbiltyData instance, EnumFacing side) {
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

		public void readNBT(Capability<IAbiltyData> capability, IAbiltyData instance, EnumFacing side, NBTBase nbt) {
			NBTTagCompound tag = (NBTTagCompound)nbt;
			EntityPlayer player = ((AbilityData)instance).getPlayer();
			NBTTagList list = tag.getTagList("abilities", Constants.NBT.TAG_COMPOUND);
			for(NBTBase n : list) {
				NBTTagCompound compound = (NBTTagCompound) n;
				String id = compound.getString("id");
				AbilityEntry entry = Ability.getRegistry().getValue(new ResourceLocation(id));
				if(entry != null) {
					Ability ability = entry.newInstance(player);
					instance.UnlockAbility(ability);
					ability.readFromNBT(compound);
				}
			}
		}
	}
}
