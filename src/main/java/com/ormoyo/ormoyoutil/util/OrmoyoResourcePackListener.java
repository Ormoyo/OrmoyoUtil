package com.ormoyo.ormoyoutil.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Multimap;
import com.ormoyo.ormoyoutil.client.font.FontHelper;
import com.ormoyo.ormoyoutil.client.font.FontLoader;
import com.ormoyo.ormoyoutil.client.model.obj.OBJLoader;
import com.ormoyo.ormoyoutil.client.model.obj.OBJModel;
import com.ormoyo.ormoyoutil.client.model.obj.OBJModel.OBJModelHandler;
import com.ormoyo.ormoyoutil.client.model.obj.OBJModelBase;
import com.ormoyo.ormoyoutil.client.model.obj.OBJModelEntry;
import com.ormoyo.ormoyoutil.util.FontHandler.Font;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

public class OrmoyoResourcePackListener implements ISelectiveResourceReloadListener {
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
		if(resourcePredicate.test(OrmoyoResourceTypes.OBJ_MODELS)) {
			loadOBJModels();
		}
		if(resourcePredicate.test(OrmoyoResourceTypes.FONTS)) {
			loadFonts();
		}
	}
	
	private static void loadOBJModels() {
		Collection<OBJModelEntry> entries = OBJModel.getObjModelRegistery().getValuesCollection();
		Multimap<OBJModelEntry, OBJModelBase> m = ObfuscationReflectionHelper.getPrivateValue(OBJModelHandler.class, null, "m");
		ProgressBar bar = ProgressManager.push("Loading Obj Models", entries.size(), true);
		for(OBJModelEntry entry : entries) {
			bar.step("Loading");
			ResourceLocation registryName = entry.getRegistryName();
			OBJModel model = OBJLoader.loadOBJModel(new ResourceLocation(registryName.getResourceDomain(), "models/" + registryName.getResourcePath()), entry.removeDuplicateVertices(), entry.hasJsonFile());
			ObfuscationReflectionHelper.setPrivateValue(OBJModelEntry.class, entry, model, "model");
			if(m.containsKey(entry)) {
				for(OBJModelBase base : m.get(entry)) {
					ObfuscationReflectionHelper.setPrivateValue(OBJModelBase.class, base, model, "model");
				}
			}
		}
		ProgressManager.pop(bar);
	}
	
	private static void loadFonts() {
		Collection<Font> entries = FontHandler.getFontRegistry().getValuesCollection();
		ProgressBar bar = ProgressManager.push("Loading Custom Fonts", entries.size(), true);
		for(Font entry : entries) {
			bar.step("Loading");
			FontLoader.loadFont(entry.getType(), entry.getResourceLocation(), entry.isAntiAliasing());
		}
		ProgressManager.pop(bar);

	}
}