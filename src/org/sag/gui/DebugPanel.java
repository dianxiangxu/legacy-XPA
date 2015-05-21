package org.sag.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class DebugPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private XPA xpa;
	private Vector<Vector<Object>> data;
	
	private GeneralTablePanel tablePanel;
	
	public DebugPanel(XPA xpa) {
		this.xpa = xpa;	
	}

	public void setUpMutantPanel(){
		removeAll();
		setLayout(new BorderLayout());
		// TODO
		try {
			String[] columnNames = { "TODO" };
			//data = xpa.mutantSuite.getMutantData();
			tablePanel = new GeneralTablePanel(data, columnNames, 5);
			tablePanel.setMinRows(30);
			JScrollPane scrollpane = new JScrollPane(tablePanel);
			add(scrollpane, BorderLayout.CENTER);
			xpa.setToMutantPane();
			xpa.updateMainTabbedPane();
		} catch (Exception e) {

		}
	}
	
}
