package com.ormoyo.util.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.ormoyo.util.OrmoyoUtil;
import com.ormoyo.util.Utils.ConsumerPerform;
import com.ormoyo.util.abilities.Ability;
import com.ormoyo.util.abilities.AbilityEntry;
import com.ormoyo.util.capability.CapabilityHandler;
import com.ormoyo.util.capability.IPlayerData;
import com.ormoyo.util.capability.PlayerData;
import com.ormoyo.util.event.AbilityEvent;
import com.ormoyo.util.event.StatsEvent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = OrmoyoUtil.MODID)
public class CommonEventHandler {
	@SubscribeEvent
	public static void onJoinWorld(EntityJoinWorldEvent event) {
		if(event.getEntity() instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
			IPlayerData capability = player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
			Set<Ability> abilitySet = new HashSet<>();
			for(AbilityEntry entry : Ability.getRegistry().getValuesCollection()) {
				Ability ability = entry.newInstance(player);
				abilitySet.add(ability);
			}
			
			if(MinecraftForge.EVENT_BUS.post(new AbilityEvent.InitAbilityEvent.Pre(player, abilitySet))) return;
			
			for(Ability ability : abilitySet) {
				if(ability.getRequiredLevel() <= 1) {
					capability.UnlockAbility(ability);
				}
			}
			Set<ResourceLocation> set = ObfuscationReflectionHelper.getPrivateValue(PlayerData.class, (PlayerData)capability, "termUnlockedAbilities");
			for(ResourceLocation location : set) {
				for(Ability ability : abilitySet) {
					if(ability.getEntry().getRegistryName().equals(location)) {
						capability.UnlockAbility(ability);
						Map<ResourceLocation, NBTTagCompound> map = ObfuscationReflectionHelper.getPrivateValue(PlayerData.class, (PlayerData)capability, "abilityToTag");
						for(ResourceLocation resource : map.keySet()) {
							if(ability.getEntry().getRegistryName().equals(resource)) {
								ability.readFromNBT(map.get(resource));
							}
						}
					}
				}
			}
			MinecraftForge.EVENT_BUS.post(new AbilityEvent.InitAbilityEvent.Post(player, abilitySet));
		}
	}
	
	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
		if(event.getSource().getTrueSource() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();
			IPlayerData capability = player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
			for(Ability ability : capability.getUnlockedAbilities()) {
				if(ability.isEnabled()) {
					ability.onDeathEvent(event);
				}
			}
		}else if(event.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			IPlayerData capability = player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
			for(Ability ability : capability.getUnlockedAbilities()) {
				if(ability.isEnabled()) {
					ability.onDeathEvent(event);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerLoadingFromFile(PlayerEvent.LoadFromFile event) {
		IPlayerData capability = event.getEntityPlayer().getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
		EntityPlayer player = ObfuscationReflectionHelper.getPrivateValue(PlayerData.class, (PlayerData)capability, "player");
		if(player == null) {
			ObfuscationReflectionHelper.setPrivateValue(PlayerData.class, (PlayerData)capability, event.getEntityPlayer(), "player");
		}
	}
	
	@SubscribeEvent
	public static void PlayerCloneEvent(PlayerEvent.Clone event) {
		if(event.isWasDeath()) {
			IPlayerData capOld = event.getOriginal().getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
			IPlayerData capNew = event.getEntityPlayer().getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null);
			ArrayList<Ability> oldList = ObfuscationReflectionHelper.getPrivateValue(PlayerData.class, (PlayerData)capOld, "unlockedAbilities");
			ObfuscationReflectionHelper.setPrivateValue(PlayerData.class, (PlayerData)capNew, oldList, "unlockedAbilities");
		}
	}
	
	@SubscribeEvent
	public static void onEntityExpCalculation(StatsEvent.CalculateEntityExp event) {
		if(Loader.isModLoaded("srparasites")) {
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
	
	//@SubscribeEvent
	//public static void onEntityHurted(LivingHurtEvent event) {
	   // if(event.getEntityLiving() instanceof EntityPlayer && event.getEntityLiving().getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null).getBadTimeMode()) {
	    	//if(event.getSource() != DamageSource.FALL && event.getSource() != DamageSource.OUT_OF_WORLD && event.getSource() != DamageSource.DROWN && event.getSource() != DamageSource.IN_FIRE && event.getSource() != DamageSource.ON_FIRE && event.getSource() != DamageSource.MAGIC && event.getSource() != DamageSource.IN_WALL) {
	    		//event.setCanceled(true);
	    	//}
	    //}
	//}
	
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
}
