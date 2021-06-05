package com.ormoyo.ormoyoutil.abilities;

import java.lang.reflect.Method;

import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;

public class AbilityEventListener extends ASMEventHandler {
	private final Method method;
	public AbilityEventListener(Object target, Method method, ModContainer owner, boolean isGeneric) throws Exception {
		super(target, method, owner, isGeneric);
		this.method = method;
	}
	
	public Method getMethod() {
		return this.method;
	}
}
