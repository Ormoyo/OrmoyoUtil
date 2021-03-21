package com.ormoyo.util.abilities;

import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.lwjgl.input.Keyboard;

import com.ormoyo.util.OrmoyoUtil;
import com.ormoyo.util.event.AbilityEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = OrmoyoUtil.MODID)
public abstract class Ability {
	public Ability(@Nonnull EntityPlayer owner) {
		this.owner = owner;
	}
	
	protected boolean hasBeenPressed;
	protected int cooldown;
	protected boolean startCooldown;
	
	protected EntityPlayer owner;
	protected AbilityEntry entry;
	protected boolean isEnabled = true;
	
	public void onKeyPress() {}
	public void onKeyRelease() {}
	
	public abstract int getRequiredLevel();
	public abstract int getMaxCooldown();
	
	/**
	 * Called every tick on client and server side
	 */
	public void onUpdate() {}
	/**
	 * Called when a player unlocks this ability
	 */
	public void onUnlocked(AbilityEvent.OnAbilityUnlockedEvent event) {}
	/**
	 * Called when a {@link LivingAttackEvent} related to the ability owner occurred i.e. the owner attacked or has been attacked by an entity
	 */
	public void onAttackEvent(LivingAttackEvent event) {}
	/**
	 * Called when a {@link LivingKnockBackEvent} related to the ability owner occurred i.e. the owner got knocked back or has knocked back an entity
	 */
	public void onKnockBackEvent(LivingKnockBackEvent event) {}
	/**
	 * Called when the ability owner got shot by a projectile
	 */
	public void onProjectileImpact(ProjectileImpactEvent event) {}
	/**
	 * Called when a {@link LivingDeathEvent} involving to the ability owner occurred i.e. the owner died or has killed an entity
	 */
	public void onDeathEvent(LivingDeathEvent event) {}
	/**
	 * Called every frame
	 */
	@SideOnly(Side.CLIENT)
	public void onRenderUpdate(float partialTicks) {}
	/**
	 * Called when a {@link RenderGameOverlayEvent.Pre} event involving the ability owner overlay occured
	 */
	@SideOnly(Side.CLIENT)
	public void onPreRenderOverlayEvent(RenderGameOverlayEvent.Pre event) {}
	/**
	 * Called when a {@link RenderGameOverlayEvent.Post} event involving the ability owner overlay occured
	 */
	@SideOnly(Side.CLIENT)
	public void onPostRenderOverlayEvent(RenderGameOverlayEvent.Post event) {}
	/**
	 * Called when a {@link ActionPerformedEvent} involving the ability owner occurred i.e. the owner has clicked a button in a gui
	 */
	@SideOnly(Side.CLIENT)
	public void onButtonPressInGui(GuiScreenEvent.ActionPerformedEvent event) {}
	/**
	 * Called when the ability owner inputs are updated
	 */
	@SideOnly(Side.CLIENT)
	public void onInputUpdate(InputUpdateEvent event) {}
	/**
	 * Called when a {@link CameraSetup} involving the ability owner camera occurred
	 */
	@SideOnly(Side.CLIENT)
	public void onCameraUpdate(CameraSetup event) {}
	/**
	 * Called when a {@link RenderSpecificHandEvent} involving the ability owner hands occurred
	 */
	@SideOnly(Side.CLIENT)
	public void onHandRender(RenderSpecificHandEvent event) {}
	/**
	 * Called on the ability owner client when disconnected from server
	 */
	@SideOnly(Side.CLIENT)
	public void onClientDisconnectedFromServer(ClientDisconnectionFromServerEvent event) {}
	/**
	 * Called on the server when the ability owner logges off
	 */
	public void onLoggedOut(PlayerLoggedOutEvent event) {}
	
	public void writeToNBT(NBTTagCompound compound) {}
	public void readFromNBT(NBTTagCompound compound) {}
	/**
	 * @return The keycode of the ability to be activated with(Can be null if the keycode not gonna be used for activision)
	 */
	@SideOnly(Side.CLIENT)
	public int getKeybindCode() {return -1;}
	/**
	 * @return The keybind of the ability to be activated with(Should use {@link #Ability.getKeybindCode()} instand)
	 */
	@SideOnly(Side.CLIENT)
	public KeyBinding getKeybind() {
		ResourceLocation location = this.getEntry().getRegistryName();
		if(this.getKeybindCode() >= 0) {
			for(KeyBinding keybind : Minecraft.getMinecraft().gameSettings.keyBindings) {
				if(keybind.getKeyDescription().equals("key." + location.getResourceDomain() + "." + location.getResourcePath())) {
					return keybind;
				}
			}
		}
		return null;
	}
	
	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
		if(isEnabled == false) {
			this.onAbilityDisabled();
			AbilitySyncedValue.invokeMethod(this, "setIsEnabled", isEnabled);
		}
	}
	
	/**
	 * If the ability is not enabled all it's methods are not gonna get called
	 */
	public boolean isEnabled() {
		return this.isEnabled;
	}
	
	/**
	 * Called when {@link #isEnabled} is set to false
	 */
	public void onAbilityDisabled() {}
	
	protected void setStartCooldown(boolean startCooldown) {
		this.startCooldown = startCooldown;
		AbilitySyncedValue.setValue(this, Ability.class, "startCooldown", startCooldown);
	}
	
	protected boolean getStartCooldown() {
		return this.startCooldown;
	}
	
	public EntityPlayer getOwner() {
		return this.owner;
	}
	
	public String getName() {
		return this.entry.getRegistryName().toString();
	}
	
	public ITextComponent getTranslatedName() {
		return new TextComponentTranslation("ability." + this.getEntry().getRegistryName().getResourceDomain() + "." + this.getEntry().getRegistryName().getResourcePath() + ".name");
	}
	
	public AbilityEntry getEntry() {
		return this.entry;
	}
	
	private static IForgeRegistry<AbilityEntry> ABILITY_REGISTRY;
	public static IForgeRegistry<AbilityEntry> getRegistry() {
		return ABILITY_REGISTRY;
	}
	
	@SubscribeEvent
	public static void onNewRegistry(RegistryEvent.NewRegistry event) {
		ABILITY_REGISTRY = new RegistryBuilder<AbilityEntry>().setName(new ResourceLocation(OrmoyoUtil.MODID, "ability")).setType(AbilityEntry.class).setIDRange(0, 2048).create();
	}
	
	@SubscribeEvent
	public static void onRegisterAbilities(RegistryEvent.Register<AbilityEntry> event) {
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj instanceof Ability) {
			Ability ability = (Ability)obj;
			if(this.getEntry() != null && ability.getEntry() != null) {
				if(this.getEntry().getRegistryName().equals(ability.getEntry().getRegistryName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static ResourceLocation getAbilityClassRegistryName(Class<? extends Ability> clazz) {
		for(Entry<ResourceLocation, AbilityEntry> entry : Ability.getRegistry().getEntries()) {
			if(entry.getValue().getAbilityClass() == clazz) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public boolean isVisable() {
		if(this.getRequiredLevel() <= 1) {
			return false;
		}
		return true;
	}
}
