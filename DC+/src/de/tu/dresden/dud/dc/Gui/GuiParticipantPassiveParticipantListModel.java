/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.Gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import de.tu.dresden.dud.dc.ParticipantManager;

public class GuiParticipantPassiveParticipantListModel extends
		AbstractTableModel implements Observer{

	private static final long serialVersionUID = 1425395471369338914L;
	
	private ParticipantManager participantManager; 
	
	public GuiParticipantPassiveParticipantListModel(ParticipantManager p) {
		participantManager = p;
		participantManager.addObserver(this);
	}
	

	@Override
	public int getColumnCount() {
		return 3;
	}


	public String getIDForRow(int i){
		if (i >= 0 && i < participantManager.getPassivePartMgmtInfo().size())
			return participantManager.getPassivePartMgmtInfo().get(i).getParticipant().getId();
		return new String("");
	}
	
	@Override
	public int getRowCount() {
		return participantManager.getPassivePartMgmtInfo().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex){
		case 0:
			return participantManager.getPassivePartMgmtInfo().get(rowIndex).hasExchangedKey();
		case 1:
			return participantManager.getPassivePartMgmtInfo().get(rowIndex).getParticipant().getUsername();
		case 2:
			return participantManager.getPassivePartMgmtInfo().get(rowIndex).getParticipant().getId();
		default:
			return null;
		}
	}

	
	
	@Override
	public void update(Observable o, Object arg) {
		switch (((Integer) arg).intValue()){
		case ParticipantManager.PARTMNG_INTERVAL_ADDED_PASSIVE:
			if (getRowCount() != 0)
				fireTableRowsInserted(0, getRowCount());
		case ParticipantManager.PARTMNG_INTERVAL_CHANGED_PASSIVE:
			if (getRowCount() != 0)
				fireTableRowsUpdated(0, getRowCount());
			break;
		}
	}

	
}
