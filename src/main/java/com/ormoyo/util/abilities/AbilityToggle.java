package com.ormoyo.util.abilities;

import com.ormoyo.util.OrmoyoUtil;
import com.ormoyo.util.network.MessageOnAbilityKeyPress;

import net.minecraft.entity.player.EntityPlayer;

public abstract class AbilityToggle extends Ability {
	protected boolean isToggled;

	public AbilityToggle(EntityPlayer owner) {
		super(owner);
	}
	
	public abstract boolean Toggle();
	public abstract boolean UnToggle();
	
	@Override
	public void onUpdate() {
		if(this.owner != null) {
			if(this.owner.world.isRemote) {
				if(this.getKeybind() != null) {
					if(this.getKeybind().isKeyDown() && !this.hasBeenPressed) {
						this.onKeyPress();
						this.hasBeenPressed = true;
					}else if(!this.getKeybind().isKeyDown() && this.hasBeenPressed) {
						this.onKeyRelease();
						this.hasBeenPressed = false;
					}
					
					if(this.getMaxCooldown() <= 0) {
						this.startCooldown = false;
					}
					
					if(this.cooldown >= 0 && this.getMaxCooldown() > 0 && this.startCooldown){
						this.cooldown++;
						this.cooldown %= this.getMaxCooldown();
						if(this.cooldown == 0) this.startCooldown = false;
					}
				}
			}else {
				if(this.getMaxCooldown() == 0) {
					this.startCooldown = false;
				}
				
				if(this.cooldown >= 0 && this.getMaxCooldown() > 0 && this.startCooldown) {
					this.cooldown++;
					this.cooldown %= this.getMaxCooldown();
					if(this.cooldown == 0) this.startCooldown = false;
				}
			}
		}
	}
	
	@Override
	public void onKeyPress() {
		if(!this.isToggled) {
			if(this.Toggle()) {
				this.isToggled = true;
			}
			if(this.owner.world.isRemote) {
				OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageOnAbilityKeyPress(this));
			}
		}else {
			if(this.UnToggle()) {
				this.isToggled = false;
				this.startCooldown = true;
			}
			if(this.owner.world.isRemote) {
				OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageOnAbilityKeyPress(this));
			}
		}
	}
	
	public void setIsToggled(boolean isToggled) {
		this.isToggled = isToggled;
		AbilitySyncedValue.setValue(this, AbilityToggle.class, "isToggled", isToggled);
	}
	
	public boolean getIsToggled() {
		return this.isToggled;
	}
	
	@Override
	public int getKeybindCode() {
		return 0;
	}
}
