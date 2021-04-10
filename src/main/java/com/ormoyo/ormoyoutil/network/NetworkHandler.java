package com.ormoyo.ormoyoutil.network;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.ormoyo.ormoyoutil.OrmoyoUtil;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void injectNetworkWrapper(ModContainer mod, ASMDataTable data) {
        SetMultimap<String, ASMDataTable.ASMData> annotations = data.getAnnotationsFor(mod);
        if (annotations != null) {
            Set<ASMDataTable.ASMData> targetList = annotations.get(NetworkWrapper.class.getName());
            ClassLoader classLoader = Loader.instance().getModClassLoader();
            for (ASMDataTable.ASMData target : targetList) {
                try {
                    Class<?> targetClass = Class.forName(target.getClassName(), true, classLoader);
                    Field field = targetClass.getDeclaredField(target.getObjectName());
                    field.setAccessible(true);
                    NetworkWrapper annotation = field.getAnnotation(NetworkWrapper.class);
                    SimpleNetworkWrapper networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(mod.getModId());
                    field.set(null, networkWrapper);
                    for (Class messageClass : annotation.value()) {
                        registerMessage(networkWrapper, messageClass);
                    }
                } catch (Exception e) {
                    OrmoyoUtil.LOGGER.fatal("Failed to inject network wrapper for mod container {}", mod, e);
                }
            }
        }
    }
    
    public static <T extends AbstractMessage<T> & IMessageHandler<T, IMessage>> void registerMessage(SimpleNetworkWrapper networkWrapper, Class<T> clazz) {
        try {
            AbstractMessage<T> message = clazz.getDeclaredConstructor().newInstance();
            if (message.registerOnSide(Side.CLIENT)) {
                registerMessage(networkWrapper, clazz, Side.CLIENT);
            }
            if (message.registerOnSide(Side.SERVER)) {
                registerMessage(networkWrapper, clazz, Side.SERVER);
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
    
    private static Map<SimpleNetworkWrapper, Integer> idMap = Maps.newHashMap();
    
    @Deprecated
    public static <T extends AbstractMessage<T> & IMessageHandler<T, IMessage>> void registerMessage(SimpleNetworkWrapper networkWrapper, Class<T> clazz, Side side) {
        int id = 0;
        if (idMap.containsKey(networkWrapper)) {
            id = idMap.get(networkWrapper);
        }
        networkWrapper.registerMessage(clazz, clazz, id, side);
        idMap.put(networkWrapper, id + 1);
    }
}
