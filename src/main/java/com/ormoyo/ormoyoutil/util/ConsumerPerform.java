package com.ormoyo.ormoyoutil.util;

import java.util.Iterator;
import java.util.function.Consumer;

public class ConsumerPerform<T> implements ITick {
	private Consumer<T> consumer;
	private T consumerValue;
	private int tick = 0;
	private int maxTick;
	private int performAmount;
	
	public ConsumerPerform(Consumer<T> consumer, T consumerValue, int tickAmount, int performAmount) {
		this.consumer = consumer;
		this.consumerValue = consumerValue;
		this.maxTick = tickAmount;
		this.performAmount = performAmount;
	}
	
	@Override
	public void onUpdate(Iterator<? extends ITick> iterator) {
		this.tick++;
		this.tick %= this.maxTick;
		if(this.tick == 0) {
			this.consumer.accept(this.consumerValue);
			this.performAmount--;
			if(this.performAmount <= 0) {
				iterator.remove();
			}
		}
	}
}