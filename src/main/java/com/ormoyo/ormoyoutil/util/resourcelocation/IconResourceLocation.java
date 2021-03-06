package com.ormoyo.ormoyoutil.util.resourcelocation;

import com.ormoyo.ormoyoutil.util.FontHandler;
import com.ormoyo.ormoyoutil.util.FontHandler.Font;

import net.minecraft.util.SoundEvent;

public class IconResourceLocation extends AdvancedResourceLocation {
	private final double scale;
	private final FontHandler.Font font;
	private final SoundEvent soundBeep;

    protected IconResourceLocation(int unused, int u, int v, int width, int height, int imageWidth, int imageHeight, double scale, FontHandler.Font font, SoundEvent vocalBeep, String... resourceName)
    {
    	super(unused, u, v, width, height, imageWidth, imageHeight, resourceName);
    	this.scale = scale;
    	this.font = font;
    	this.soundBeep = vocalBeep;
    }

    public IconResourceLocation(String resourceName, int u, int v ,int width, int height, int imageWidth, int imageHeight, double scale, FontHandler.Font font, SoundEvent vocalBeep)
    {
        super(resourceName, u, v, width, height, imageWidth, imageHeight);
    	this.scale = scale;
    	this.font = font;
    	this.soundBeep = vocalBeep;
    }

    public IconResourceLocation(String resourceDomainIn, String resourcePathIn, int u, int v, int width, int height, int imageWidth, int imageHeight, double scale, FontHandler.Font font, SoundEvent vocalBeep)
    {
        super(resourceDomainIn, resourcePathIn, u, v, width, height, imageWidth, imageHeight);
    	this.scale = scale;
    	this.font = font;
    	this.soundBeep = vocalBeep;
    }
    
    public IconResourceLocation(int u, int v, int width, int height, int imageWidth, int imageHeight, double scale, FontHandler.Font font, SoundEvent vocalBeep)
    {
        super(u, v, width, height, imageWidth, imageHeight);
    	this.scale = scale;
    	this.font = font;
    	this.soundBeep = vocalBeep;
    }
    
    public double getScale() {
    	return this.scale;
    }
    
    public FontHandler.Font getFont() {
    	return this.font;
    }
    
    public SoundEvent getVocalBeep() {
    	return this.soundBeep;
    }
}
