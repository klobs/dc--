/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc.Gui;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;

import javax.swing.WindowConstants;

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
public class Gui extends javax.swing.JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton newParticipant;
	private AbstractAction actionNewParticipant;
	private AbstractAction actionNewParticipantFromConfig;
	private JButton newParticipantFromConfig;
	private JMenuItem jMenuItemParticipant;
	private JMenu jMenuFile;
	private JMenuBar File;
	private JTabbedPane mainPane;

	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	/**
	* Auto-generated main method to display this JFrame
	*/
	
	public Gui() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)getContentPane());
			getContentPane().setLayout(thisLayout);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			this.setMinimumSize(new java.awt.Dimension(800, 600));
			{
				File = new JMenuBar();
				setJMenuBar(File);
				File.add(getJMenu1());
			}
			{
				newParticipant = new JButton();
				newParticipant.setText("New Participant");
				newParticipant.setAction(getActionNewParticipant());
				newParticipant.addKeyListener(new KeyListener() {
					
					@Override
					public void keyTyped(KeyEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void keyReleased(KeyEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void keyPressed(KeyEvent e) {
						// TODO Auto-generated method stub
						if (e.getKeyChar() == KeyEvent.VK_ENTER) getActionNewParticipant().actionPerformed(null);
					}
				});
			}
			{
				mainPane = new JTabbedPane();
				mainPane.setFocusable(true);
				GuiServer g = new GuiServer();
				
				getMainPane().add("Server / Log output" , g);

			}
				thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
					.addGroup(thisLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					    .addComponent(getNewParticipantFromConfig(), GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					    .addComponent(newParticipant, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					.addComponent(mainPane, 0, 497, Short.MAX_VALUE)
					.addContainerGap());
				thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(thisLayout.createParallelGroup()
					    .addGroup(GroupLayout.Alignment.LEADING, thisLayout.createSequentialGroup()
					        .addComponent(newParticipant, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
					        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
					        .addComponent(getNewParticipantFromConfig(), GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					        .addGap(0, 693, Short.MAX_VALUE))
					    .addComponent(mainPane, GroupLayout.Alignment.LEADING, 0, 908, Short.MAX_VALUE))
					.addContainerGap());
			pack();
			this.setSize(940, 600);
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}
	
	public JTabbedPane getMainPane() {
		return mainPane;
	}
	
	public JButton getNewParticipant() {
		return newParticipant;
	}

	private AbstractAction getActionNewParticipant() {
		if(actionNewParticipant == null) {
			actionNewParticipant = new AbstractAction("New participant", null) {
				private static final long serialVersionUID = 11L;
				
				public void actionPerformed(ActionEvent evt) {
					GuiParticipant p = new GuiParticipant();
					getMainPane().add("Participant " + String.valueOf(getMainPane().getComponentCount()), p);
					getMainPane().setSelectedIndex(getMainPane().getComponentCount() - 1);
					p.getNameText().requestFocus();
				}
			};
		}
		return actionNewParticipant;
	}
	
	private JMenu getJMenu1() {
		if(jMenuFile == null) {
			jMenuFile = new JMenu();
			jMenuFile.setText("File");
			jMenuFile.add(getJMenuItemParticipant());
		}
		return jMenuFile;
	}
	
	private JMenuItem getJMenuItemParticipant() {
		if(jMenuItemParticipant == null) {
			jMenuItemParticipant = new JMenuItem();
			jMenuItemParticipant.setText("New participant");
			jMenuItemParticipant.setAction(getActionNewParticipant());
			jMenuItemParticipant.setMnemonic('C');
			jMenuItemParticipant.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.META_MASK));
		}
		return jMenuItemParticipant;
	}
	
	private JButton getNewParticipantFromConfig() {
		if(newParticipantFromConfig == null) {
			newParticipantFromConfig = new JButton();
			newParticipantFromConfig.setText("jButton1");
			newParticipantFromConfig.setAction(getActionNewParticipantFromConfig());
		}
		return newParticipantFromConfig;
	}
	
	private AbstractAction getActionNewParticipantFromConfig() {
		if(actionNewParticipantFromConfig == null) {
			actionNewParticipantFromConfig = new AbstractAction("New participant from config", null) {
				private static final long serialVersionUID = -1790265366419107488L;

				public void actionPerformed(ActionEvent evt) {
				}
			};
			actionNewParticipantFromConfig.setEnabled(false);
		}
		return actionNewParticipantFromConfig;
	}

}
