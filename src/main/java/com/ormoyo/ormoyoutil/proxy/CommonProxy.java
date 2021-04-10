package com.ormoyo.ormoyoutil.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser.Reader;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser.Writer;
import com.ormoyo.ormoyoutil.capability.CapabilityHandler;
import com.ormoyo.ormoyoutil.network.AbstractMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.server.FMLServerHandler;

public class CommonProxy {
    public <T extends AbstractMessage<T>> void handleMessage(final T message, final MessageContext messageContext) {
        WorldServer world = (WorldServer) messageContext.getServerHandler().player.world;
        world.addScheduledTask(() -> message.onServerReceived(FMLCommonHandler.instance().getMinecraftServerInstance(), message, messageContext.getServerHandler().player, messageContext));
    }
    
    public void preInit() {
    	CapabilityHandler.registerCapabilities();
    }
    
    public void init() {
    }
    
    public void postInit() {
    }
    
	public Set<Ability> getUnlockedAbilities(EntityPlayer player) {
		if(player != null) {
			return player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null).getUnlockedAbilities();
		}
		return null;
	}
	
	public Ability getUnlockedAbility(EntityPlayer player, ResourceLocation name) {
		if(player != null) {
			return player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null).getUnlockedAbility(name);
		}
		return null;
	}
	
	public<T extends Ability> T getUnlockedAbility(EntityPlayer player, Class<T> clazz) {
		if(player != null) {
			return player.getCapability(CapabilityHandler.CAPABILITY_PLAYER_DATA, null).getUnlockedAbility(clazz);
		}
		return null;
	}
	
	public void writeToAbility(ISyncedValueParser<?> iSyncedValueParser, Writer writer, Object value, Class<?> clazz, EntityPlayer player) {
		try {
			Method method = iSyncedValueParser.getClass().getDeclaredMethod("writeToClient", MinecraftServer.class, Writer.class, EntityPlayer.class, clazz);
			method.setAccessible(true);
			method.invoke(iSyncedValueParser, FMLCommonHandler.instance().getMinecraftServerInstance(), writer, player, value);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
		}
	}
	
	public<T> T readToAbility(ISyncedValueParser<T> parser, Reader reader, EntityPlayer player) {
		return parser.readToServer(FMLServerHandler.instance().getServer(), reader, player);
	}
	
	public EntityPlayer getPlayerByUsername(String username) {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(username);
	}
}
