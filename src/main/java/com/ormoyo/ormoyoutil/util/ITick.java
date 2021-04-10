package com.ormoyo.ormoyoutil.util;

import java.util.Iterator;

public interface ITick {
	void onUpdate(Iterator<? extends ITick> iterator);
}
