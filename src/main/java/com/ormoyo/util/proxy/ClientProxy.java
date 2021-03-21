package com.ormoyo.util.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.ormoyo.util.abilities.Ability;
import com.ormoyo.util.abilities.AbilityEntry;
import com.ormoyo.util.abilities.AbilitySyncedValue.ISyncedValueParser;
import com.ormoyo.util.abilities.AbilitySyncedValue.ISyncedValueParser.Reader;
import com.ormoyo.util.abilities.AbilitySyncedValue.ISyncedValueParser.Writer;
import com.ormoyo.util.network.AbstractMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	private static final Set<Ability> unlockedAbilities = new HashSet<>();
    @Override
    public <T extends AbstractMessage<T>> void handleMessage(final T message, final MessageContext messageContext) {
        if (messageContext.side.isServer()) {
            super.handleMessage(message, messageContext);
        } else {
            Minecraft.getMinecraft().addScheduledTask(() -> message.onClientReceived(Minecraft.getMinecraft(), message, Minecraft.getMinecraft().player, messageContext));
        }
    }
    
    @Override
    public void init() {
    	super.init();
    	this.registerKeybinds();
    }
    
	public void registerKeybinds() {
		for(AbilityEntry entry : Ability.getRegistry().getValuesCollection()) {
			Ability ability = entry.newInstance(Minecraft.getMinecraft().player);
			if(ability.getKeybindCode() >= 0) {
				ResourceLocation location = ability.getEntry().getRegistryName();
				ClientRegistry.registerKeyBinding(new KeyBinding("key." + location.getResourceDomain() + "." + location.getResourcePath(), MathHelper.clamp(ability.getKeybindCode(), Integer.MIN_VALUE, Keyboard.KEYBOARD_SIZE), "key." + location.getResourceDomain() + ".catagory"));
			}
		}
	}
    
	@Override
	public Set<Ability> getUnlockedAbilities(EntityPlayer player) {
		if(player != null) {
			if(!player.world.isRemote) {
				return super.getUnlockedAbilities(player);
			}
		}
		return Collections.unmodifiableSet(unlockedAbilities);
	}
	
	@Override
	public Ability getUnlockedAbility(EntityPlayer player, ResourceLocation name) {
		if(player != null) {
			if(!player.world.isRemote) {
				return super.getUnlockedAbility(player, name);
			}
		}
		for(Ability ability : unlockedAbilities) {
			if(ability.getEntry().getRegistryName().equals(name)) {
				return ability;
			}
		}
		return null;
	}
	
	@Override
	public Ability getUnlockedAbility(EntityPlayer player, Class<? extends Ability> clazz) {
		if(player != null) {
			if(!player.world.isRemote) {
				return super.getUnlockedAbility(player, clazz);
			}
		}
		for(Ability ability : unlockedAbilities) {
			if(ability.getEntry().getRegistryName().equals(Ability.getAbilityClassRegistryName(clazz))) {
				return ability;
			}
		}
		return null;
	}
	
	@Override
	public void writeToAbility(ISyncedValueParser<?> parser, Writer writer, Object value, Class<?> clazz, EntityPlayer player) {
		if(!player.world.isRemote) {
			super.writeToAbility(parser, writer, value, clazz, player);
		}else {
			try {
				Method method = parser.getClass().getDeclaredMethod("writeToServer", Minecraft.class, Writer.class, EntityPlayer.class, clazz);
				method.setAccessible(true);
				method.invoke(parser, Minecraft.getMinecraft(), writer, player, value);
			} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
			}
		}
	}
	
	@Override
	public<T> T readToAbility(ISyncedValueParser<T> parser, Reader reader, EntityPlayer player) {
		if(!player.world.isRemote) {
			return super.readToAbility(parser, reader, player);
		}
		return parser.readToClient(Minecraft.getMinecraft(), reader, player);
	}
	
	@Override
	public EntityPlayer getPlayerByUsername(String username) {
		if(Minecraft.getMinecraft().isIntegratedServerRunning()) {
			super.getPlayerByUsername(username);
		}
		return Minecraft.getMinecraft().player;
	}
}
