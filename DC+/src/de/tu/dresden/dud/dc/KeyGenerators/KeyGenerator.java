package de.tu.dresden.dud.dc.KeyGenerators;

import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;

public abstract class KeyGenerator {

	public static final short METHOD_NULL						= 0;
	public static final short METHOD_DC							= 1;
	public static final short METHOD_DC_FAIL_STOP_WORK_CYCLE	= 2;

	protected WorkCycleManager 	assocWorkCycleManag = null;
	protected static short 		actualKeyGeneratingMethod;
	
	public KeyGenerator(WorkCycleManager wcm){
		assocWorkCycleManag = wcm;
	}
	
	public abstract byte[] calcKeys(final int length, final long wcn, final int rn);
	
	public short getKeyGenerationMethod(){
		return actualKeyGeneratingMethod;
	}
	
	public static KeyGenerator keyGeneratorFactory(int keyGeneratorMethod, WorkCycleManager wcm){
		switch(keyGeneratorMethod){
		case METHOD_DC:
			return new KeyGeneratorNormalDC(wcm);
		case METHOD_DC_FAIL_STOP_WORK_CYCLE:
			return new KeyGeneratorFailStopWorkCycle(wcm);
		}
		return null;
	}
	
}
