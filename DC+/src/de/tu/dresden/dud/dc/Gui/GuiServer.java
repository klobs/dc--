/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.Gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;

import de.tu.dresden.dud.dc.Connection;
import de.tu.dresden.dud.dc.KeyExchangeManager;
import de.tu.dresden.dud.dc.Server;
import de.tu.dresden.dud.dc.WorkCycle.WorkCycleManager;

import java.util.Observable;
import java.util.Observer;
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
public class GuiServer extends javax.swing.JPanel implements Observer{

	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	private static final long serialVersionUID = 3L;
	private DefaultComboBoxModel outputListModel;
	private JButton toggleServerButton;
	private AbstractAction actionToggleServer;
	private JPanel jPanel3;
	private JRadioButton jRadioButtonTickSleep2;
	private JRadioButton jRadioButtonTickSleep1;
	private JPanel jPanel2;
	private JRadioButton jRadioButtonTickSleep0;
	private JPanel jPanel1;
	private JCheckBox jCheckBoxVariableMessageLength;
	private JComboBox jComboBoxKeG;
	private JComboBox jComboBoxKeX;
	
	private Server assocServer = null;
	
	private ActionListener actionTickButton = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			assocServer.getWorkCycleManager().setTickPause(getTickTimer());
		}
	};
	
	private ButtonGroup buttonGroupTick = new ButtonGroup();
	
	public GuiServer() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)this);
			this.setLayout(thisLayout);
			this.setPreferredSize(new java.awt.Dimension(885, 525));
			{
				toggleServerButton = new JButton();
				toggleServerButton.setText("Start Server");
				toggleServerButton.setAction(getActionStartServer());
				toggleServerButton.addKeyListener(new KeyAdapter() {
					public void keyTyped(KeyEvent e) {
						// TODO Auto-generated method stub
						
					}
					public void keyReleased(KeyEvent e) {
						// TODO Auto-generated method stub
						
					}
					public void keyPressed(KeyEvent e) {
						// TODO Auto-generated method stub
						if (e.getKeyChar() == KeyEvent.VK_ENTER) getActionStartServer().actionPerformed(null);

					}
				});
				
				buttonGroupTick.add(getJRadioButtonTickSleep0());
				buttonGroupTick.add(getJRadioButtonTickSleep1());
				buttonGroupTick.add(getJRadioButtonTickSleep2());

				getJRadioButtonTickSleep0().addActionListener(actionTickButton);
				getJRadioButtonTickSleep1().addActionListener(actionTickButton);
				getJRadioButtonTickSleep2().addActionListener(actionTickButton);
				
				Component com[] = getJPanel2().getComponents();
				
				for (int a = 0; a < com.length; a++) {
					com[a].setEnabled(false);
				}

			}
				thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(getJPanel1(), GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(toggleServerButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(getJPanel3(), GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(285, Short.MAX_VALUE));
				thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(thisLayout.createParallelGroup()
					    .addGroup(thisLayout.createSequentialGroup()
					        .addComponent(toggleServerButton, GroupLayout.PREFERRED_SIZE, 861, GroupLayout.PREFERRED_SIZE)
					        .addGap(0, 0, Short.MAX_VALUE))
					    .addComponent(getJPanel3(), GroupLayout.Alignment.LEADING, 0, 861, Short.MAX_VALUE)
					    .addGroup(thisLayout.createSequentialGroup()
					        .addComponent(getJPanel1(), GroupLayout.PREFERRED_SIZE, 861, GroupLayout.PREFERRED_SIZE)
					        .addGap(0, 0, Short.MAX_VALUE)))
					.addContainerGap());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private AbstractAction getActionStartServer() {
		if(actionToggleServer == null) {
			actionToggleServer = new AbstractAction("Start Server", null) {
				private static final long serialVersionUID = 10L;

				public void actionPerformed(ActionEvent evt) {
					assocServer = new Server(Connection.DEFAULTPORT,
							getKeyGenerationMethod(), getKeyExchangeMethod(),
							getVariableMessageLengthsFeature());
					
					new Thread(assocServer, "Server").start();
					
					Component[] com = getJPanel1().getComponents();
					
					for (int a = 0; a < com.length; a++) {
					     com[a].setEnabled(false);
					}
					
					com = getJPanel2().getComponents();

					for (int a = 0; a < com.length; a++) {
					     com[a].setEnabled(true);
					}
					
					this.setEnabled(false);
				}
			};
		}
		return actionToggleServer;
	}

	@Override
	public void update(Observable o, Object arg) {
		outputListModel.addElement(arg);
	}
	
	private JComboBox getJComboBoxKeX() {
		if(jComboBoxKeX == null) {
			ComboBoxModel jComboBoxKeXModel = 
				new DefaultComboBoxModel(
						new String[] { "Fully automatic key exchange", "Manual key exchange"});
			jComboBoxKeX = new JComboBox();
			jComboBoxKeX.setModel(jComboBoxKeXModel);
		}
		return jComboBoxKeX;
	}
	
	private JComboBox getJComboBoxKeG() {
		if(jComboBoxKeG == null) {
			ComboBoxModel jComboBoxKeGModel = 
				new DefaultComboBoxModel(
						new String[] { "Null keys", "Normal DC-Keys", "Fail-Stop during work cycles", "Probabilistic-Fail-Stop during work cycle"});
			jComboBoxKeG = new JComboBox();
			jComboBoxKeG.setModel(jComboBoxKeGModel);
		}
		return jComboBoxKeG;
	}
	
	private JCheckBox getJCheckBoxVariableMessageLength() {
		if(jCheckBoxVariableMessageLength == null) {
			jCheckBoxVariableMessageLength = new JCheckBox();
			jCheckBoxVariableMessageLength.setText("Allow variable sized messages");
		}
		return jCheckBoxVariableMessageLength;
	}
	
	private JPanel getJPanel1() {
		if(jPanel1 == null) {
			jPanel1 = new JPanel();
			GroupLayout jPanel1Layout = new GroupLayout((JComponent)jPanel1);
			jPanel1.setLayout(jPanel1Layout);
			jPanel1.setBorder(BorderFactory.createTitledBorder("Adjust before you start"));
			jPanel1Layout.setHorizontalGroup(jPanel1Layout.createSequentialGroup()
				.addComponent(getJComboBoxKeX(), GroupLayout.PREFERRED_SIZE, 238, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(getJComboBoxKeG(), GroupLayout.PREFERRED_SIZE, 211, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(getJCheckBoxVariableMessageLength(), GroupLayout.PREFERRED_SIZE, 263, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(121, Short.MAX_VALUE));
			jPanel1Layout.setVerticalGroup(jPanel1Layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				    .addComponent(getJComboBoxKeX(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJComboBoxKeG(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				    .addComponent(getJCheckBoxVariableMessageLength(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
				.addContainerGap(196, 196));
		}
		return jPanel1;
	}
	
	private JRadioButton getJRadioButtonTickSleep0() {
		if(jRadioButtonTickSleep0 == null) {
			jRadioButtonTickSleep0 = new JRadioButton();
			jRadioButtonTickSleep0.setText("0 sec");
		}
		return jRadioButtonTickSleep0;
	}
	
	private JPanel getJPanel2() {
		if(jPanel2 == null) {
			jPanel2 = new JPanel();
			GroupLayout jPanel2Layout = new GroupLayout((JComponent)jPanel2);
			jPanel2.setLayout(jPanel2Layout);
			jPanel2.setBorder(BorderFactory.createTitledBorder(null, "Sleep during ticks", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
			jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup()
				.addComponent(getJRadioButtonTickSleep0(), GroupLayout.Alignment.LEADING, 0, 155, Short.MAX_VALUE)
				.addComponent(getJRadioButtonTickSleep1(), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)
				.addComponent(getJRadioButtonTickSleep2(), GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE));
			jPanel2Layout.setVerticalGroup(jPanel2Layout.createSequentialGroup()
				.addComponent(getJRadioButtonTickSleep0(), GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
				.addComponent(getJRadioButtonTickSleep1(), GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
				.addComponent(getJRadioButtonTickSleep2(), GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		}
		return jPanel2;
	}
	
	private JRadioButton getJRadioButtonTickSleep1() {
		if(jRadioButtonTickSleep1 == null) {
			jRadioButtonTickSleep1 = new JRadioButton();
			jRadioButtonTickSleep1.setText("1 sec");
			jRadioButtonTickSleep1.setSelected(true);
		}
		return jRadioButtonTickSleep1;
	}
	
	private JRadioButton getJRadioButtonTickSleep2() {
		if(jRadioButtonTickSleep2 == null) {
			jRadioButtonTickSleep2 = new JRadioButton();
			jRadioButtonTickSleep2.setText("20 sec");
		}
		return jRadioButtonTickSleep2;
	}
	
	private JPanel getJPanel3() {
		if(jPanel3 == null) {
			jPanel3 = new JPanel();
			GroupLayout jPanel3Layout = new GroupLayout((JComponent)jPanel3);
			jPanel3.setLayout(jPanel3Layout);
			jPanel3.setBorder(BorderFactory.createTitledBorder("Adjust while running"));
			jPanel3.setEnabled(true);
			jPanel3Layout.setVerticalGroup(jPanel3Layout.createSequentialGroup()
				.addGroup(jPanel3Layout.createSequentialGroup()
				    .addComponent(getJPanel2(), GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)));
			jPanel3Layout.setHorizontalGroup(jPanel3Layout.createSequentialGroup()
				.addGroup(jPanel3Layout.createSequentialGroup()
				    .addComponent(getJPanel2(), GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)));
		}
		return jPanel3;
	}
	
	private short getKeyGenerationMethod(){
		return (short) getJComboBoxKeG().getSelectedIndex();
	}
	
	private short getKeyExchangeMethod(){
		if (getJComboBoxKeX().getSelectedIndex() == 0)
			return KeyExchangeManager.KEX_FULLY_AUTOMATIC;
		if (getJComboBoxKeX().getSelectedIndex() == 1)
			return KeyExchangeManager.KEX_MANUAL;

		return KeyExchangeManager.KEX_MANUAL;
	}
	
	private int getTickTimer(){
		if (jRadioButtonTickSleep0.isSelected()) return 0;
		if (jRadioButtonTickSleep1.isSelected()) return 1000;
		if (jRadioButtonTickSleep2.isSelected()) return 20000;
		return 1000;
	}
	
	private short getVariableMessageLengthsFeature(){
		if (getJCheckBoxVariableMessageLength().isSelected())
			return WorkCycleManager.MESSAGE_LENGTHS_VARIABLE;
		else return WorkCycleManager.MESSAGE_LENGTHS_FIXED;
	}
}
