package com.ormoyo.util.icon;

import com.ormoyo.util.resourcelocation.IconResourceLocation;

import net.minecraft.util.ResourceLocation;

public interface IIcon {
	IconResourceLocation[] getIcons();
	
	IconResourceLocation getIcon(int index);
}
