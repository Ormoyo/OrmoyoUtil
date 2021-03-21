package com.ormoyo.util.event;

import com.ormoyo.util.icon.Icon.FontEntry;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a font render is happening in {@link FontHelper}
 */
public class FontRenderEvent extends Event {
	protected String text;
	protected FontEntry font;
	
	@Cancelable
	public static class Pre extends FontRenderEvent {

		public Pre(String text, FontEntry font) {
			this.text = text;
			this.font = font;
		}
		
		public void setFont(FontEntry font) {
			this.font = font;
		}
		
		public void setText(String text) {
			this.text = text;
		}
	}
	
	public static class Post extends FontRenderEvent {
		public Post(String text, FontEntry font) {
			this.text = text;
			this.font = font;
		}
	}
	
	public String getText() {
		return this.text;
	}

	public FontEntry getFont() {
		return this.font;
	}
}
