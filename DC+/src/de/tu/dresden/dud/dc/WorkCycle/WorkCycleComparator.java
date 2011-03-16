/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.WorkCycle;

import java.util.Comparator;

/**
 * This little piece of code is able to compare two work cycles with each other. 
 * This is needed to sort work cycles.
 * 
 * It orders them by work cycle number.
 * @author klobs
 *
 */
public class WorkCycleComparator implements Comparator<WorkCycle> {

	@Override
	public int compare(WorkCycle r1, WorkCycle r2) {
		if (r1 == null || r2 == null)
			throw new NullPointerException();
		else if (r1.getWorkCycleNumber() < r2.getWorkCycleNumber())
			return -1;
		else if (r1.getWorkCycleNumber() == r2.getWorkCycleNumber())
			return 0;
		else
			return 1;
	}
}
