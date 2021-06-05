package com.ormoyo.ormoyoutil.event;

import com.ormoyo.ormoyoutil.util.FontHandler;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Called when a font render is happening in {@link FontHelper}
 */
public class FontRenderEvent extends Event {
	protected String text;
	protected FontHandler.Font font;
	
	@Cancelable
	public static class Pre extends FontRenderEvent {

		public Pre(String text, FontHandler.Font font) {
			this.text = text;
			this.font = font;
		}
		
		public void setFont(FontHandler.Font font) {
			this.font = font;
		}
		
		public void setText(String text) {
			this.text = text;
		}
	}
	
	public static class Post extends FontRenderEvent {
		public Post(String text, FontHandler.Font font) {
			this.text = text;
			this.font = font;
		}
	}
	
	public String getText() {
		return this.text;
	}

	public FontHandler.Font getFont() {
		return this.font;
	}
}
