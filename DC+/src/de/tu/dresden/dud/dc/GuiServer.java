/**
 * This software is GPLv2. 
 * Take a look at the LICENSE file for more info.
 */
package de.tu.dresden.dud.dc;

import java.awt.Dimension;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComponent;

import javax.swing.JList;
import javax.swing.JScrollPane;

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
	
	private static final long serialVersionUID = 3L;
	private JList outputList;
	private JScrollPane jScrollPane1;
	private DefaultComboBoxModel outputListModel;
	
	public GuiServer() {
		super();
		initGUI();
	}
	
	private void initGUI() {
		try {
			GroupLayout thisLayout = new GroupLayout((JComponent)this);
			this.setLayout(thisLayout);
			setPreferredSize(new Dimension(400, 300));
			{
				jScrollPane1 = new JScrollPane();
				{
					outputListModel = 
						new DefaultComboBoxModel(
								new String[] { "" });
					outputList = new JList();
					jScrollPane1.setViewportView(outputList);
					GroupLayout outputListLayout = new GroupLayout((JComponent)outputList);
					outputList.setLayout(outputListLayout);
					outputList.setModel(outputListModel);
					outputListLayout.setVerticalGroup(outputListLayout.createSequentialGroup());
					outputListLayout.setHorizontalGroup(outputListLayout.createSequentialGroup());
				}
			}
				thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
					.addComponent(jScrollPane1, 0, 285, Short.MAX_VALUE)
					.addContainerGap(15, 15));
				thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup()
					.addContainerGap(26, 26)
					.addComponent(jScrollPane1, 0, 362, Short.MAX_VALUE)
					.addContainerGap(12, 12));
				thisLayout.setAutoCreateContainerGaps(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public JList getOutputList() {
		return outputList;
	}

	@Override
	public void update(Observable o, Object arg) {
		outputListModel.addElement(arg);
	}

}
