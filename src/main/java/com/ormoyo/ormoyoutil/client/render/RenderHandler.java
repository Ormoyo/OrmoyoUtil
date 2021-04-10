package com.ormoyo.ormoyoutil.client.render;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import com.google.common.collect.SetMultimap;
import com.ormoyo.ormoyoutil.OrmoyoUtil;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

public class RenderHandler {
	@SuppressWarnings({"rawtypes", "unchecked" })
	public static void injectRender(ModContainer mod, ASMDataTable data) {
        SetMultimap<String, ASMDataTable.ASMData> annotations = data.getAnnotationsFor(mod);
        if (annotations != null) {
            Set<ASMDataTable.ASMData> targetList = annotations.get(InjectRender.class.getName());
            ClassLoader classLoader = Loader.instance().getModClassLoader();
            for (ASMDataTable.ASMData target : targetList) {
                try {
                    Class<?> clazz = (Class<?>) Class.forName(target.getClassName(), true, classLoader);
                    InjectRender annotation = clazz.getAnnotation(InjectRender.class);
                    RenderingRegistry.registerEntityRenderingHandler(annotation.value(), new IRenderFactory() {
						@Override
						public Render createRenderFor(RenderManager manager) {
							try {
								return (Render) clazz.getDeclaredConstructor(RenderManager.class).newInstance(manager);
							} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
								e.printStackTrace();
							}
							return null;
						}
					});
                } catch (Exception e) {
                    OrmoyoUtil.LOGGER.fatal("Failed to inject render class {} for mod container {}", target.getClassName(), mod, e);
                }
            }
        }
	}
}
