/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.Gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.Participant;
import de.tu.dresden.dud.dc.PreferenceSaver;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAccepted4Service;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageAdded;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageInfo;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageKThxBye;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageTick;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageWelcome2Service;
import de.tu.dresden.dud.dc.ManagementMessage.ManagementMessageWelcome2WorkCycle;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycle;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class GuiParticipant extends javax.swing.JPanel implements Observer {

	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private AbstractAction actionRefreshActiveParts;
	private AbstractAction actionRefreshPassiveParts;
	private AbstractAction actionJoinWorkCycle;
	private AbstractAction actionLeaveWorkCycle;
	private AbstractAction actionregisterAtService;
	private AbstractAction actionSend;
	private AbstractAction actionStart;
	private Connection assocConnection;
	private DefaultComboBoxModel modelListMessages; 
	private DefaultComboBoxModel modelListSums;
	private GuiParticipant observer;
	private JButton buttonJoinWorkCycle;
	private JButton buttonRegisterAtService;
	private JButton buttonSend;
	private JButton buttonStartClient;
	private JButton jButtonRefresPassiveParts;
	private JButton jButtonRefreshActiveParts;
	private JLabel jLabel1;
	private JLabel jLabel4;
	private JLabel jLabel5;
	private JLabel jLabel6;
	private JLabel jLabel7;
	private JLabel jLabel8;
	private JLabel labelLength;
	private JLabel labelParticipants;
	private JLabel labelTick;
	private JLabel labelVersion;
	private JList listMessages;
	private JList listSums;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JPanel jPanel3;
	private JPanel jPanel4;
	private JPanel jPanel5;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane3;
	private JScrollPane jScrollPane4;
	private JTextField inputTexField;
	private JTextField textMessage;
	private JTextField textName;
	private Participant assocParticipant;
	private JButton buttonExchangeKeyPassive;
	private AbstractAction actionQuitService;
	private AbstractAction actionExchangeKeyPassive;
	private JTable tableActiveParts;
	private JTable tablePassivParts;
	private AbstractAction actionSaveParticipant;
	private JButton buttonSaveParticipant;
	private JLabel labelWorkCycle;
	private JLabel jLabel9;
	private JScrollPane jScrollPane2;

	
	
	//// KeyListener
	private KeyListener listenerStart = new KeyListener() {
		public void keyTyped(KeyEvent e) {

		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER)
				getStartAction().actionPerformed(null);
		}

		public void keyReleased(KeyEvent e) {

		}
	};
	private KeyListener listenerJoinWorkCycle = new KeyListener() {
		public void keyTyped(KeyEvent e) {

		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER)
				getJoinWorkCycleAction().actionPerformed(null);
		}

		public void keyReleased(KeyEvent e) {

		}
	};
	private KeyListener listenerRegisterAtService = new KeyListener() {
		public void keyTyped(KeyEvent e) {

		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_ENTER)
				getRegisterAtServiceAction().actionPerformed(null);
		}

		public void keyReleased(KeyEvent e) {

		}
	};
	private KeyListener listenerMessage = new KeyListener() {
		public void keyTyped(KeyEvent e) {

		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyChar() == KeyEvent.VK_TAB) 	buttonJoinWorkCycle.requestFocus(true);
			if (e.getKeyChar() == KeyEvent.VK_ENTER) getSendAction().actionPerformed(null);
		}

		public void keyReleased(KeyEvent e) {

		}
	};

		
	public GuiParticipant() {
		super();
		initGUI();
		observer = this;
		this.setMinimumSize(new java.awt.Dimension(800, 499));
		this.setSize(800, 499);
	}
	
	private JButton getButtonSend() {
		if(buttonSend == null) {
			buttonSend = new JButton();
			buttonSend.setText("Payload");
			buttonSend.setAction(getSendAction());
		}
		return buttonSend;
	}
	
	public JTextField getInputTexField() {
		return inputTexField;
	}
	
	
	private AbstractAction getStartAction() {
		if(actionStart == null) {
			actionStart = new AbstractAction("Start Participant", null) {
				private static final long serialVersionUID = 8749319356187884901L;

				public void actionPerformed(ActionEvent evt) {
					
					String u = textName.getText();
	
					textName.setText(u);
										
					assocParticipant = new Participant(u);
					assocParticipant.addObserver(observer);
	
					assocConnection = assocParticipant.establishNewConnection("localhost", Connection.DEFAULTPORT);
					
					getTableActiveParts().setModel(
							new GuiParticipantActiveParticipantListModel(assocConnection.getAssociatedParticipantManager()));
					getTableActiveParts().getColumnModel().getColumn(0).setHeaderValue(new String("Key"));
					getTableActiveParts().getColumnModel().getColumn(1).setHeaderValue(new String("Name"));
					getTableActiveParts().getColumnModel().getColumn(2).setHeaderValue(new String("Fingerprint"));
					
					getTablePassivParts().setModel(
							new GuiParticipantPassiveParticipantListModel(assocConnection.getAssociatedParticipantManager()));
					getTablePassivParts().getColumnModel().getColumn(0).setHeaderValue(new String("Key"));
					getTablePassivParts().getColumnModel().getColumn(1).setHeaderValue(new String("Name"));
					getTablePassivParts().getColumnModel().getColumn(2).setHeaderValue(new String("Fingerprint"));
					
					getRegisterAtServiceAction().setEnabled(true);
					getSendAction().setEnabled(true);
					getActionSaveParticipant().setEnabled(true);
					this.setEnabled(false);
					
					buttonRegisterAtService.requestFocus();
				}
			
			};
		}
		return actionStart;
	}
	
	private AbstractAction getRegisterAtServiceAction() {
		if(actionregisterAtService == null) {
			actionregisterAtService = new AbstractAction("Register at the service", null) {
				private static final long serialVersionUID = 3416831018427667491L;

				public void actionPerformed(ActionEvent evt) {
					assocParticipant.registerAtService(assocConnection);
					this.setEnabled(false);
					actionJoinWorkCycle.setEnabled(true);
					buttonJoinWorkCycle.setFocusable(true);
					actionRefreshActiveParts.setEnabled(true);
					actionRefreshPassiveParts.setEnabled(true);
					getTextMessage().requestFocus();
				}
			
			};
		}
		return actionregisterAtService;
	}
	
	private AbstractAction getJoinWorkCycleAction() {
		if(actionJoinWorkCycle == null) {
			actionJoinWorkCycle = new AbstractAction("Join work cycle", null) {
				private static final long serialVersionUID = -9050384692608513171L;

				public void actionPerformed(ActionEvent evt) {
					assocParticipant.joinWorkCycle(assocConnection);
					actionSend.setEnabled(true);
					getLeaveWorkCycleAction().setEnabled(true);
					this.setEnabled(false);
					buttonJoinWorkCycle.setAction(getLeaveWorkCycleAction());
				}
			};
		}
		return actionJoinWorkCycle;
	}

	private AbstractAction getLeaveWorkCycleAction() {
		if(actionLeaveWorkCycle == null) {
			actionLeaveWorkCycle = new AbstractAction("Leave work cycle", null) {
				private static final long serialVersionUID = -9050384692628113171L;

				public void actionPerformed(ActionEvent evt) {
					assocParticipant.leaveWorkCycle(assocConnection);
					actionSend.setEnabled(false);
					getJoinWorkCycleAction().setEnabled(true);
					this.setEnabled(false);
					buttonJoinWorkCycle.setAction(getJoinWorkCycleAction());
				}
			};
		}
		return actionLeaveWorkCycle;
	}
	
	public JTextField getNameText() {
		if(textName == null) {
			textName = new JTextField();
			textName.addKeyListener(listenerStart);
		}
		return textName;
	}

	private JPanel getJPanel1() {
		if(jPanel1 == null) {
			jPanel1 = new JPanel();
			GroupLayout jPanel1Layout = new GroupLayout((JComponent)jPanel1);
			jPanel1.setLayout(jPanel1Layout);
			jPanel1.setEnabled(true);
			jPanel1.setBorder(BorderFactory.createTitledBorder("Step 1: Start up a new connection giving a username and connect to the server (localhost, default port)"));
			jPanel1.setToolTipText("Starts up a participant with given parameters and connects to the server (on localhost, default port)");
			{
				buttonStartClient = new JButton();
				buttonStartClient.setText("Start");
				buttonStartClient.setAction(getStartAction());
			}
			jPanel1.setFocusCycleRoot(true);
			jPanel1Layout.setHorizontalGroup(jPanel1Layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(buttonStartClient, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(getJLabel1(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(getNameText(), GroupLayout.PREFERRED_SIZE, 143, GroupLayout.PREFERRED_SIZE)
				.addGap(0, 329, Short.MAX_VALUE)
				.addComponent(getButtonSaveParticipant(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
			jPanel1Layout.setVerticalGroup(jPanel1Layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(buttonStartClient, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJLabel1(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getNameText(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getButtonSaveParticipant(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED));
		}
		return jPanel1;
	}
	
	private JPanel getJPanel2() {
		if(jPanel2 == null) {
			jPanel2 = new JPanel();
			GroupLayout jPanel2Layout = new GroupLayout((JComponent)jPanel2);
			jPanel2.setLayout(jPanel2Layout);
			jPanel2.setBorder(BorderFactory.createTitledBorder("Step 2: Register at the service and become a passive member of the DC Network"));
			{
				buttonRegisterAtService = new JButton();
				buttonRegisterAtService.setText("Register");
				buttonRegisterAtService.setAction(getRegisterAtServiceAction());
				getRegisterAtServiceAction().setEnabled(false);
				buttonRegisterAtService.addKeyListener(listenerRegisterAtService);
			}
			jPanel2Layout.setHorizontalGroup(jPanel2Layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(getJLabel4(), GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(getLabelVersion(), GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
				.addComponent(getJLabel5(), GroupLayout.PREFERRED_SIZE, 149, GroupLayout.PREFERRED_SIZE)
				.addComponent(getLabelLength(), GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(getJLabel6(), GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(getLabelParticipants(), GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
				.addGap(0, 139, Short.MAX_VALUE)
				.addComponent(buttonRegisterAtService, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
				.addContainerGap());
			jPanel2Layout.setVerticalGroup(jPanel2Layout.createSequentialGroup()
				.addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(buttonRegisterAtService, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJLabel4(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getLabelVersion(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJLabel5(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getLabelLength(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJLabel6(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getLabelParticipants(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE))
				.addContainerGap());
		}
		return jPanel2;
	}
	
	private JPanel getJPanel3() {
		if(jPanel3 == null) {
			jPanel3 = new JPanel();
			GroupLayout jPanel3Layout = new GroupLayout((JComponent)jPanel3);
			jPanel3.setLayout(jPanel3Layout);
			jPanel3.setBorder(BorderFactory.createTitledBorder("Step 3: Prepare payloads"));
			{
				inputTexField = new JTextField();
			}
			jPanel3.setFocusCycleRoot(true);
			jPanel3Layout.setHorizontalGroup(jPanel3Layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(jPanel3Layout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
				        .addComponent(getJLabel7(), GroupLayout.PREFERRED_SIZE, 265, GroupLayout.PREFERRED_SIZE)
				        .addGap(15))
				    .addComponent(getJScrollPane3(), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 278, GroupLayout.PREFERRED_SIZE)
				    .addGroup(GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
				        .addPreferredGap(getJLabel7(), getTextMessage(), LayoutStyle.ComponentPlacement.INDENT)
				        .addComponent(getTextMessage(), GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addComponent(getButtonSend(), GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)))
				.addGap(0, 74, Short.MAX_VALUE)
				.addComponent(inputTexField, GroupLayout.PREFERRED_SIZE, 0, GroupLayout.PREFERRED_SIZE));
			jPanel3Layout.setVerticalGroup(jPanel3Layout.createSequentialGroup()
				.addComponent(getJLabel7(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(getTextMessage(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getButtonSend(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(jPanel3Layout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
				        .addComponent(getJScrollPane3(), 0, 274, Short.MAX_VALUE)
				        .addGap(12))
				    .addGroup(GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
				        .addGap(0, 258, Short.MAX_VALUE)
				        .addComponent(inputTexField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))));
		}
		return jPanel3;
	}

	private JPanel getJPanel4() {
		if(jPanel4 == null) {
			jPanel4 = new JPanel();
			GroupLayout jPanel4Layout = new GroupLayout((JComponent)jPanel4);
			jPanel4.setLayout(jPanel4Layout);
			jPanel4.setBorder(BorderFactory.createTitledBorder("Step 4: Join work cycle and watch output:"));
			jPanel4.setMinimumSize(new java.awt.Dimension(300, 10));
			{
				buttonJoinWorkCycle = new JButton();
				buttonJoinWorkCycle.setText("Join work cycle");
				buttonJoinWorkCycle.setAction(getJoinWorkCycleAction());
				buttonJoinWorkCycle.addKeyListener(listenerJoinWorkCycle);
				getJoinWorkCycleAction().setEnabled(false);
			}
				jPanel4Layout.setHorizontalGroup(jPanel4Layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(jPanel4Layout.createParallelGroup()
				    .addGroup(GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
				        .addComponent(buttonJoinWorkCycle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addGroup(jPanel4Layout.createParallelGroup()
				            .addGroup(GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
				                .addComponent(getJLabel9(), GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
				                .addGap(55))
				            .addComponent(getJLabel8(), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				        .addGroup(jPanel4Layout.createParallelGroup()
				            .addComponent(getLabelWorkCycle(), GroupLayout.Alignment.LEADING, 0, 121, Short.MAX_VALUE)
				            .addComponent(getLabelTick(), GroupLayout.Alignment.LEADING, 0, 121, Short.MAX_VALUE)))
				    .addGroup(GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
				        .addPreferredGap(buttonJoinWorkCycle, getJScrollPane2(), LayoutStyle.ComponentPlacement.INDENT)
				        .addComponent(getJScrollPane2(), 0, 375, Short.MAX_VALUE)))
				.addContainerGap());
				jPanel4Layout.setVerticalGroup(jPanel4Layout.createSequentialGroup()
				.addGroup(jPanel4Layout.createParallelGroup()
				    .addGroup(jPanel4Layout.createSequentialGroup()
				        .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				            .addComponent(getLabelWorkCycle(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
				            .addComponent(getJLabel9(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				        .addGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				            .addComponent(getJLabel8(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				            .addComponent(getLabelTick(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)))
				    .addGroup(GroupLayout.Alignment.LEADING, jPanel4Layout.createSequentialGroup()
				        .addGap(12)
				        .addComponent(buttonJoinWorkCycle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(getJScrollPane2(), 0, 400, Short.MAX_VALUE)
				.addContainerGap(22, 22));
		}
		return jPanel4;
	}
	
	private JLabel getJLabel1() {
		if(jLabel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setText("Name:");
		}
		return jLabel1;
	}

	private JLabel getJLabel4() {
		if(jLabel4 == null) {
			jLabel4 = new JLabel();
			jLabel4.setText("Version:");
		}
		return jLabel4;
	}
	
	private JLabel getJLabel5() {
		if(jLabel5 == null) {
			jLabel5 = new JLabel();
			jLabel5.setText("Character Length [Byte]:");
		}
		return jLabel5;
	}
	
	private JLabel getJLabel6() {
		if(jLabel6 == null) {
			jLabel6 = new JLabel();
			jLabel6.setText("Participants:");
			jLabel6.setToolTipText("Those participants actually clicked the button to the left. The number doesn't indicate the participants in the work cycles.");
		}
		return jLabel6;
	}
	
	private JLabel getJLabel7() {
		if(jLabel7 == null) {
			jLabel7 = new JLabel();
			jLabel7.setText("Messages for the next work cycles:");
		}
		return jLabel7;
	}
	
	private JLabel getJLabel8() {
		if(jLabel8 == null) {
			jLabel8 = new JLabel();
			jLabel8.setText("Last TICK received:");
		}
		return jLabel8;
	}

	private JLabel getLabelLength() {
		if(labelLength == null) {
			labelLength = new JLabel();
			labelLength.setText("none");
		}
		return labelLength;
	}
	
	private JLabel getLabelParticipants() {
		if(labelParticipants == null) {
			labelParticipants = new JLabel();
			labelParticipants.setText("none");
			labelParticipants.setToolTipText("Those participants actually clicked the button to the left. The number doesn't indicate the participants in the work cycles.");
		}
		return labelParticipants;
	}
	
	private JLabel getLabelTick() {
		if(labelTick == null) {
			labelTick = new JLabel();
			labelTick.setText("none");
			labelTick.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return labelTick;
	}
	
	private JLabel getLabelVersion() {
		if(labelVersion == null) {
			labelVersion = new JLabel();
			labelVersion.setText("none");
		}
		return labelVersion;
	}

	private JScrollPane getJScrollPane3() {
		if(jScrollPane3 == null) {
			jScrollPane3 = new JScrollPane();
			jScrollPane3.setBorder(BorderFactory.createTitledBorder(null, "Message Queue:", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
			jScrollPane3.setViewportView(getListMessages());
		}
		return jScrollPane3;
	}
	
	private JList getListMessages() {
		if(listMessages == null) {
			modelListMessages = new DefaultComboBoxModel(
										new String[] { "Hier werden die Nachrichten fuer die kommenden Runden eingetragen." });
			listMessages = new JList();
			listMessages.setModel(modelListMessages);
			listMessages.setPreferredSize(new java.awt.Dimension(268, 50));
		}
		return listMessages;
	}
	
	private JList getListSums() {
		if(listSums == null) {
			modelListSums = new DefaultComboBoxModel(
						new String[] { "Hier könnte auch Ihre empfangene Summe stehen" });
			listSums = new JList();
			GroupLayout listSumsLayout = new GroupLayout((JComponent)listSums);
			listSums.setLayout(listSumsLayout);
			listSums.setModel(modelListSums);
			listSumsLayout.setVerticalGroup(listSumsLayout.createSequentialGroup());
			listSumsLayout.setHorizontalGroup(listSumsLayout.createSequentialGroup());
		}
		return listSums;
	}
	
	private AbstractAction getSendAction() {
		if(actionSend == null) {
			actionSend = new AbstractAction("Payload", null) {
				private static final long serialVersionUID = 8620905350292023918L;

				public void actionPerformed(ActionEvent evt) {
					if (textMessage.getText() != null)
						assocConnection.feedWorkCycleManager(textMessage.getText());
					else buttonStartClient.requestFocus();
					modelListMessages.addElement(textMessage.getText());
				}
			};
			actionSend.setEnabled(false);
		}
		return actionSend;
	}
	
	private JTextField getTextMessage() {
		if(textMessage == null) {
			textMessage = new JTextField();
			textMessage.addKeyListener(listenerMessage);
			textMessage.setFocusTraversalKeysEnabled(false);
		}
		return textMessage;
	}

	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)this);
			this.setLayout(thisLayout);
				thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
					.addComponent(getJPanel1(), GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
					.addComponent(getJPanel2(), GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE)
					.addGroup(thisLayout.createParallelGroup()
					    .addComponent(getJPanel3(), GroupLayout.Alignment.LEADING, 0, 370, Short.MAX_VALUE)
					    .addComponent(getJPanel4(), GroupLayout.Alignment.LEADING, 0, 370, Short.MAX_VALUE)
					    .addComponent(getJPanel5(), GroupLayout.Alignment.LEADING, 0, 370, Short.MAX_VALUE)));
				thisLayout.setHorizontalGroup(thisLayout.createParallelGroup()
					.addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
					    .addComponent(getJPanel3(), GroupLayout.PREFERRED_SIZE, 314, GroupLayout.PREFERRED_SIZE)
					    .addComponent(getJPanel4(), 0, 300, Short.MAX_VALUE)
					    .addComponent(getJPanel5(), 0, 176, Short.MAX_VALUE))
					.addComponent(getJPanel1(), GroupLayout.Alignment.LEADING, 0, 790, Short.MAX_VALUE)
					.addComponent(getJPanel2(), GroupLayout.Alignment.LEADING, 0, 790, Short.MAX_VALUE));
			this.setPreferredSize(new java.awt.Dimension(1036, 628));
			this.setBackground(new java.awt.Color(233,233,233));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void update(Observable o, Object arg){
	
		if (o instanceof Participant){
		  
			// WELCOME2SERVICE
			// Character length
			// Version
			// Participants.
	    	if ( arg instanceof ManagementMessageWelcome2Service ){
	    		
	    		ManagementMessageWelcome2Service m = (ManagementMessageWelcome2Service) arg;
	    		labelLength.setText( String.valueOf(m.getCharLength()));
	    		labelVersion.setText(String.valueOf(m.getVersion()));
	    		labelParticipants.setText(String.valueOf(m.getParticipantsCount()));
	    		
	    		getStartAction().setEnabled(false);
	    		getActionQuitService().setEnabled(true);
	    		
	    		buttonStartClient.setAction(getActionQuitService());
	    	}
	    	
	    	// ACCEPTED4SERVICE
	    	else if (arg instanceof ManagementMessageAccepted4Service){
	    		
	    	}

	    	//WELCOME2WORKCYCLE
	    	else if (arg instanceof ManagementMessageWelcome2WorkCycle){
	    		ManagementMessageWelcome2WorkCycle m = (ManagementMessageWelcome2WorkCycle) arg;
	    		labelWorkCycle.setText(String.valueOf(m.getWorkCycle()));
	    		
	    	} 	
	    	
	    	//K THX BYE
	    	else if (arg instanceof ManagementMessageKThxBye){
	    		ManagementMessageKThxBye m = (ManagementMessageKThxBye) arg;
	    		
	    		if (m.getQuitOK() == ManagementMessageKThxBye.QUITOK_ALL_OK){
	    			getStartAction().setEnabled(true);
	    			getActionQuitService().setEnabled(false);
	    			
	    			buttonStartClient.setAction(getStartAction());
	    			getJoinWorkCycleAction().setEnabled(false);
	    			buttonJoinWorkCycle.setAction(getJoinWorkCycleAction());
	    		}
	    	}
	    	// TICK
	    	else if (arg instanceof ManagementMessageTick){
	    		ManagementMessageTick m = (ManagementMessageTick) arg;
	    		labelTick.setText( String.valueOf(m.getWorkCycleNumber()));
	    	}
	    	
	    	// ADDED
	    	else if (arg instanceof ManagementMessageAdded){
	    		ManagementMessageAdded m = (ManagementMessageAdded) arg;
	    		if(assocConnection.getAssociatedWorkCycleManager() != null){
	    		if (assocConnection.getAssociatedWorkCycleManager().getCurrentWorkCycle().getCurrentPhase() == WorkCycle.WC_RESERVATION)
	    		modelListSums.addElement("R:" + m.getWorkCycleNumber() + ", SR:"+ m.getRoundNumber() + "(reservation): "+ Arrays.toString(m.getPayload()) );
	    		else /* if (assocConnection.getAssociatedWorkCycleManager().getCurrentWorkCycle().getCurrentPhase() == WorkCycle.WC_SENDING)  TODO remove this with a better save model?*/
		    		modelListSums.addElement("R:" + m.getWorkCycleNumber() + ", SR:"+ m.getRoundNumber() + ": " + new String(m.getPayload()) +"/"+ Arrays.toString(m.getPayload()));
	    		}
	    	}
	    	
	    	else if (arg instanceof ManagementMessageInfo){
	    		tablePassivParts.updateUI();
	    	}
		}
	}

	private JPanel getJPanel5() {
		if(jPanel5 == null) {
			jPanel5 = new JPanel();
			GroupLayout jPanel5Layout = new GroupLayout((JComponent)jPanel5);
			jPanel5.setLayout(jPanel5Layout);
			jPanel5.setBorder(BorderFactory.createTitledBorder("Info Requests"));
			jPanel5.setMaximumSize(new java.awt.Dimension(300, 32767));
			getActionRefreshPassiveParts().setEnabled(false);
			getActionRefreshActiveParts().setEnabled(false);
			jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup()
				.addComponent(getButtonExchangeKeyPassive(), GroupLayout.Alignment.LEADING, 0, 169, Short.MAX_VALUE)
				.addGroup(GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
				    .addComponent(getJScrollPane4(), 0, 163, Short.MAX_VALUE)
				    .addGap(6))
				.addComponent(getJButtonRefreshActiveParts(), GroupLayout.Alignment.LEADING, 0, 169, Short.MAX_VALUE)
				.addComponent(getJButtonRefresPassiveParts(), GroupLayout.Alignment.LEADING, 0, 169, Short.MAX_VALUE)
				.addComponent(getJScrollPane1(), GroupLayout.Alignment.LEADING, 0, 169, Short.MAX_VALUE));
			jPanel5Layout.setVerticalGroup(jPanel5Layout.createSequentialGroup()
				.addComponent(getJScrollPane1(), 0, 95, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(getJButtonRefresPassiveParts(), GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(getButtonExchangeKeyPassive(), GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(getJScrollPane4(), 0, 129, Short.MAX_VALUE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(getJButtonRefreshActiveParts(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		}
		return jPanel5;
	}
	
	private JScrollPane getJScrollPane1() {
		if(jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getTablePassivParts());
		}
		return jScrollPane1;
	}
	
	private JButton getJButtonRefresPassiveParts() {
		if(jButtonRefresPassiveParts == null) {
			jButtonRefresPassiveParts = new JButton();
			jButtonRefresPassiveParts.setText("Refresh Parssive Participants");
			jButtonRefresPassiveParts.setAction(getActionRefreshPassiveParts());
		}
		return jButtonRefresPassiveParts;
	}
	
	private AbstractAction getActionRefreshPassiveParts() {
		if(actionRefreshPassiveParts == null) {
			actionRefreshPassiveParts = new AbstractAction("Refresh Passive Participants", null) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 8285815505883049643L;

				public void actionPerformed(ActionEvent evt) {
					assocConnection.requestPassiveConnections();
				}
			};
		}
		return actionRefreshPassiveParts;
	}

	private JScrollPane getJScrollPane4() {
		if(jScrollPane4 == null) {
			jScrollPane4 = new JScrollPane();
			jScrollPane4.setViewportView(getTableActiveParts());
		}
		return jScrollPane4;
	}
	
	private JButton getJButtonRefreshActiveParts() {
		if(jButtonRefreshActiveParts == null) {
			jButtonRefreshActiveParts = new JButton();
			jButtonRefreshActiveParts.setText("Refresh Active Participants");
			jButtonRefreshActiveParts.setAction(getActionRefreshActiveParts());
		}
		return jButtonRefreshActiveParts;
	}
	
	private AbstractAction getActionRefreshActiveParts() {
		if(actionRefreshActiveParts == null) {
			actionRefreshActiveParts = new AbstractAction("Refresh Active Participants", null) {
				/**
				 * 
				 */
				private static final long serialVersionUID = -4440500934893505017L;

				public void actionPerformed(ActionEvent evt) {
					assocConnection.requestActiveConnections();					
				}
			};
		}
		return actionRefreshActiveParts;
	}

	private JScrollPane getJScrollPane2() {
		if(jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getListSums());
		}
		return jScrollPane2;
	}
	
	private JLabel getJLabel9() {
		if(jLabel9 == null) {
			jLabel9 = new JLabel();
			jLabel9.setText("Join work cycle:");
		}
		return jLabel9;
	}
	
	private JLabel getLabelWorkCycle() {
		if(labelWorkCycle == null) {
			labelWorkCycle = new JLabel();
			labelWorkCycle.setText("none");
			labelWorkCycle.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return labelWorkCycle;
	}
	
	private JButton getButtonSaveParticipant() {
		if(buttonSaveParticipant == null) {
			buttonSaveParticipant = new JButton();
			buttonSaveParticipant.setText("Save participant");
			buttonSaveParticipant.setAction(getActionSaveParticipant());
		}
		return buttonSaveParticipant;
	}
	
	private AbstractAction getActionSaveParticipant() {
		if(actionSaveParticipant == null) {
			actionSaveParticipant = new AbstractAction("Save participant", null) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 3786364892526109599L;

				public void actionPerformed(ActionEvent evt) {
					if (PreferenceSaver.getInstance().saveParticipant(assocParticipant)) this.setEnabled(false);
				}
			};
			actionSaveParticipant.setEnabled(false);
		}
		return actionSaveParticipant;
	}
	
	private JTable getTablePassivParts() {
		if (tablePassivParts == null) {
			tablePassivParts = new JTable();
			tablePassivParts
					.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return tablePassivParts;
	}
	
	private JTable getTableActiveParts() {
		if (tableActiveParts == null) {
			tableActiveParts = new JTable();
			tableActiveParts
					.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return tableActiveParts;
	}
	
	private JButton getButtonExchangeKeyPassive() {
		if(buttonExchangeKeyPassive == null) {
			buttonExchangeKeyPassive = new JButton();
			buttonExchangeKeyPassive.setText("Exchange Key");
			buttonExchangeKeyPassive.setAction(getActionExchangeKeyPassive());
		}
		return buttonExchangeKeyPassive;
	}
	
	private AbstractAction getActionExchangeKeyPassive() {
		if(actionExchangeKeyPassive == null) {
			actionExchangeKeyPassive = new AbstractAction("Exchange Key", null) {
				private static final long serialVersionUID = -2615192063494549225L;

				public void actionPerformed(ActionEvent evt) {
					
					int i = getTablePassivParts().getSelectedRow();
					if (i>=0)
						assocParticipant
							.exchangeKeyWith(((GuiParticipantPassiveParticipantListModel) getTablePassivParts()
									.getModel()).getIDForRow(i), assocConnection);
					
				}
			};
		}
		return actionExchangeKeyPassive;
	}
	
	private AbstractAction getActionQuitService() {
		if(actionQuitService == null) {
			actionQuitService = new AbstractAction("Quit", null) {
				private static final long serialVersionUID = 4619644940971896342L;

				public void actionPerformed(ActionEvent evt) {
					assocParticipant.quitService(assocConnection);
				}
			};
			actionQuitService.setEnabled(false);
		}
		return actionQuitService;
	}

}
