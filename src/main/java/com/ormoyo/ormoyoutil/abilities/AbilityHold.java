package com.ormoyo.ormoyoutil.abilities;

import com.ormoyo.ormoyoutil.OrmoyoUtil;
import com.ormoyo.ormoyoutil.network.MessageOnAbilityKeyPress;
import com.ormoyo.ormoyoutil.network.MessageOnAbilityKeyRelease;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbilityHold extends Ability {
	protected boolean isHolding;

	public AbilityHold(EntityPlayer owner) {
		super(owner);
	}
	
	public abstract boolean Hold();
	
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
					
					if(this.isHolding) {
						this.Hold();
					}
					
					if(this.cooldown >= 0 && this.getMaxCooldown() > 0 && this.startCooldown){
						this.cooldown++;
						this.cooldown %= this.getMaxCooldown();
						if(this.cooldown == 0) this.startCooldown = false;
					}
				}
			}else {
				if(this.isHolding) {
					this.Hold();
				}
				
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
		if(!this.startCooldown && !this.isHolding) {
			this.isHolding = true;
			if(this.owner.world.isRemote) {
				OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageOnAbilityKeyPress(this));
			}
		}
	}
	
	@Override
	public void onKeyRelease() {
		if(!this.startCooldown && this.isHolding) {
			this.isHolding = false;
			this.startCooldown = true;
			if(this.owner.world.isRemote) {
				OrmoyoUtil.NETWORK_WRAPPER.sendToServer(new MessageOnAbilityKeyRelease(this));
			}
		}
	}
	
	protected void setIsHolding(boolean isHolding) {
		this.isHolding = isHolding;
		AbilitySyncedValue.setValue(this, AbilityHold.class, "isHolding", isHolding);
	}
	
	protected boolean getIsHolding() {
		return this.isHolding;
	}
	
	@Override
	public int getKeybindCode() {
		return 0;
	}
}
