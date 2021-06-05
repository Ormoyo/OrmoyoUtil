package com.ormoyo.ormoyoutil.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.abilities.Ability;
import com.ormoyo.ormoyoutil.abilities.AbilityEntry;
import com.ormoyo.ormoyoutil.abilities.AbilityEventListener;
import com.ormoyo.ormoyoutil.abilities.Ability.IAbilityEventInvoker;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser.Reader;
import com.ormoyo.ormoyoutil.abilities.AbilitySyncedValue.ISyncedValueParser.Writer;
import com.ormoyo.ormoyoutil.network.AbstractMessage;
import com.ormoyo.ormoyoutil.util.DoubleKeyListMap;
import com.ormoyo.ormoyoutil.util.DoubleKeyMap;
import com.ormoyo.ormoyoutil.util.OrmoyoResourcePackListener;
import com.ormoyo.ormoyoutil.util.OrmoyoResourceTypes;
import com.ormoyo.ormoyoutil.util.TripleKeyListMap;
import com.ormoyo.ormoyoutil.util.TripleKeyMap;
import com.ormoyo.ormoyoutil.util.TripleKeyMap.TripleKeyEntry;
import com.ormoyo.ormoyoutil.util.DoubleKeyMap.DoubleKeyEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	@SuppressWarnings("rawtypes")
	private static TripleKeyMap<Ability, Class<? extends Event>, IAbilityEventInvoker, AbilityEventListener> eventToListener = new TripleKeyListMap<>();
	private static final Set<Ability> unlockedAbilities = new HashSet<>();
	public static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    @Override
    public <T extends AbstractMessage<T>> void handleMessage(final T message, final MessageContext messageContext) {
        if (messageContext.side.isServer()) {
            super.handleMessage(message, messageContext);
        } else {
        	MINECRAFT.addScheduledTask(() -> message.onClientReceived(MINECRAFT, message, MINECRAFT.player, messageContext));
        }
    }
    
    @Override
    public void preInit() {
    	super.preInit();
    	if(MINECRAFT.getResourceManager() instanceof IReloadableResourceManager) {
    		IReloadableResourceManager resourceManager = (IReloadableResourceManager) MINECRAFT.getResourceManager();
    		resourceManager.registerReloadListener(new OrmoyoResourcePackListener());
    	}
    }
    
    @Override
    public void init() {
    	super.init();
    	this.registerKeybinds();
    	OrmoyoResourcePackListener listener = new OrmoyoResourcePackListener();
    	listener.onResourceManagerReload(null, type -> type == OrmoyoResourceTypes.OBJ_MODELS || type == OrmoyoResourceTypes.FONTS);
    }
    
	public void registerKeybinds() {
		for(AbilityEntry entry : Ability.getAbilityRegistry().getValuesCollection()) {
			Ability ability = entry.newInstance(MINECRAFT.player);
			if(ability.getKeybindCode() >= 0) {
				ResourceLocation location = ability.getRegistryName();
				ClientRegistry.registerKeyBinding(new KeyBinding("key." + location.getResourceDomain() + "." + location.getResourcePath(), MathHelper.clamp(ability.getKeybindCode(), Integer.MIN_VALUE, Keyboard.KEYBOARD_SIZE), "key." + location.getResourceDomain() + ".category"));
			}
		}
	}
	
	@Override
	public boolean isServerSide() {
		return MINECRAFT.isIntegratedServerRunning();
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
	public Ability getUnlockedAbility(ResourceLocation name, EntityPlayer player) {
		if(player != null) {
			if(!player.world.isRemote) {
				return super.getUnlockedAbility(name, player);
			}
		}
		for(Ability ability : unlockedAbilities) {
			if(ability.getRegistryName().equals(name)) {
				return ability;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public<T extends Ability> T getUnlockedAbility(Class<T> clazz, EntityPlayer player) {
		if(player != null) {
			if(!player.world.isRemote) {
				return super.getUnlockedAbility(clazz, player);
			}
		}
		for(Ability ability : unlockedAbilities) {
			if(ability.getRegistryName().equals(Ability.getAbilityClassRegistryName(clazz))) {
				return (T) ability;
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
				method.invoke(parser, MINECRAFT, writer, player, value);
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
		return parser.readToClient(MINECRAFT, reader, player);
	}
	
	@Override
	public EntityPlayer getPlayerByUsername(String username) {
		EntityPlayer player = null;
		if(MINECRAFT.world != null) {
			player = Minecraft.getMinecraft().world.getPlayerEntityByName(username);
		}
		if(MINECRAFT.isIntegratedServerRunning()) {
			super.getPlayerByUsername(username);
		}else if(player != null) {
			return player;
		}
		return MINECRAFT.player;
	}
	
	@Override
	public void openGui(Object gui) {
		if(gui instanceof GuiScreen) {
			MINECRAFT.displayGuiScreen((GuiScreen)gui);
		}
	}
	
	@EventBusSubscriber(modid = OrmoyoUtil.MODID, value = Side.CLIENT)
	private static class EventHandler {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@SubscribeEvent
		public static void onEvent(Event event) {
			for(TripleKeyEntry<Ability, Class<? extends Event>, IAbilityEventInvoker, AbilityEventListener> entry : eventToListener.entrySet()) {
				if(!entry.getKey1().isEnabled()) continue;
				if(entry.getKey2() == event.getClass()) {
					if(entry.getKey3().invoke(entry.getKey1(), event)) {
						entry.getValue().invoke(event);
					}
				}
			}
		}
		
		@SubscribeEvent
		public static void onClientDisconnectionFromServer(ClientDisconnectionFromServerEvent event) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					ClientProxy.unlockedAbilities.clear();
					ClientProxy.eventToListener.clear();
				}
			});
		}
	}
}
