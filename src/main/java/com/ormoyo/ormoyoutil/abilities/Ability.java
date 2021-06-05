package com.ormoyo.ormoyoutil.abilities;

import java.util.Set;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.capability.AbilityData;
import com.ormoyo.ormoyoutil.capability.CapabilityHandler;
import com.ormoyo.ormoyoutil.capability.IAbilityData;
import com.ormoyo.ormoyoutil.proxy.ClientProxy;
import com.ormoyo.ormoyoutil.util.TripleKeyMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
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
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

public abstract class Ability {
	public Ability(EntityPlayer owner) {
		this.owner = owner;
		this.registryName = getAbilityClassRegistryName(this.getClass());
	}
	
	protected boolean hasBeenPressed;
	protected int cooldown;
	protected boolean startCooldown;
	
	protected final EntityPlayer owner;
	private final ResourceLocation registryName;
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
	 * @param object 
	 */
	public void onUnlocked() {}
	
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
		ResourceLocation location = this.registryName;
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
		if(!isEnabled) {
			this.onAbilityDisabled();
			AbilitySyncedValue.invokeMethod(this, "setIsEnabled", isEnabled);
		}
	}
	
	/**
	 * If the ability is disabled all it's methods are not gonna get called
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
		return this.registryName.toString();
	}
	
	public ITextComponent getTranslatedName() {
		return new TextComponentTranslation("ability." + this.registryName.getResourceDomain() + "." + this.registryName.getResourcePath() + ".name");
	}
	
	public ResourceLocation getRegistryName() {
		return this.registryName;
	}
	
	private static IForgeRegistry<AbilityEntry> ABILITY_REGISTRY;
	public static IForgeRegistry<AbilityEntry> getAbilityRegistry() {
		return ABILITY_REGISTRY;
	}
	
	private static IForgeRegistry<AbilityEventEntry> ABILITY_EVENT_REGISTRY;
	public static IForgeRegistry<AbilityEventEntry> getAbilityEventRegistry(){
		return ABILITY_EVENT_REGISTRY;
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
			if(this.registryName != null && ability.registryName != null) {
				if(this.registryName.equals(ability.registryName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static ResourceLocation getAbilityClassRegistryName(Class<? extends Ability> clazz) {
		for(Entry<ResourceLocation, AbilityEntry> entry : Ability.getAbilityRegistry().getEntries()) {
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
	static class EventHandler {
		//COMMON SIDE
		@SubscribeEvent
		public static void onNewRegistry(RegistryEvent.NewRegistry event) {
			ABILITY_REGISTRY = new RegistryBuilder<AbilityEntry>().setName(new ResourceLocation(OrmoyoUtil.MODID, "ability")).setType(AbilityEntry.class).setIDRange(0, 2048).create();
			ABILITY_EVENT_REGISTRY = new RegistryBuilder<AbilityEventEntry>().setName(new ResourceLocation(OrmoyoUtil.MODID, "ability_event")).setType(AbilityEventEntry.class).setIDRange(0, 2048).create();	
		}
		
		@SubscribeEvent
		public static void registerAbilities(RegistryEvent.Register<AbilityEntry> event) {
			event.getRegistry().register(new AbilityEntry(new ResourceLocation(OrmoyoUtil.MODID, "stats"), AbilityStats.class));
		}
		
		@SubscribeEvent
		public static void registerAbilityEventInvokers(RegistryEvent.Register<AbilityEventEntry> event) {
			register(event, EntityEvent.class, ENTITY_EVENT);
			register(event, PlayerEvent.class, PLAYER_EVENT);
			register(event, LivingAttackEvent.class, LIVING_ATTACK_EVENT);
			register(event, LivingDeathEvent.class, LIVING_DEATH_EVENT);
			register(event, LivingKnockBackEvent.class, LIVING_KNOCKBACK_EVENT);
			register(event, ProjectileImpactEvent.class, PROJECTILE_IMPACT_EVENT);
			register(event, ProjectileImpactEvent.Arrow.class, PROJECTILE_IMPACT_ARROW_EVENT);
			register(event, ProjectileImpactEvent.Fireball.class, PROJECTILE_IMPACT_FIREBALL_EVENT);
			register(event, ProjectileImpactEvent.Throwable.class, PROJECTILE_IMPACT_THROWABLE_EVENT);
			register(event, ClientTickEvent.class, CLIENT_TICK_EVENT);
			register(event, RenderTickEvent.class, RENDER_TICK_EVENT);
			register(event, RenderGameOverlayEvent.class, RENDER_GAME_OVERLAY_EVENT);
			register(event, RenderLivingEvent.class, RENDER_LIVING_EVENT);
			register(event, RenderPlayerEvent.class, RENDER_PLAYER_EVENT);
			register(event, InputUpdateEvent.class, INPUT_UPDATE_EVENT);
			register(event, CameraSetup.class, CAMERA_SETUP_EVENT);
			register(event, MouseEvent.class, MOUSE_EVENT);
			register(event, MouseInputEvent.class, MOUSE_INPUT_EVENT);
			register(event, KeyInputEvent.class, KEY_INPUT_EVENT);
			register(event, RenderSpecificHandEvent.class, RENDER_SPECIFIC_HAND_EVENT);
			register(event, ClientDisconnectionFromServerEvent.class, CLIENT_DISCONNECTION_FROM_SERVER_EVENT);
		}
		
		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public static void registerAbilityEventInvokersOnClient(RegistryEvent.Register<AbilityEventEntry> event) {
			register(event, GuiScreenEvent.class, GUI_SCREEN_EVENT);
		}
		
		private static<T extends Event> void register(RegistryEvent.Register<AbilityEventEntry> event, Class<T> clazz, IAbilityEventInvoker<T> invoker) {
			String name = Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1);
			event.getRegistry().register(new AbilityEventEntry(new ResourceLocation(OrmoyoUtil.MODID, name), clazz, invoker));
		}
		
		public static final IAbilityEventInvoker<EntityEvent> ENTITY_EVENT = new IAbilityEventInvoker<EntityEvent>() {
			@Override
			public boolean invoke(Ability ability, EntityEvent event) {
				return event.getEntity() == ability.owner;
			}
		};
		
		public static final IAbilityEventInvoker<LivingAttackEvent> LIVING_ATTACK_EVENT = new IAbilityEventInvoker<LivingAttackEvent>() {
			@Override
			public boolean invoke(Ability ability, LivingAttackEvent event) {
				return event.getEntityLiving() == ability.owner || event.getSource().getTrueSource() == ability.owner;
			}
		};
		
		public static final IAbilityEventInvoker<LivingDeathEvent> LIVING_DEATH_EVENT = new IAbilityEventInvoker<LivingDeathEvent>() {
			@Override
			public boolean invoke(Ability ability, LivingDeathEvent event) {
				return event.getEntityLiving() == ability.owner || event.getSource().getTrueSource() == ability.owner;
			}
		};
		
		public static final IAbilityEventInvoker<LivingKnockBackEvent> LIVING_KNOCKBACK_EVENT = new IAbilityEventInvoker<LivingKnockBackEvent>() {
			@Override
			public boolean invoke(Ability ability, LivingKnockBackEvent event) {
				return event.getEntityLiving() == ability.owner || event.getAttacker() == ability.owner;
			}
		};
		
		public static final IAbilityEventInvoker<ProjectileImpactEvent> PROJECTILE_IMPACT_EVENT = new IAbilityEventInvoker<ProjectileImpactEvent>() {
			@Override
			public boolean invoke(Ability ability, ProjectileImpactEvent event) {
				return event.getRayTraceResult().entityHit == ability.owner;
			}
		};
		
		public static final IAbilityEventInvoker<ProjectileImpactEvent.Arrow> PROJECTILE_IMPACT_ARROW_EVENT = new IAbilityEventInvoker<ProjectileImpactEvent.Arrow>() {
			@Override
			public boolean invoke(Ability ability, ProjectileImpactEvent.Arrow event) {
				return event.getRayTraceResult().entityHit == ability.owner || event.getArrow().shootingEntity == ability.owner;
			}
		};
		
		public static final IAbilityEventInvoker<ProjectileImpactEvent.Fireball> PROJECTILE_IMPACT_FIREBALL_EVENT = new IAbilityEventInvoker<ProjectileImpactEvent.Fireball>() {
			@Override
			public boolean invoke(Ability ability, ProjectileImpactEvent.Fireball event) {
				return event.getRayTraceResult().entityHit == ability.owner || event.getFireball().shootingEntity == ability.owner;
			}
		};
		
		public static final IAbilityEventInvoker<ProjectileImpactEvent.Throwable> PROJECTILE_IMPACT_THROWABLE_EVENT = new IAbilityEventInvoker<ProjectileImpactEvent.Throwable>() {
			@Override
			public boolean invoke(Ability ability, ProjectileImpactEvent.Throwable event) {
				return event.getRayTraceResult().entityHit == ability.owner || event.getThrowable().getThrower() == ability.owner;
			}
		};
		
		public static final IAbilityEventInvoker<PlayerEvent> PLAYER_EVENT = new IAbilityEventInvoker<PlayerEvent>() {
			@Override
			public boolean invoke(Ability ability, PlayerEvent event) {
				return event.player == ability.owner;
			}
		};
		
		public static final IAbilityEventInvoker<ClientTickEvent> CLIENT_TICK_EVENT = new IAbilityEventInvoker<ClientTickEvent>() {
			@Override
			public boolean invoke(Ability ability, ClientTickEvent event) {
				return true;
			}
		};
		
		public static final IAbilityEventInvoker<RenderTickEvent> RENDER_TICK_EVENT = new IAbilityEventInvoker<RenderTickEvent>() {
			@Override
			public boolean invoke(Ability ability, RenderTickEvent event) {
				return true;
			}
		};
		
		public static final IAbilityEventInvoker<RenderGameOverlayEvent> RENDER_GAME_OVERLAY_EVENT = new IAbilityEventInvoker<RenderGameOverlayEvent>() {
			@Override
			public boolean invoke(Ability ability, RenderGameOverlayEvent event) {
				return true;
			}
		};
		
		@SuppressWarnings("rawtypes")
		public static final IAbilityEventInvoker<RenderLivingEvent> RENDER_LIVING_EVENT = new IAbilityEventInvoker<RenderLivingEvent>() {
			@Override
			public boolean invoke(Ability ability, RenderLivingEvent event) {
				return event.getEntity() != ability.owner;
			}
		};
		
		public static final IAbilityEventInvoker<RenderPlayerEvent> RENDER_PLAYER_EVENT = new IAbilityEventInvoker<RenderPlayerEvent>() {
			@Override
			public boolean invoke(Ability ability, RenderPlayerEvent event) {
				return event.getEntityPlayer() == ability.owner;
			}
		};
		
		public static final IAbilityEventInvoker<GuiScreenEvent> GUI_SCREEN_EVENT = new IAbilityEventInvoker<GuiScreenEvent>() {
			@Override
			public boolean invoke(Ability ability, GuiScreenEvent event) {
				return true;
			}
		};
		
		public static final IAbilityEventInvoker<InputUpdateEvent> INPUT_UPDATE_EVENT = new IAbilityEventInvoker<InputUpdateEvent>() {
			@Override
			public boolean invoke(Ability ability, InputUpdateEvent event) {
				return true;
			}
		};
		
		public static final IAbilityEventInvoker<CameraSetup> CAMERA_SETUP_EVENT = new IAbilityEventInvoker<CameraSetup>() {
			@Override
			public boolean invoke(Ability ability, CameraSetup event) {
				return true;
			}
		};
		
		public static final IAbilityEventInvoker<MouseEvent> MOUSE_EVENT = new IAbilityEventInvoker<MouseEvent>() {
			@Override
			public boolean invoke(Ability ability, MouseEvent event) {
				return true;
			}
		};
		
		public static final IAbilityEventInvoker<MouseInputEvent> MOUSE_INPUT_EVENT = new IAbilityEventInvoker<MouseInputEvent>() {
			@Override
			public boolean invoke(Ability ability, MouseInputEvent event) {
				return true;
			}
		};
		
		public static final IAbilityEventInvoker<KeyInputEvent> KEY_INPUT_EVENT = new IAbilityEventInvoker<KeyInputEvent>() {
			@Override
			public boolean invoke(Ability ability, KeyInputEvent event) {
				return true;
			}
		};
		
		public static final IAbilityEventInvoker<RenderSpecificHandEvent> RENDER_SPECIFIC_HAND_EVENT = new IAbilityEventInvoker<RenderSpecificHandEvent>() {
			@Override
			public boolean invoke(Ability ability, RenderSpecificHandEvent event) {
				return true;
			}
		};
		
		public static final IAbilityEventInvoker<ClientDisconnectionFromServerEvent> CLIENT_DISCONNECTION_FROM_SERVER_EVENT = new IAbilityEventInvoker<ClientDisconnectionFromServerEvent>() {
			@Override
			public boolean invoke(Ability ability, ClientDisconnectionFromServerEvent event) {
				return true;
			}
		};
		
		@SubscribeEvent
		public static void onPlayerTickEvent(PlayerTickEvent event) {
			if(event.phase == Phase.END) {
				if(event.side.equals(Side.SERVER)) {
					for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(event.player)) {
						if(ability.isEnabled()) {
							ability.onUpdate();
						}
					}
				}else {
					for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(event.player)) {
						if(ability.isEnabled()) {
							ability.onUpdate();
						}
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
			if(event.isWasDeath()) {
				IAbilityData o = event.getOriginal().getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
				IAbilityData n = event.getEntityPlayer().getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
				if(n instanceof AbilityData && o instanceof AbilityData) {
					AbilityData oldcap = (AbilityData) o;
					AbilityData newcap = (AbilityData) n;
					ObfuscationReflectionHelper.setPrivateValue(AbilityData.class, newcap, oldcap.getPlayer(), "player");
					ObfuscationReflectionHelper.setPrivateValue(AbilityData.class, newcap, oldcap.getUnlockedAbilities(), "unlockedAbilities");
					ObfuscationReflectionHelper.setPrivateValue(AbilityData.class, newcap, ObfuscationReflectionHelper.getPrivateValue(AbilityData.class, oldcap, "eventToListener"), "eventToListener");
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static class AbilityEventEntry extends Impl<AbilityEventEntry> {
		private final Class<? extends Event> event;
		private final IAbilityEventInvoker<? extends Event> invoker;
		public<T extends Event> AbilityEventEntry(ResourceLocation name, Class<T> event, IAbilityEventInvoker<T> invoker) {
			this.setRegistryName(name);
			this.event = event;
			this.invoker = invoker;
		}
		
		public Class<? extends Event> getEventClass(){
			return this.event;
		}
		
		public IAbilityEventInvoker getEventInvoker() {
			return this.invoker;
		}
		
		@Override
		public String toString() {
			return this.event.getName();
		}
	}
	
	public static interface IAbilityEventInvoker<T extends Event> {
		public boolean invoke(Ability ability, T event);
	}
}
