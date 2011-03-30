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
	
	public static boolean isAsynchronous(int method){
		return !isSynchronous(method);
	}
	
	/**
	 * Tell whether key generation method is synchronous
	 * @param method id of key generation method
	 * @return 
	 */
	public static boolean isSynchronous(int method){
		if ( method == KGMETHOD_DC_FAIL_STOP_WORK_CYCLE ||
			 method == KGMETHOD_PROBAB_FAIL_STOP )
			return true;
		return false;
	}
	
	public static KeyGenerator keyGeneratorFactory(int keyGeneratorMethod, WorkCycleManager wcm){
		switch(keyGeneratorMethod){
		case KGMETHOD_NULL:
			return new KeyGeneratorNull(wcm);
		case KGMETHOD_DC:
			return new KeyGeneratorNormalDC(wcm);
		case KGMETHOD_DC_FAIL_STOP_WORK_CYCLE:
			return new KeyGeneratorFailStopWorkCycle(wcm);
		case KGMETHOD_PROBAB_FAIL_STOP:
			return new KeyGeneratorProbabFailStop(wcm);
		}
		log.warn("No suitable key generator method found. Not using keys at all!!!");
		return new KeyGeneratorNull(wcm);
	}
	
}
