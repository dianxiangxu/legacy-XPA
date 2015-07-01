package org.sag.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.sag.combiningalgorithms.Call_Z3str;
import org.sag.combiningalgorithms.PolicyX;
import org.sag.combiningalgorithms.function;
import org.sag.combiningalgorithms.loadPolicy;
import org.sag.coverage.PolicySpreadSheetTestRecord;
import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.mutation.PolicyMutant;
import org.sag.mutation.PolicyMutator;
import org.sag.mutation.PolicySpreadSheetMutantSuite;
import org.umu.editor.XMLFileFilter;
import org.wso2.balana.AbstractTarget;
import org.wso2.balana.Policy;
import org.wso2.balana.Rule;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.xacml3.Target;
import org.sag.combiningalgorithms.MyAttr;

import java.util.ArrayList;

public class MutationPanel2 extends JPanel {
	/*
	 * Use code to present the test needs to be generated 1 -> policy target
	 * false + 1st rule true 2 -> policy target true + 1st rule true 3 -> policy
	 * target true + 1 rule true // rule coverage 4 -> policy target true + rule
	 * target false + rule condition true 5 -> policy target true + rule target
	 * true + rule condition false 6 -> if CA != FA, return; else make first
	 * permit rule true 7 -> if CA != FA, return; else make first deny rule
	 * true; 8 -> combining algorithm; 9 -> MC/DC Coverage
	 */

	//HashSet<Integer> test_table = new HashSet<Integer>();

	private static final long serialVersionUID = 1L;

	private TestPanel testPanel;
	private PolicySpreadSheetTestSuite testSuite;

	private XPA xpa;
	private PolicySpreadSheetMutantSuite mutantSuite;
	private Vector<Vector<Object>> data;

	private GeneralTablePanel tablePanel;
	
	private ArrayList<PolicySpreadSheetTestRecord> tests;
	private ArrayList<PolicySpreadSheetTestRecord> valid;

	public MutationPanel2(XPA xpa, TestPanel testPanel) {
		this.xpa = xpa;
		this.testPanel = testPanel;
		tests = new ArrayList<PolicySpreadSheetTestRecord>();
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
	}

	private JCheckBox boxPTT = new JCheckBox("Policy Target True (PTT)");
	private JCheckBox boxPTF = new JCheckBox("Policy Target False (PTF)");
	private JCheckBox boxCRC = new JCheckBox(
			"Change Rule CombiningAlgorithm (CRC)");
	private JCheckBox boxCRE = new JCheckBox("Flip Rule Effect (CRE)");
	private JCheckBox boxRER = new JCheckBox("Remove One Rule (RER)");
	private JCheckBox boxANR = new JCheckBox("Add a New Rule (ANR)");
	private JCheckBox boxRTT = new JCheckBox("Rule Target True (RTT)");
	private JCheckBox boxRTF = new JCheckBox("Rule Target False (RTF)");
	private JCheckBox boxRCT = new JCheckBox("Rule Condition True (RCT)");
	private JCheckBox boxRCF = new JCheckBox("Rule Condition False (RCF)");
	private JCheckBox boxFPR = new JCheckBox("First Permit Rules (FPR)");
	private JCheckBox boxFDR = new JCheckBox("First Deny Rules (FDR)");
	private JCheckBox boxRTR = new JCheckBox(
			"Rule Type Replaced (RTR) - Not implemented");
	//private JCheckBox boxRUF = new JCheckBox(
	//		"Remove Uniqueness Function (RUF) - Not Implemented"); //Turner Lehmbecker
	private JCheckBox boxFCF = new JCheckBox("Flip Comparison Function (FCF)");
	private JCheckBox boxANF = new JCheckBox("Add Not Function (ANF)");
	private JCheckBox boxRNF = new JCheckBox("Remove Not Function (RNF)");
	private JCheckBox boxRPTE = new JCheckBox(
			"Remove Parallel Target Element (RPTE)");
	private JCheckBox boxRPCE = new JCheckBox(
			"Remove Parallel Condition Element (RPCE) - Not implemented");

	private JCheckBox boxSelectAll = new JCheckBox("Select All"); // All 13
																	// types of
																	// mutation.
	private JCheckBox boxSelectEight = new JCheckBox("Select 8"); // 8 type
																	// (PTT,
																	// PTF, CRC,
																	// CRE, RTT,
																	// RTF, RCT,
																	// RCF)

