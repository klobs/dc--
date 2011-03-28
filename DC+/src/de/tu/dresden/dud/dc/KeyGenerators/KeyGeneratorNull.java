package de.tu.dresden.dud.dc.KeyGenerators;

import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;

public class KeyGeneratorNull extends KeyGenerator {

	public KeyGeneratorNull(WorkCycleManager wcm) {
		super(wcm);
		actualKeyGeneratingMethod = KeyGenerator.METHOD_NULL;
	}

	@Override
	public byte[] calcKeys(int length, long wcn, int rn) {
		return new byte[length];
	}

}
