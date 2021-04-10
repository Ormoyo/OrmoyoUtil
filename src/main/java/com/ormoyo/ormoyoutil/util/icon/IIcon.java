package com.ormoyo.ormoyoutil.util.icon;

import com.ormoyo.ormoyoutil.util.resourcelocation.IconResourceLocation;

import net.minecraft.util.ResourceLocation;

public interface IIcon {
	IconResourceLocation[] getIcons();
	
	IconResourceLocation getIcon(int index);
}