	private JPanel createPanel() {
		setAllIndividualBoxes(true);

		boxSelectAll.setSelected(true);
		boxSelectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (boxSelectAll.isSelected())
					setAllIndividualBoxes(true);
				else
					setAllIndividualBoxes(false);
			}
		});

		boxSelectEight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (boxSelectEight.isSelected())
					setEightBoxes(true);
				else
					setEightBoxes(false);
			}
		});

		JPanel myPanel = new JPanel();
		myPanel.setLayout(new GridLayout(13, 2));
		myPanel.add(boxPTT);
		myPanel.add(boxPTF);
		myPanel.add(boxCRC);
		myPanel.add(boxCRE);
		myPanel.add(boxRER);
		myPanel.add(boxANR);
		myPanel.add(boxRTT);
		myPanel.add(boxRTF);
		myPanel.add(boxRCT);
		myPanel.add(boxRCF);
		myPanel.add(boxFPR);
		myPanel.add(boxFDR);
		myPanel.add(boxRTR);
		myPanel.add(boxFCF);
		myPanel.add(boxANF);
		myPanel.add(boxRNF);
		myPanel.add(boxRPTE);
		myPanel.add(boxRPCE);
		//myPanel.add(boxRUF);
		myPanel.add(boxSelectAll);
		myPanel.add(boxSelectEight);

		myPanel.setBorder(new TitledBorder(new EtchedBorder(), ""));

		return myPanel;
	}

	// set all individual checked boxes.
	private void setAllIndividualBoxes(boolean selected) {
		boxPTT.setSelected(selected);
		boxPTF.setSelected(selected);
		boxCRC.setSelected(selected);
		boxCRE.setSelected(selected);
		boxRER.setSelected(selected);
		// boxANR.setSelected(selected); //temporarily not considered.
		boxRTT.setSelected(selected);
		boxRTF.setSelected(selected);
		boxRCT.setSelected(selected);
		boxRCF.setSelected(selected);
		boxFPR.setSelected(selected);
		boxFDR.setSelected(selected);
		// boxRTR.setSelected(selected); // Not implemented
		// boxFCF.setSelected(selected); // not applicable in our examples.
		boxANF.setSelected(selected);
		boxRNF.setSelected(selected);
		boxRPTE.setSelected(selected);
		// boxRPCE.setSelected(selected); // Not implemented
		boxSelectAll.setSelected(selected);
		boxSelectEight.setSelected(false);
	}

	// set Eight type checked boxes. (PTT, PTF, CRC, CRE, RTT, RTF, RCT, RCF)
	private void setEightBoxes(boolean selected) {

		setAllIndividualBoxes(false);

		boxPTT.setSelected(selected);
		boxPTF.setSelected(selected);
		boxCRC.setSelected(selected);
		boxCRE.setSelected(selected);
		boxRTT.setSelected(selected);
		boxRTF.setSelected(selected);
		boxRCT.setSelected(selected);
		boxRCF.setSelected(selected);

		boxSelectEight.setSelected(selected);
	}

	public void setUpMutantPanel() {
		removeAll();
		setLayout(new BorderLayout());
		try {
			String[] columnNames = { "No", "Mutant Name", "Mutant File", "Bug Position", "Test Result" };
			data = mutantSuite.getMutantData();
			System.out.println(data.size() + " data size");
			System.out.println(data.toString());
			tablePanel = new GeneralTablePanel(data, columnNames, 5);
			tablePanel.setMinRows(30);
			JScrollPane scrollpane = new JScrollPane(tablePanel);
			add(scrollpane, BorderLayout.CENTER);
			xpa.setToMutantPane();
			xpa.updateMainTabbedPane();
		} catch (Exception e) {

		}
	}

	public void openMutants() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		if (xpa.getWorkingPolicyFile() != null)
			fileChooser.setCurrentDirectory(xpa.getWorkingPolicyFile()
					.getParentFile());
		fileChooser.setFileFilter(new XMLFileFilter("xls"));
		fileChooser.setDialogTitle("Open Mutants");
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File mutantSuiteFile = fileChooser.getSelectedFile();
			if (!mutantSuiteFile.toString().endsWith(".xls")) {
				JOptionPane.showMessageDialog(xpa,
						"The open File is not a test suite *.xls",
						"Error of Selection", JOptionPane.WARNING_MESSAGE);
			} else {
				try {
					mutantSuite = new PolicySpreadSheetMutantSuite(
							mutantSuiteFile.getAbsolutePath(),
							xpa.getWorkingPolicyFilePath());
					setUpMutantPanel();
				} catch (Exception e) {
				}
			}
		}
	}

	public void generateMutants() {
		if (!xpa.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(xpa, "There is no policy.");
			return;
		}
		
		loadPolicy lp = new loadPolicy();
		Policy policy = lp.getPolicy(xpa.getWorkingPolicyFilePath());
		System.out.println(xpa.getWorkingPolicyFilePath());
		PolicyX policyx = new PolicyX(policy);
		function f = new function();
		File out = new File(this.testPanel.getTestOutputDestination("_MutationTests"));
		if(!out.isDirectory() && !out.exists())
			out.mkdir();
		else
			f.deleteFile(out);
		List<Rule> rules = policyx.getRuleFromPolicy(policy);
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		String testPath = this.testPanel.getTestsuiteXLSfileName("_MutationTests");
		testPanel.setWorkingTestSuiteFileName(testPath);
		int result = JOptionPane.showConfirmDialog(xpa, createPanel(),
				"Please Select Mutation Methods", JOptionPane.OK_CANCEL_OPTION);
		String policyPath = xpa.getWorkingPolicyFilePath();
		if (result == JOptionPane.OK_OPTION) {
			try {
				PolicyMutator policyMutator = new PolicyMutator(
						xpa.getWorkingPolicyFilePath());
				if (boxPTT.isSelected()) {
					policyMutator.createPolicyTargetTrueMutants();
					//PolicySpreadSheetTestRecord PTT = policyx.generate_PolicyTargetTrue(getTestPanel());
					//tests.add(PTT);
					PolicySpreadSheetTestRecord ptt = policyx.generate_PolicyTargetTrue(getTestPanel());
					tests.add(ptt);
					String mutantPath = policyMutator.getMutantFileName("PTT1");
					String reqPath = testPanel.getTestOutputDestination("_MutationTests") + File.separator + ptt.getRequestFile();
					if(validation(policyPath, mutantPath, reqPath))
						valid.add(ptt);
				}
				if (boxPTF.isSelected()) {
					policyMutator.createPolicyTargetFalseMutants();
					PolicySpreadSheetTestRecord ptf = policyx.generate_PolicyTargetFalse(getTestPanel());
					tests.add(ptf);
					String mutantPath = policyMutator.getMutantFileName("PTF1");
					String reqPath = testPanel.getTestOutputDestination("_MutationTests") + File.separator + ptf.getRequestFile();
					if(validation(policyPath, mutantPath, reqPath))
						valid.add(ptf);
				}
				if (boxCRC.isSelected()) {
					policyMutator.createCombiningAlgorithmMutants();
					
				}
				if (boxCRE.isSelected()) {
					policyMutator.createRuleEffectFlippingMutants();
					
				}
				if (boxRER.isSelected()) {
					policyMutator.createRemoveRuleMutants();
					
				}

				if (boxANR.isSelected()) {
					policyMutator.createAddNewRuleMutants();
					
				}
				
				if (boxRTT.isSelected()) {
					policyMutator.createRuleTargetTrueMutants();
					ArrayList<PolicySpreadSheetTestRecord> rtt = policyx.generate_RuleTargetTrue(getTestPanel());
					determineValidTests(rtt, policyMutator, policyPath, "RTT");
				}
				
				if(boxRTF.isSelected())
				{
					policyMutator.createRuleTargetFalseMutants();
					ArrayList<PolicySpreadSheetTestRecord> rtf = policyx.generate_RuleTargetFalse(getTestPanel(), policyMutator);
					determineValidTests(rtf, policyMutator, policyPath, "RTF");
				}
				

				if (boxRCT.isSelected()) {
					policyMutator.createRuleConditionTrueMutants();
					ArrayList<PolicySpreadSheetTestRecord> rct = policyx.generate_RuleConditionTrue(getTestPanel());
					determineValidTests(rct, policyMutator, policyPath, "RCT");
				}
				
				if(boxRCF.isSelected())
				{
					policyMutator.createRuleConditionFalseMutants();
					ArrayList<PolicySpreadSheetTestRecord> rcf = policyx.generate_RuleConditionFalse(getTestPanel());
					determineValidTests(rcf, policyMutator, policyPath, "RCF");
				}

				if (boxFPR.isSelected()) {
					policyMutator.createFirstPermitRuleMutants();
					
				}							
				
				
				if (boxFDR.isSelected()) {
					policyMutator.createFirstDenyRuleMutants();
					
				}
				
				if (boxRTR.isSelected()) {
					policyMutator.createRuleTypeReplacedMutants();
					
				}
				
				if (boxFCF.isSelected()) {
					policyMutator.createFlipComparisonFunctionMutants();
					
				}
				
				if (boxANF.isSelected()) {
					policyMutator.createAddNotFunctionMutants();
				}
				
				if (boxRNF.isSelected()){
					policyMutator.createRemoveNotFunctionMutants();
					
				}
				
				if (boxRPTE.isSelected()) {
					policyMutator.createRemoveParallelTargetElementMutants();
					
				}
				
				if (boxRPCE.isSelected()) {
					policyMutator.createRemoveParallelConditionElementMutants();
					
				}
				mutantSuite = policyMutator.generateMutants(); // write to spreadsheet
				setUpMutantPanel();	
				System.out.printf("Generated tests: " + tests.size() + "\nValid tests: " + valid.size() + "\nPercent valid: %.2f\n", ((double)valid.size()/(double)tests.size()) * 100.00);


				try {
					testSuite = new PolicySpreadSheetTestSuite(
							valid,
							xpa.getWorkingPolicyFilePath());
					testSuite.writeToExcelFile(testPath);
					testPanel.setTestSuite(testSuite);
					testPanel.setUpTestPanel();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String getMutationTestingResultsFileName() {
		return new File(xpa.getWorkingTestSuiteFileName()).getParent()
				+ File.separator + "MutationTestingResults.xls";
	}

	public void testMutants() {
		if (mutantSuite == null) {
			JOptionPane.showMessageDialog(xpa, "There are no mutants.");
			return;
		}
		if (!xpa.hasTests()) {
			JOptionPane.showMessageDialog(xpa, "There are no tests.");
			return;
		}
		try {
			String outputFileName = getMutationTestingResultsFileName();
			// Time this.
			final long startTime = System.currentTimeMillis();

			mutantSuite.runAndWriteDetectionInfoToExcelFile(outputFileName,
					xpa.getWorkingTestSuite());

			final long endTime = System.currentTimeMillis();
			System.out.println("Mutants testing time: " + (endTime - startTime)
					/ 1000.00);

			mutantSuite.updateMutantTestResult(data);
			xpa.setToMutantPane();
			JOptionPane.showMessageDialog(xpa,
					"Mutation testing results are saved into file: \n"
							+ outputFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*public String getTestOutputDestination(String testMethod) {

		File file = xpa.getWorkingPolicyFile();
		String path = file.getParentFile().getAbsolutePath();
		String name = file.getName();
		name = name.substring(0, name.length() - 4);
		path = path + File.separator + "test_suites" + File.separator + name
				+ testMethod;
		return path;
	}*/
	
	public TestPanel getTestPanel(){return this.testPanel;}
	
	private boolean validation(String policyPath, String mutantPath, String requestPath)
	{
		String request = "";
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(requestPath));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while(line != null)
			{
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			request = sb.toString();
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println(request);
		loadPolicy lp = new loadPolicy();
		Policy original = lp.getPolicy(policyPath);
		Policy mutant = lp.getPolicy(mutantPath);
		//PolicyX policyx = new PolicyX(original);
		int pres = lp.PolicyEvaluate(original, request);
		int mres = lp.PolicyEvaluate(mutant, request);
		System.out.println("POLICY DECISION: " + pres + "\nMUTANT DECISION: " + mres);
		if(pres == mres)
			return false;
		else
			return true;
	}
	
	private void determineValidTests(ArrayList<PolicySpreadSheetTestRecord> records, PolicyMutator mutator, String policyPath, String type)
	{
		if(records.size() == 0)
			return;
		if(records.size() == 1)
		{
			tests.add(records.get(0));
			String req = testPanel.getTestOutputDestination("_MutationTests")
					+ File.separator + records.get(0).getRequestFile();
			int successes = 0;
			int matches = 0;
			for(PolicyMutant m : mutator.getMutantList())
			{
				String muType = m.getNumber().substring(m.getNumber().indexOf(' ') + 1, m.getNumber().length() - 1);
				if(muType.compareTo(type) == 0)
				{
					matches++;
					String mutantPath = m.getMutantFilePath();
					successes += validation(policyPath, mutantPath, req) ? 1 : 0;
				}
			}
			if(successes == matches)
				valid.add(records.get(0));
		}
		else
		{
			for(PolicySpreadSheetTestRecord record : records)
			{
				
				tests.add(record);
				String req = testPanel.getTestOutputDestination("_MutationTests")
						+ File.separator + record.getRequestFile();
				int mutantNum = records.indexOf(record) + 1;
				String mutantPath = mutator.getMutantFileName(type + mutantNum);
				if(validation(policyPath, mutantPath, req))
					valid.add(record);
				else
				{
					for(PolicyMutant m : mutator.getMutantList())
					{
						String mutantPath2 = m.getMutantFilePath();
						if(validation(policyPath, mutantPath2, req))//find at least one mutant that fails
						{
							valid.add(record);
							break;
						}
					}
				}
			}
		}
	}
}
