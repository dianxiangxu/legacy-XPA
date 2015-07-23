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
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
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

	// HashSet<Integer> test_table = new HashSet<Integer>();

	private static final long serialVersionUID = 1L;

	private TestPanel testPanel;
	private PolicySpreadSheetTestSuite testSuite;

	private XPA xpa;
	private PolicySpreadSheetMutantSuite mutantSuite;
	private Vector<Vector<Object>> data;

	private GeneralTablePanel tablePanel;

	private ArrayList<PolicySpreadSheetTestRecord> tests;
	private ArrayList<PolicySpreadSheetTestRecord> valid;
	private ArrayList<PolicySpreadSheetTestRecord> optimal;
	private ArrayList<PolicySpreadSheetTestRecord> final_optimal;

	private PolicySpreadSheetTestRecord[] mergeArray;

	public MutationPanel2(XPA xpa, TestPanel testPanel) {
		this.xpa = xpa;
		this.testPanel = testPanel;
		tests = new ArrayList<PolicySpreadSheetTestRecord>();
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		optimal = new ArrayList<PolicySpreadSheetTestRecord>();
		final_optimal = new ArrayList<PolicySpreadSheetTestRecord>();
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
	// private JCheckBox boxRUF = new JCheckBox(
	// "Remove Uniqueness Function (RUF) - Not Implemented"); //Turner
	// Lehmbecker
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
	private JCheckBox boxOptimize = new JCheckBox(
			"Do optimization - will significantly increase generation time");
	private JCheckBox boxOTF = new JCheckBox(
			"On the fly optimization - faster than normal optimization but still slow");
	private JCheckBox boxOPT2 = new JCheckBox(
			"Optimize v2 - potentially faster");
	private JCheckBox boxOPT3 = new JCheckBox(
			"Optimize v3 - Fastest, but not the most accurate");
	private int validTests = 0;

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
		// myPanel.add(boxRUF);
		myPanel.add(boxSelectAll);
		myPanel.add(boxSelectEight);
		myPanel.add(boxOptimize);
		myPanel.add(boxOPT2);
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
			String[] columnNames = { "No", "Mutant Name", "Mutant File",
					"Bug Position", "Test Result" };
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
		File out = new File(
				this.testPanel.getTestOutputDestination("_MutationTests"));
		if (!out.isDirectory() && !out.exists())
			out.mkdir();
		else
			f.deleteFile(out);
		List<Rule> rules = policyx.getRuleFromPolicy(policy);
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		String testPath = this.testPanel
				.getTestsuiteXLSfileName("_MutationTests");
		testPanel.setWorkingTestSuiteFileName(testPath);
		int result = JOptionPane.showConfirmDialog(xpa, createPanel(),
				"Please Select Mutation Methods", JOptionPane.OK_CANCEL_OPTION);
		String policyPath = xpa.getWorkingPolicyFilePath();
		int mlistIndex = 0;
		int tlistIndex = 0;

		ArrayList<PolicyMutant> testable = new ArrayList<PolicyMutant>();

		long totalOptimizationTime = 0;
		long totalGenerationTime = 0;

		if (boxOPT2.isSelected() && boxOptimize.isSelected())
			boxOptimize.setSelected(false);
		if (result == JOptionPane.OK_OPTION) {
			try {
				PolicyMutator policyMutator = new PolicyMutator(
						xpa.getWorkingPolicyFilePath());
				if (boxPTT.isSelected()) {
					policyMutator.createPolicyTargetTrueMutants();
					// PolicySpreadSheetTestRecord PTT =
					// policyx.generate_PolicyTargetTrue(getTestPanel());
					// tests.add(PTT);
					PolicySpreadSheetTestRecord ptt = policyx
							.generate_PolicyTargetTrue(getTestPanel());
					if (ptt != null) {
						tests.add(ptt);
						String mutantPath = policyMutator
								.getMutantFileName("PTT1");
						String reqPath = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + ptt.getRequestFile();
						if (validation(policyPath, mutantPath, reqPath)) {
							mlistIndex++;
							tlistIndex++;
							valid.add(ptt);
							validTests++;
						}
					}
					getMutantsByType(policyMutator.getMutantList(), testable,
							"PTT");
				}
				if (boxPTF.isSelected()) {
					policyMutator.createPolicyTargetFalseMutants();
					PolicySpreadSheetTestRecord ptf = policyx
							.generate_PolicyTargetFalse(getTestPanel());
					if (ptf != null) {
						tests.add(ptf);
						String mutantPath = policyMutator
								.getMutantFileName("PTF1");
						String reqPath = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + ptf.getRequestFile();
						if (validation(policyPath, mutantPath, reqPath)) {
							mlistIndex++;
							tlistIndex++;
							valid.add(ptf);
							validTests++;
						}
					}
					getMutantsByType(policyMutator.getMutantList(), testable,
							"PTF");
				}
				if (boxCRC.isSelected()) {
					policyMutator.createCombiningAlgorithmMutants();
					long start = System.currentTimeMillis();
					Vector<Vector<Object>> data = new Vector<Vector<Object>>();
					data = policyx.generateRequestForDifferenceRCAs();

					ArrayList<PolicySpreadSheetTestRecord> crc = new ArrayList<PolicySpreadSheetTestRecord>();
					int count = 1;
					for (Vector<Object> child : data) {
						String request = child.get(4).toString();
						if(request == null || request == "")
							continue;
						try {
							String path = testPanel
									.getTestOutputDestination("_MutationTests")
									+ File.separator
									+ "request"
									+ "CRC"
									+ count + ".txt";
							FileWriter fw = new FileWriter(path);
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(request);
							bw.close();
							
							PolicySpreadSheetTestRecord ptr = null;
							ptr = new PolicySpreadSheetTestRecord(
									PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + "CRC" + count,
									"request" + "CRC" + count + ".txt", request, "");
							if(ptr != null){
								crc.add(ptr);
								count++;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					determineValidTests(crc, policyMutator, policyPath, "CRC");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (crc.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "CRC");	

				}
				if (boxCRE.isSelected()) {
					policyMutator.createRuleEffectFlippingMutants();
					long start = System.currentTimeMillis();
					ArrayList<PolicySpreadSheetTestRecord> cre = policyx
							.generate_FlipRuleEffect(getTestPanel());
					determineValidTests(cre, policyMutator, policyPath, "CRE");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (cre.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "CRE");
				}
				if (boxRER.isSelected()) {
					policyMutator.createRemoveRuleMutants();
					long start = System.currentTimeMillis();
					ArrayList<PolicySpreadSheetTestRecord> rer = policyx
							.generate_RemoveOneRule(getTestPanel());
					determineValidTests(rer, policyMutator, policyPath, "RER");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (rer.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "RER");
				}

				if (boxANR.isSelected()) {
					// policyMutator.createAddNewRuleMutants();

				}

				if (boxRTT.isSelected()) {
					policyMutator.createRuleTargetTrueMutants();
					long start = System.currentTimeMillis();
					ArrayList<PolicySpreadSheetTestRecord> rtt = policyx
							.generate_RuleTargetTrue(getTestPanel());
					determineValidTests(rtt, policyMutator, policyPath, "RTT");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (rtt.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "RTT");
				}

				if (boxRTF.isSelected()) {
					policyMutator.createRuleTargetFalseMutants();
					long start = System.currentTimeMillis();
					ArrayList<PolicySpreadSheetTestRecord> rtf = policyx
							.generate_RuleTargetFalse(getTestPanel(),
									policyMutator);
					determineValidTests(rtf, policyMutator, policyPath, "RTF");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (rtf.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "RTF");
				}

				if (boxRCT.isSelected()) {
					policyMutator.createRuleConditionTrueMutants();
					long start = System.currentTimeMillis();
					ArrayList<PolicySpreadSheetTestRecord> rct = policyx
							.generate_RuleConditionTrue(getTestPanel());
					determineValidTests(rct, policyMutator, policyPath, "RCT");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (rct.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "RCT");
				}

				if (boxRCF.isSelected()) {
					policyMutator.createRuleConditionFalseMutants();
					long start = System.currentTimeMillis();
					ArrayList<PolicySpreadSheetTestRecord> rcf = policyx
							.generate_RuleConditionFalse(getTestPanel());
					determineValidTests(rcf, policyMutator, policyPath, "RCF");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (rcf.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "RCF");
				}

				if (boxFPR.isSelected()) {
					policyMutator.createFirstPermitRuleMutants();
					long start = System.currentTimeMillis();
					ArrayList<PolicySpreadSheetTestRecord> fpr = policyx
							.generate_FirstPermitRule(getTestPanel());
					determineValidTests(fpr, policyMutator, policyPath, "FPR");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (fpr.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "FPR");
				}

				if (boxFDR.isSelected()) {
					policyMutator.createFirstDenyRuleMutants();
					long start = System.currentTimeMillis();
					ArrayList<PolicySpreadSheetTestRecord> fdr = policyx
							.generate_FirstDenyRule(getTestPanel());
					determineValidTests(fdr, policyMutator, policyPath, "FDR");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (fdr.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "FDR");
				}

				if (boxRTR.isSelected()) {
					// policyMutator.createRuleTypeReplacedMutants();

				}

				if (boxFCF.isSelected()) {
					// policyMutator.createFlipComparisonFunctionMutants();

				}

				if (boxANF.isSelected()) {
					policyMutator.createAddNotFunctionMutants();
					long start = System.currentTimeMillis();
					ArrayList<PolicySpreadSheetTestRecord> anf = policyx
							.generate_AddNotFunction(getTestPanel());
					determineValidTests(anf, policyMutator, policyPath, "ANF");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (anf.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "ANF");
				}

				if (boxRNF.isSelected()) {
					policyMutator.createRemoveNotFunctionMutants();
					long start = System.currentTimeMillis();
					ArrayList<PolicySpreadSheetTestRecord> rnf = policyx
							.generate_RemoveNotFunction(getTestPanel());
					determineValidTests(rnf, policyMutator, policyPath, "RNF");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (rnf.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "RNF");

				}

				if (boxRPTE.isSelected()) {
					policyMutator.createRemoveParallelTargetElementMutants();
					long start = System.currentTimeMillis();
					ArrayList<PolicySpreadSheetTestRecord> rpte = policyx
							.generate_RemoveParallelTargetElement(getTestPanel());
					determineValidTests(rpte, policyMutator, policyPath, "RPTE");
					totalGenerationTime += System.currentTimeMillis() - start;
					if(boxOptimize.isSelected())
						removeDuplicates(valid);
					if (rpte.size() >= 1)
						getMutantsByType(policyMutator.getMutantList(),
								testable, "RPTE");
				}

				if (boxRPCE.isSelected()) {
					// policyMutator.createRemoveParallelConditionElementMutants();

				}
				int numMutants = testable.size();
				mutantSuite = policyMutator.generateMutants(); // write to
																// spreadsheet
				setUpMutantPanel();
				if (validTests > 0)
					System.out
							.printf("Generated tests: " + tests.size()
									+ "\nValid tests: " + validTests
									+ "\nPercent valid: %.2f\n",
									((double) validTests / (double) tests
											.size()) * 100.00);
				System.out.println("Testable size: " + testable.size());

				if (boxOptimize.isSelected()) {
					long start = System.currentTimeMillis();
					optimize5(valid, policyMutator);
					totalOptimizationTime += (System.currentTimeMillis() - start);
				}

				// PolicySpreadSheetTestRecord[] myTests = new
				// PolicySpreadSheetTestRecord[valid.size()];
				// for(int i = 0; i < valid.size(); i++)
				// myTests[i] = valid.get(i);

				// optimize(valid, myTests, policyMutator.getMutantList(),
				// policyMutator);

				if (validTests > 0)
					System.out
							.printf("Generated tests: " + tests.size()
									+ "\nValid tests: " + validTests
									+ "\nPercent valid: %.2f\n",
									((double) validTests / (double) tests
											.size()) * 100.00);
				System.out.println("Mutants: " + numMutants);
				System.out.println("Generation time: " + totalGenerationTime
						/ 1000.00);
				System.out.println("Optimization time: "
						+ totalOptimizationTime / 1000.00);
				if (valid.size() > 0)
					System.out.println("Optimal tests: " + valid.size());

				try {
					testSuite = new PolicySpreadSheetTestSuite(valid,
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

	/*
	 * public String getTestOutputDestination(String testMethod) {
	 * 
	 * File file = xpa.getWorkingPolicyFile(); String path =
	 * file.getParentFile().getAbsolutePath(); String name = file.getName();
	 * name = name.substring(0, name.length() - 4); path = path + File.separator
	 * + "test_suites" + File.separator + name + testMethod; return path; }
	 */

	public TestPanel getTestPanel() {
		return this.testPanel;
	}

	private boolean validation(String policyPath, String mutantPath,
			String requestPath) {
		String request = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(requestPath));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			request = sb.toString();
			br.close();
		} catch (Exception e) {
			return false;
		}
		// System.out.println(request);
		loadPolicy lp = new loadPolicy();
		Policy original = lp.getPolicy(policyPath);
		Policy mutant = lp.getPolicy(mutantPath);
		// PolicyX policyx = new PolicyX(original);
		if (mutant != null) {
			int pres = lp.PolicyEvaluate(original, request);
			int mres = lp.PolicyEvaluate(mutant, request);
			// System.out.println("POLICY DECISION: " + pres +
			// "\nMUTANT DECISION: " + mres);
			if (pres == mres)
				return false;
			else
				return true;
		} else
			return false;
	}

	private void determineValidTests(
			ArrayList<PolicySpreadSheetTestRecord> records,
			PolicyMutator mutator, String policyPath, String type) {
		if (records.size() == 0)
			return;
		if (records.size() == 1) {
			tests.add(records.get(0));
			String req = testPanel.getTestOutputDestination("_MutationTests")
					+ File.separator + records.get(0).getRequestFile();
			int successes = 0;
			int matches = 0;
			for (PolicyMutant m : mutator.getMutantList()) {
				String muType = m.getNumber().substring(
						m.getNumber().indexOf(' ') + 1,
						m.getNumber().length() - 1);
				if (muType.compareTo(type) == 0) {
					matches++;
					String mutantPath = m.getMutantFilePath();
					successes += validation(policyPath, mutantPath, req) ? 1
							: 0;
				}
			}
			if (successes == matches) {
				valid.add(records.get(0));
				validTests++;
			}
			if (!valid.contains(records.get(0))) {
				System.err.println(req);
				Runtime run = Runtime.getRuntime();
				try {
					run.exec("sudo rm " + req);
				} catch (Exception e) {
					System.err.println("File deletion failed");
				}
			}
		} else {
			for (PolicySpreadSheetTestRecord record : records) {
				tests.add(record);
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + record.getRequestFile();
				int mutantNum = records.indexOf(record) + 1;
				String mutantPath = mutator.getMutantFileName(type + mutantNum);
				if (validation(policyPath, mutantPath, req)) {
					valid.add(record);
					validTests++;
				} else {
					System.err.println(req);
					Runtime run = Runtime.getRuntime();
					try {
						run.exec("sudo rm " + req);
					} catch (Exception e) {
						System.err.println("File deletion failed");
					}
				}
			}
		}
	}

	private void optimize(ArrayList<PolicySpreadSheetTestRecord> tests,
			PolicySpreadSheetTestRecord[] records,
			ArrayList<PolicyMutant> mutants, PolicyMutator mutator) {
		String policyPath = xpa.getWorkingPolicyFilePath();
		if (tests.size() == 1)
			return;
		for (int i = 0; i < records.length; i++) {
			if (records[i] == null)
				continue;

			PolicySpreadSheetTestRecord psstr = records[i];
			String req = testPanel.getTestOutputDestination("_MutationTests")
					+ File.separator + psstr.getRequestFile();
			String testNum = psstr.getNumber().substring(
					psstr.getNumber().indexOf(' ') + 1,
					psstr.getNumber().length());
			for (int j = 0; j < records.length; j++) {
				if (records[j] == null)
					continue;

				String mnum = records[j].getNumber().substring(
						records[j].getNumber().indexOf(' ') + 1,
						records[j].getNumber().length());
				String mutantPath = mutator.getMutantFileName(mnum);
				if (validation(policyPath, mutantPath, req)
						&& mnum.compareTo(testNum) == 0)
					continue;
				if (validation(policyPath, mutantPath, req)
						&& mnum.compareTo(testNum) != 0) {
					String rem = testPanel
							.getTestOutputDestination("_MutationTests")
							+ File.separator + records[j].getRequestFile();
					Runtime run = Runtime.getRuntime();
					try {
						run.exec("sudo rm" + rem);
					} catch (Exception e) {
						System.err.println("Unable to delete associated file");
					}
					records[j] = null;
				} else
					continue;
			}
		}
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		for (int i = 0; i < records.length; i++)
			if (records[i] != null)
				valid.add(records[i]);
	}

	private void optimize2(ArrayList<PolicySpreadSheetTestRecord> records,
			PolicyMutator mutator) {
		// LinkedHashSet<PolicySpreadSheetTestRecord> record_set = new
		// LinkedHashSet<PolicySpreadSheetTestRecord>();
		// for(PolicySpreadSheetTestRecord record : records)
		// record_set.add(record);
		String policyPath = xpa.getWorkingPolicyFilePath();
		for (int i = 0; i < records.size(); i++) {
			PolicySpreadSheetTestRecord psstr = records.get(i);
			String req = testPanel.getTestOutputDestination("_MutationTests")
					+ File.separator + psstr.getRequestFile();
			String testNum = psstr.getNumber().substring(
					psstr.getNumber().indexOf(' ') + 1,
					psstr.getNumber().length());
			for (int j = 0; j < records.size(); j++) {
				PolicySpreadSheetTestRecord m = records.get(j);
				String mnum = m.getNumber().substring(
						m.getNumber().indexOf(' ') + 1, m.getNumber().length());
				String mutantPath = mutator.getMutantFileName(mnum);
				if (validation(policyPath, mutantPath, req)
						&& mnum.contains(testNum))
					continue;
				else if (validation(policyPath, mutantPath, req)
						&& !mnum.contains(testNum)) {
					String rem = testPanel
							.getTestOutputDestination("_MutationTests")
							+ File.separator + records.get(j).getRequestFile();
					Runtime run = Runtime.getRuntime();
					try {
						run.exec("sudo rm" + rem);
					} catch (Exception e) {
						System.err.println("Unable to delete associated file");
					}
					records.remove(j);
				}
			}
		}
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		for (PolicySpreadSheetTestRecord record : records)
			valid.add(record);
	}

	private void optimize3(ArrayList<PolicySpreadSheetTestRecord> records,
			PolicyMutator mutator) {
		String policyPath = xpa.getWorkingPolicyFilePath();
		for (int i = 0; i < records.size() / 2; i++)// do a round of
													// optimization for each
													// mutation type
		{
			for (int j = 0; j < records.size() - 1; j++) {
				PolicySpreadSheetTestRecord psstr = records.get(j);
				PolicySpreadSheetTestRecord next = records.get(j + 1);
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				String mnum = next.getNumber().substring(
						next.getNumber().indexOf(' ') + 1,
						next.getNumber().length());
				String mutantPath = mutator.getMutantFileName(mnum);
				if (validation(policyPath, mutantPath, req)) {
					String rem = testPanel
							.getTestOutputDestination("_MutationTests")
							+ File.separator + next.getRequestFile();
					Runtime run = Runtime.getRuntime();
					try {
						run.exec("sudo rm" + rem);
					} catch (Exception e) {
						System.err.println("Unable to delete associated file");
					}
					records.remove(j);
				} else
					continue;
			}
		}
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		for (PolicySpreadSheetTestRecord record : records)
			valid.add(record);
	}

	private void getMutantsByType(ArrayList<PolicyMutant> mutants,
			ArrayList<PolicyMutant> sublist, String type) {
		for (PolicyMutant m : mutants) {
			if (m.getNumber().contains(type))
				sublist.add(m);
		}
	}

	private int getTestsByType(ArrayList<PolicySpreadSheetTestRecord> records,
			ArrayList<PolicySpreadSheetTestRecord> sublist, String type,
			int current) {
		while (records.get(current).getNumber().contains(type)
				&& current < records.size()) {
			sublist.add(records.get(current));
			current++;
		}
		return current;
	}

	private long onTheFlyOptimization(
			ArrayList<PolicySpreadSheetTestRecord> records,
			ArrayList<PolicyMutant> mutants, PolicyMutator mutator) {
		long tot = 0;
		PolicySpreadSheetTestRecord[] recs = new PolicySpreadSheetTestRecord[records
				.size()];
		String policyPath = xpa.getWorkingPolicyFilePath();
		for (int i = 0; i < records.size(); i++) {
			recs[i] = records.get(i);
		}
		long start = System.currentTimeMillis();
		for (PolicySpreadSheetTestRecord ptr : records) {
			String req = testPanel.getTestOutputDestination("_MutationTests")
					+ File.separator + ptr.getRequestFile();
			String testNum = ptr.getNumber().substring(
					ptr.getNumber().indexOf(' ') + 1, ptr.getNumber().length());
			for (int i = 0; i < mutants.size(); i++) {
				PolicyMutant m = mutants.get(i);
				String mutant = m.getNumber().substring(
						m.getNumber().indexOf(' ') + 1, m.getNumber().length());
				String mutantPath = mutator.getMutantFileName(mutant);
				if (validation(policyPath, mutantPath, req)
						&& m.getNumber().compareTo(testNum) != 0) {
					for (int k = 0; k < records.size(); k++) {
						PolicySpreadSheetTestRecord rec = records.get(k);
						;
						String recNum = rec.getNumber().substring(
								rec.getNumber().indexOf(' ') + 1,
								rec.getNumber().length());
						if (recNum.compareTo(mutant) == 0)
							recs[k] = null;
					}
				} else
					continue;
			}
		}
		tot += System.currentTimeMillis() - start;
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		for (int i = 0; i < recs.length; i++)
			if (recs[i] != null)
				valid.add(recs[i]);
		return tot;
	}

	private void optimize4(ArrayList<PolicySpreadSheetTestRecord> records,
			PolicyMutator mutator) {
		PolicySpreadSheetTestRecord[] input = new PolicySpreadSheetTestRecord[records
				.size()];
		for (int i = 0; i < input.length; i++)
			input[i] = records.get(i);
		mergeArray = new PolicySpreadSheetTestRecord[input.length];
		doOptimization(0, input.length - 1, input, mutator);
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		for (int i = 0; i < input.length; i++)
			if (input[i] != null)
				valid.add(mergeArray[i]);
	}

	private void doOptimization(int low, int high,
			PolicySpreadSheetTestRecord[] records, PolicyMutator mutator) {
		if (low < high) {
			int mid = low + (high - low) / 2;
			doOptimization(low, mid, records, mutator);
			doOptimization(mid + 1, high, records, mutator);
			merge_kill(low, mid, high, records, mutator);
		}
	}

	private void merge_kill(int low, int mid, int high,
			PolicySpreadSheetTestRecord[] records, PolicyMutator mutator) {
		for (int i = low; i <= high; i++)
			mergeArray[i] = records[i];

		String policy = xpa.getWorkingPolicyFilePath();
		int i = low;
		int j = high;
		int k = low;
		int l = high;
		while (j >= low && i <= high)// search from bottom up
		{
			if (i == j)// i and j point to same request/test combination
			{
				j--;
				i++;
				continue;
			}
			if (mergeArray[i] != null) {
				PolicySpreadSheetTestRecord psstr = mergeArray[i];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (mergeArray[j] != null) {
					PolicySpreadSheetTestRecord m = mergeArray[j];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + mutant;
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("sudo rm" + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						mergeArray[j] = null;
						records[j] = null;
						j--;
					} else
						i++;
				} else
					j--;
			} else
				i++;
		}
		while (l >= low && k <= high)// search from top down
		{
			if (k == l)// k and l point to same test/mutant combo
			{
				k++;
				l--;
				continue;
			}
			if (mergeArray[l] != null) {
				PolicySpreadSheetTestRecord psstr = mergeArray[l];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (mergeArray[k] != null) {
					PolicySpreadSheetTestRecord m = mergeArray[k];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + mutant;
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("sudo rm" + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						mergeArray[k] = null;
						records[k] = null;
						k++;
					} else
						l--;
				} else
					k++;
			} else
				l--;
		}
		/*
		 * i = low; j = i + 1; while(j <= high && i <= high)//semi-iterative
		 * bottom up { if(mergeArray[i] != null) { PolicySpreadSheetTestRecord
		 * psstr = mergeArray[i]; String req =
		 * testPanel.getTestOutputDestination("_MutationTests") + File.separator
		 * + psstr.getRequestFile(); String testNum =
		 * psstr.getNumber().substring(psstr.getNumber().indexOf(' ') + 1,
		 * psstr.getNumber().length()); if(mergeArray[j] != null) {
		 * PolicySpreadSheetTestRecord m = mergeArray[j]; String mnum =
		 * m.getNumber().substring(m.getNumber().indexOf(' ') + 1,
		 * m.getNumber().length()); String mutant =
		 * mutator.getMutantFileName(mnum); if(validation(policy, mutant, req)
		 * && mnum.compareTo(testNum) != 0) { String rem =
		 * testPanel.getTestOutputDestination("_MutationTests") + File.separator
		 * + mutant; Runtime run = Runtime.getRuntime(); try {
		 * run.exec("sudo rm" + rem); } catch(Exception e) {
		 * e.printStackTrace(); } mergeArray[j] = null; records[j] = null; j++;
		 * } else { i = j; j = i + 1; continue; } } else j++; } else i++; }
		 * 
		 * l = high; k = l - 1; while(l >= low && k >= low)//semi-iterative top
		 * down { if(mergeArray[l] != null) { PolicySpreadSheetTestRecord psstr
		 * = mergeArray[l]; String req =
		 * testPanel.getTestOutputDestination("_MutationTests") + File.separator
		 * + psstr.getRequestFile(); String testNum =
		 * psstr.getNumber().substring(psstr.getNumber().indexOf(' ') + 1,
		 * psstr.getNumber().length()); if(mergeArray[k] != null) {
		 * PolicySpreadSheetTestRecord m = mergeArray[k]; String mnum =
		 * m.getNumber().substring(m.getNumber().indexOf(' ') + 1,
		 * m.getNumber().length()); String mutant =
		 * mutator.getMutantFileName(mnum); if(validation(policy, mutant, req)
		 * && mnum.compareTo(testNum) != 0) { String rem =
		 * testPanel.getTestOutputDestination("_MutationTests") + File.separator
		 * + mutant; Runtime run = Runtime.getRuntime(); try {
		 * run.exec("sudo rm" + rem); } catch(Exception e) {
		 * e.printStackTrace(); } mergeArray[k] = null; records[k] = null; k--;
		 * } else { l = k; k = l - 1; continue; } } else k--; } else l--; }
		 */
		// Total for merge_kill: 2n iterations
		// Overall: 2nlogn or O(nlogn)
	}

	private void optimize5(ArrayList<PolicySpreadSheetTestRecord> records,
			PolicyMutator mutator) {
		PolicySpreadSheetTestRecord[] test = new PolicySpreadSheetTestRecord[records
				.size()];
		for (int i = 0; i < records.size(); i++)
			test[i] = records.get(i);
		String policy = xpa.getWorkingPolicyFilePath();
		int i = 0;
		int j = test.length - 1;
		int k = 0;
		int l = test.length - 1;

		while (i < test.length && j >= 0) {
			if (i == j) {
				i++;
				j--;
				continue;
			}
			if (test[i] != null) {
				PolicySpreadSheetTestRecord psstr = test[i];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (test[j] != null) {
					PolicySpreadSheetTestRecord m = test[j];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + mutant;
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("sudo rm" + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[j] = null;
						j--;
					} else
						i++;
				} else
					j--;
			} else
				i++;
		}

		while (l >= 0 && k < test.length) {
			if (k == l) {
				k++;
				l--;
				continue;
			}
			if (test[l] != null) {
				PolicySpreadSheetTestRecord psstr = test[l];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (test[k] != null) {
					PolicySpreadSheetTestRecord m = test[k];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + mutant;
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("sudo rm" + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[k] = null;
						k++;
					} else
						l--;
				} else
					k++;
			} else
				l--;
		}

		i = 0;
		j = i + 1;
		while (j < test.length && i <= test.length)// semi-iterative bottom up
		{
			if (test[i] != null) {
				PolicySpreadSheetTestRecord psstr = test[i];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (test[j] != null) {
					PolicySpreadSheetTestRecord m = test[j];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + mutant;
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("sudo rm" + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[j] = null;
						j++;
					} else {
						i = j;
						j = i + 1;
						continue;
					}
				} else
					j++;
			} else
				i++;
		}

		l = test.length - 1;
		k = l - 1;
		while (l >= 0 && k >= 0)// semi-iterative top down
		{
			if (test[l] != null) {
				PolicySpreadSheetTestRecord psstr = test[l];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (test[k] != null) {
					PolicySpreadSheetTestRecord m = test[k];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + mutant;
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("sudo rm" + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[k] = null;
						k--;
					} else {
						l = k;
						k = l - 1;
						continue;
					}
				} else
					k--;
			} else
				l--;
		}
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		for (PolicySpreadSheetTestRecord r : test)
			if (r != null)
				valid.add(r);
	}

	private void removeDuplicates(ArrayList<PolicySpreadSheetTestRecord> records) {
		for (int i = 0; i < records.size(); i++) {
			String req = records.get(i).getRequest();
			for (int j = i + 1; j < records.size(); j++) {
				if (records.get(j).getRequest().compareTo(req) == 0)
					records.remove(j);
			}
		}
	}

	private LinkedHashSet<String> findDuplicateRequests(
			ArrayList<String> requests) {
		LinkedHashSet<String> duplicates = new LinkedHashSet<String>();

		boolean first = true;
		for (String s : requests) {
			if (!duplicates.contains(s)) {
				duplicates.add(s);
			}
		}
		return duplicates;
	}
}
