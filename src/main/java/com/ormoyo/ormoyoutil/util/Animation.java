package com.ormoyo.ormoyoutil.util;

import java.util.Iterator;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Animation implements ITick {
	public int ticksExisted;
	boolean removed;
	//Don't forget to only render with a specific element to not render multiple times
	@SideOnly(Side.CLIENT)
	public void renderPreOverlay(RenderGameOverlayEvent.Pre event) {}
	//Don't forget to only render with a specific element to not render multiple times
	@SideOnly(Side.CLIENT)
	public void renderPostOverlay(RenderGameOverlayEvent.Post event) {}
	@SideOnly(Side.CLIENT)
	public void renderWorld(RenderWorldLastEvent event) {}
	
	@Override
	public final void onUpdate(Iterator<? extends ITick> iterator) {
		if(this.removed) return;
		this.ticksExisted++;
		this.onUpdate();
	}
	
	public void onUpdate() {}
	
	public final void remove() {
		this.removed = true;
	}
}
