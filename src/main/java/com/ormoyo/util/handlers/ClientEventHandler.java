package com.ormoyo.util.handlers;

import java.lang.reflect.Field;
import java.util.Set;

import com.ormoyo.util.OrmoyoUtil;
import com.ormoyo.util.abilities.Ability;
import com.ormoyo.util.client.RenderHelper;
import com.ormoyo.util.icon.IIcon;
import com.ormoyo.util.proxy.ClientProxy;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = OrmoyoUtil.MODID, value = Side.CLIENT)
public class ClientEventHandler {
	
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
	
	@SubscribeEvent
	public static void onGameOverlay(RenderGameOverlayEvent.Pre event) {
		for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
			if(ability.isEnabled()) {
				ability.onPreRenderOverlayEvent(event);
			}
		}
	}
	
	@SubscribeEvent
	public static void onGameOverlay(RenderGameOverlayEvent.Post event) {
		for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
			if(ability.isEnabled()) {
				ability.onPostRenderOverlayEvent(event);
			}
		}
	}
	
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
	
	@SubscribeEvent
	public static void onPlayerRender(RenderPlayerEvent.Pre event) {
		
	}
	
	@SubscribeEvent
	public static void onGuiActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event) {
		for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
			if(ability.isEnabled()) {
				ability.onButtonPressInGui(event);
			}
		}
	}
	
	@SubscribeEvent
	public static void onInputUpdate(InputUpdateEvent event) {
		for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
			if(ability.isEnabled()) {
				ability.onInputUpdate(event);
			}
		}
	}
	
	@SubscribeEvent
	public static void onCameraUpdate(CameraSetup event) {
		for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
			if(ability.isEnabled()) {
				ability.onCameraUpdate(event);
			}
		}
	}
	
	@SubscribeEvent
	public static void onRenderHand(RenderSpecificHandEvent event) {
		for(Ability ability : OrmoyoUtil.proxy.getUnlockedAbilities(null)) {
			if(ability.isEnabled()) {
				ability.onHandRender(event);
			}
		}
	}
	
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
