package com.ormoyo.ormoyoutil.abilities;

import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.network.MessageOnAbilityKeyPress;

import net.minecraft.entity.player.EntityPlayer;

public abstract class AbilityAction extends Ability {
	
	public AbilityAction(EntityPlayer owner) {
		super(owner);
	}

	public abstract boolean Action();
	
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
						if(this.cooldown == 0) startCooldown = false;
					}
				}
			}else {
				if(this.getMaxCooldown() == 0) {
					this.startCooldown = false;
				}
				
				if(this.cooldown >= 0 && this.getMaxCooldown() > 0 && this.startCooldown) {
					this.cooldown++;
					this.cooldown %= this.getMaxCooldown();
					if(this.cooldown == 0) startCooldown = false;
				}
			}
		}
	}
	
	@Override
	public void onKeyPress() {
		if(!this.startCooldown) {
			if(this.Action()) {
				this.startCooldown = true;
			}
			if(this.owner.world.isRemote) {
				OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageOnAbilityKeyPress(this));
			}
		}
	}
	
	@Override
	public int getKeybindCode() {
		return 0;
	}
}
