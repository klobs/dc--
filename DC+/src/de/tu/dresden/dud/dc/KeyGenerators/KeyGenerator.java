package de.tu.dresden.dud.dc.KeyGenerators;

import org.apache.log4j.Logger;

import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;

public abstract class KeyGenerator {

	// Logging
	private static Logger log = Logger.getLogger(KeyGenerator.class);
	
	public static final short KGMETHOD_NULL						= 0;
	public static final short KGMETHOD_DC						= 1;
	public static final short KGMETHOD_DC_FAIL_STOP_WORK_CYCLE	= 2;
	public static final short KGMETHOD_PROBAB_FAIL_STOP			= 3;

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
		case KGMETHOD_DC:
			return new KeyGeneratorNormalDC(wcm);
		case KGMETHOD_DC_FAIL_STOP_WORK_CYCLE:
			return new KeyGeneratorFailStopWorkCycle(wcm);
		}
		return null;
	}
	
}
