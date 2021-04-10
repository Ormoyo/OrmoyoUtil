package com.ormoyo.ormoyoutil.capability;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * A simple implementation of {@link ICapabilitySerializable} that supports a single {@link Capability} handler instance.
 * <p>
 * Uses the {@link Capability}'s {@link IStorage} to serialise/deserialise NBT.
 *
 * @author Choonster
 */
public class AbilityDataProvider<HANDLER> implements ICapabilitySerializable<NBTBase> {
	/**
	 * The {@link Capability} instance to provide the handler for.
	 */
	private final Capability<HANDLER> capability;

	/**
	 * The {@link EnumFacing} to provide the handler for.
	 */
	private final EnumFacing facing;

	/**
	 * The handler instance to provide.
	 */
	private final HANDLER instance;

	/**
	 * Create a provider for the default handler instance.
	 *
	 * @param capability The Capability instance to provide the handler for
	 * @param facing     The EnumFacing to provide the handler for
	 */
	@SuppressWarnings("unchecked")
	public AbilityDataProvider(Capability<HANDLER> capability, @Nullable EnumFacing facing, EntityPlayer player) {
		this(capability, facing, (HANDLER) new AbilityData(player), player);
	}

	/**
	 * Create a provider for the specified handler instance.
	 *
	 * @param capability The Capability instance to provide the handler for
	 * @param facing     The EnumFacing to provide the handler for
	 * @param instance   The handler instance to provide
	 */
	public AbilityDataProvider(Capability<HANDLER> capability, @Nullable EnumFacing facing, HANDLER instance, EntityPlayer player) {
		this.capability = capability;
		this.instance = instance;
		this.facing = facing;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(capability == CapabilityHandler.CAPABILITY_PLAYER_DATA) {
			return true;
		}
		return false;
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityHandler.CAPABILITY_PLAYER_DATA) {
			return CapabilityHandler.CAPABILITY_PLAYER_DATA.cast((IAbiltyData) getInstance());
		}

		return null;
	}
	
	

	@Override
	public NBTBase serializeNBT() {
		return getCapability().getStorage().writeNBT(capability, getInstance(), getFacing());
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		getCapability().getStorage().readNBT(capability, getInstance(), getFacing(), nbt);
	}

	/**
	 * Get the {@link Capability} instance to provide the handler for.
	 *
	 * @return The Capability instance
	 */
	public final Capability<HANDLER> getCapability() {
		return capability;
	}

	/**
	 * Get the {@link EnumFacing} to provide the handler for.
	 *
	 * @return The EnumFacing to provide the handler for
	 */
	@Nullable
	public EnumFacing getFacing() {
		return facing;
	}

	/**
	 * Get the handler instance.
	 *
	 * @return The handler instance
	 */
	public final HANDLER getInstance() {
		return instance;
	}
}
