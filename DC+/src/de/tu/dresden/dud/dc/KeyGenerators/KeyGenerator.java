package de.tu.dresden.dud.dc.KeyGenerators;

import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;

public abstract class KeyGenerator {

	public static final short METHOD_NULL						= 0;
	public static final short METHOD_DC							= 1;
	public static final short METHOD_DC_FAIL_STOP_WORK_CYCLE	= 2;

}
