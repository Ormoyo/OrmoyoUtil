package com.ormoyo.ormoyoutil.abilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import com.google.common.collect.Sets;
import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.capability.CapabilityHandler;
import com.ormoyo.ormoyoutil.capability.IAbiltyData;
import com.ormoyo.ormoyoutil.client.render.RenderHelper;
import com.ormoyo.ormoyoutil.capability.AbilityData;
import com.ormoyo.ormoyoutil.event.AbilityEvent;
import com.ormoyo.ormoyoutil.event.StatsEvent;
import com.ormoyo.ormoyoutil.proxy.ClientProxy;
import com.ormoyo.ormoyoutil.util.icon.IIcon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public abstract class Ability {
	public Ability(@Nonnull EntityPlayer owner) {
		this.owner = owner;
	}
	
	protected boolean hasBeenPressed;
	protected int cooldown;
	protected boolean startCooldown;
	
	protected final EntityPlayer owner;
	private AbilityEntry entry;
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
	public void onAttackEntity(AttackEntityEvent event) {}
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
	
	public void onEntityInteractSpecificEvent(PlayerInteractEvent.EntityInteractSpecific event) {}
	public void onEntityInteractEvent(PlayerInteractEvent.EntityInteract event) {}
	public void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {}
	public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {}
	public void onEmptyLeftClick(PlayerInteractEvent.LeftClickEmpty event) {}
	public void onEmptyRightClick(PlayerInteractEvent.RightClickEmpty event) {}
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
	 * Called when a {@link RenderPlayerEvent.Pre} involving the ability owner occured
	 * @apiNote Called even if first person
	 */
	@SideOnly(Side.CLIENT)
	public void onPrePlayerRender(RenderPlayerEvent.Pre event) {}
	/**
	 * Called when a {@link RenderPlayerEvent.Post} involving the ability owner occured
	 * @apiNote Called even if first person
	 */
	@SideOnly(Side.CLIENT)
	public void onPostPlayerRender(RenderPlayerEvent.Post event) {}
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
	 * Called when a mouse input from the ability owner occurred
	 */
	@SideOnly(Side.CLIENT)
	public void onMouseEvent(MouseEvent event) {}
	/**
	 * Basically {@linkplain #onMouseEvent(MouseEvent)} but called after it
	 */
	@SideOnly(Side.CLIENT)
	public void onMouseInput(MouseInputEvent event) {}
	/**
	 * Called when a key input from the ability owner occurred
	 */
	@SideOnly(Side.CLIENT)
	public void onKeyInput(KeyInputEvent event) {}
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
		return this.getRequiredLevel() > 1;
	}
	
	@EventBusSubscriber(modid = OrmoyoUtil.MODID)
	private static class EventHandler {
		//COMMON SIDE
		@SubscribeEvent
		public static void onNewRegistry(RegistryEvent.NewRegistry event) {
			ABILITY_REGISTRY = new RegistryBuilder<AbilityEntry>().setName(new ResourceLocation(OrmoyoUtil.MODID, "ability")).setType(AbilityEntry.class).setIDRange(0, 2048).create();
		}
		
		@SubscribeEvent
		public static void onLivingDeath(LivingDeathEvent event) {
			if(event.getSource().getTrueSource() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();
				IAbiltyData capability = player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
				for(Ability ability : capability.getUnlockedAbilities()) {
					if(ability.isEnabled()) {
						ability.onDeathEvent(event);
					}
				}
			}else if(event.getEntityLiving() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer)event.getEntityLiving();
				IAbiltyData capability = player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
				for(Ability ability : capability.getUnlockedAbilities()) {
					if(ability.isEnabled()) {
						ability.onDeathEvent(event);
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void onEntityAttack(LivingAttackEvent event) {
			if(event.getEntityLiving() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer)event.getEntityLiving();
				Set<Ability> set = OrmoyoUtil.proxy.getUnlockedAbilities(player);
				for(Ability ability : set) {
					if(ability.isEnabled()) {
						ability.onAttackEvent(event);
					}
				}
				return;
			}
			if(event.getSource().getTrueSource() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();
				Set<Ability> set = OrmoyoUtil.proxy.getUnlockedAbilities(player);
				for(Ability ability : set) {
					if(ability.isEnabled()) {
						ability.onAttackEvent(event);
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void onEntityKnockback(LivingKnockBackEvent event) {
			if(event.getEntityLiving() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer)event.getEntityLiving();
				Set<Ability> set = OrmoyoUtil.proxy.getUnlockedAbilities(player);
				for(Ability ability : set) {
					if(ability.isEnabled()) {
						ability.onKnockBackEvent(event);
					}
				}
			}else if(event.getAttacker() instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer)event.getAttacker();
				Set<Ability> set = OrmoyoUtil.proxy.getUnlockedAbilities(player);
				for(Ability ability : set) {
					if(ability.isEnabled()) {
						ability.onKnockBackEvent(event);
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void onAttackEntity(AttackEntityEvent event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(event.getEntityPlayer())) {
				if(ability.isEnabled()) {
					ability.onAttackEntity(event);
				}
			}
		}
		
		@SubscribeEvent
		public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(event.getEntityPlayer())) {
				if(ability.isEnabled()) {
					ability.onEntityInteractSpecificEvent(event);
				}
			}
		}
		
		@SubscribeEvent
		public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteract event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(event.getEntityPlayer())) {
				if(ability.isEnabled()) {
					ability.onEntityInteractEvent(event);
				}
			}
		}
		
		@SubscribeEvent
		public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(event.getEntityPlayer())) {
				if(ability.isEnabled()) {
					ability.onBlockLeftClick(event);
				}
			}
		}
		
		@SubscribeEvent
		public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(event.getEntityPlayer())) {
				if(ability.isEnabled()) {
					ability.onBlockRightClick(event);
				}
			}
		}
		
		@SubscribeEvent
		public static void onEmptyLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(event.getEntityPlayer())) {
				if(ability.isEnabled()) {
					ability.onEmptyLeftClick(event);
				}
			}
		}
		
		@SubscribeEvent
		public static void onEmptyRightClick(PlayerInteractEvent.RightClickEmpty event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(event.getEntityPlayer())) {
				if(ability.isEnabled()) {
					ability.onEmptyRightClick(event);
				}
			}
		}
		
		@SubscribeEvent
		public static void onPlayerTickEvent(PlayerTickEvent event) {
			if(event.side.equals(Side.SERVER) && event.phase == Phase.END) {
				for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(event.player)) {
					if(ability.isEnabled()) {
						ability.onUpdate();
					}
				}
			}
		}
		
		private static Set<Ability> unlockedAbilities = Sets.newHashSet();
		
		@SubscribeEvent
		public static void onProjectileImpact(ProjectileImpactEvent event) {
			if(event.getRayTraceResult().entityHit instanceof EntityPlayer) {
				for(Ability ability : unlockedAbilities) {
					if(ability.isEnabled()) {
						ability.onProjectileImpact(event);
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void onAbilityUnlock(AbilityEvent.OnAbilityUnlockedEvent event) {
			event.getAbility().onUnlocked(event);
			unlockedAbilities.add(event.getAbility());
		}
		
		@SubscribeEvent
		public static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(event.player)) {
				if(ability.isEnabled()) {
					ability.onLoggedOut(event);
				}
			}
		}
		
		//CLIENT SIDE
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onClientTick(ClientTickEvent event) {
			if(event.phase == Phase.END) {
				for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
					if(ability.isEnabled()) {
						ability.onUpdate();
					}
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onGameRenderTick(RenderTickEvent event) {
			if(event.phase == Phase.END) {
				for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
					if(ability.isEnabled()) {
						ability.onRenderUpdate(event.renderTickTime);
					}
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onGameOverlay(RenderGameOverlayEvent.Pre event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onPreRenderOverlayEvent(event);
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onGameOverlay(RenderGameOverlayEvent.Post event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onPostRenderOverlayEvent(event);
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onRender(RenderLivingEvent.Post<EntityLivingBase> event) {
			if(event.getEntity() instanceof IIcon) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(event.getX(), event.getY() + event.getEntity().getRenderBoundingBox().maxY, event.getZ());
				GlStateManager.disableLighting();
				GlStateManager.rotate(-event.getRenderer().getRenderManager().playerViewY, 0, 1, 0);
				RenderHelper.drawTexturedRect(new ResourceLocation(OrmoyoUtil.MODID, "textures/gui/undercraft_text_box.png"), -1.2, 0, 0, 0, 256, 67, 3, 1, 256, 256, 1, 1);
				GlStateManager.rotate(-event.getRenderer().getRenderManager().playerViewY, 0, -1, 0);
		        GlStateManager.enableLighting();
				GlStateManager.popMatrix();
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onPlayerRender(RenderPlayerEvent.Pre event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onPrePlayerRender(event);
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onPlayerRender(RenderPlayerEvent.Post event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onPostPlayerRender(event);
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onGuiActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onButtonPressInGui(event);
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onInputUpdate(InputUpdateEvent event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onInputUpdate(event);
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onCameraUpdate(CameraSetup event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onCameraUpdate(event);
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onMouseEvent(MouseEvent event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onMouseEvent(event);
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onMouseEvent(MouseInputEvent event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onMouseInput(event);
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onKeyInput(KeyInputEvent event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onKeyInput(event);
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void onRenderHand(RenderSpecificHandEvent event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onHandRender(event);
				}
			}
		}
		
		@SideOnly(Side.CLIENT)
		@SuppressWarnings("unchecked")
		@SubscribeEvent
		public static void onLoggedOutFromServer(ClientDisconnectionFromServerEvent event) {
			for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
				if(ability.isEnabled()) {
					ability.onClientDisconnectedFromServer(event);
				}
			}
			try {
				Field field = ClientProxy.class.getDeclaredField("unlockedAbilities");
				field.setAccessible(true);
				((Set<Ability>)field.get(null)).clear();
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
