package com.ormoyo.ormoyoutil.util.resourcelocation;

import net.minecraft.util.ResourceLocation;

public class AdvancedResourceLocation extends ResourceLocation {
	protected final int u;
	protected final int v;
	protected final int width;
	protected final int height;
	protected final int imageWidth;
	protected final int imageHeight;
	
    protected AdvancedResourceLocation(int unused, int u, int v, int width, int height, int imageWidth, int imageHeight, String... resourceName)
    {
    	super(unused, resourceName);
    	this.u = u;
    	this.v = v;
    	this.width = width;
    	this.height = height;
    	this.imageWidth = imageWidth;
    	this.imageHeight = imageHeight;
    }

    public AdvancedResourceLocation(String resourceName, int u, int v ,int width, int height, int imageWidth, int imageHeight)
    {
        super(resourceName);
    	this.u = u;
    	this.v = v;
        this.width = width;
        this.height = height;
    	this.imageWidth = imageWidth;
    	this.imageHeight = imageHeight;
    }

    public AdvancedResourceLocation(String resourceDomainIn, String resourcePathIn, int u, int v, int width, int height, int imageWidth, int imageHeight)
    {
        super(resourceDomainIn, resourcePathIn);
    	this.u = u;
    	this.v = v;
        this.width = width;
        this.height = height;
    	this.imageWidth = imageWidth;
    	this.imageHeight = imageHeight;
    }
    
    public AdvancedResourceLocation(int u, int v, int width, int height, int imageWidth, int imageHeight)
    {
        this("Null", u, v, width, height, imageWidth, imageHeight);
    }
    
    public int getU() {
    	return this.u;
    }
    
    public int getV() {
    	return this.v;
    }
    
    public int getWidth() {
		return this.width;
    }
    
    public int getHeight() {
		return this.width;
    }
    
    public int getImageWidth() {
    	return this.imageWidth;
    }
    
    public int getImageHeight() {
    	return this.imageHeight;
    }
}
