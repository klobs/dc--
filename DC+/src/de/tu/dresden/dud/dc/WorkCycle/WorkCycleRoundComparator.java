/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.WorkCycle;

import java.util.Comparator;

/**
 * This little piece of code is able to compare two {@link WorkCycleRound} with each other. 
 * This is needed to sort rounds.
 * 
 * It orders them by round number.
 * @author klobs
 *
 */
public class WorkCycleRoundComparator implements Comparator<WorkCycleRound> {

	@Override
	public int compare(WorkCycleRound r1, WorkCycleRound r2) {
		if (r1 == null || r2 == null)
			throw new NullPointerException();
		else if (r1.getRoundNumber() < r2.getRoundNumber())
			return -1;
		else if (r1.getRoundNumber() == r2.getRoundNumber())
			return 0;
		else
			return 1;
	}
}
