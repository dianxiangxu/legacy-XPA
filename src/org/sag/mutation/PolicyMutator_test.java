package org.sag.mutation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.sag.combiningalgorithms.*;
import org.sag.coverage.PolicySpreadSheetTestRecord;
import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.wso2.balana.Policy;
import org.wso2.balana.Rule;
import org.wso2.balana.xacml3.Target;

public class PolicyMutator_test {
	
	/* In a panel, called MutationTest_Panel, have this method */
	
	HashSet test_table = new HashSet();
	
	public void generateMutants() {
		if (!xpa.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(xpa, "There is no policy.");
			return;
		}
		int result = JOptionPane.showConfirmDialog(xpa, createPanel(),
				"Please Select Mutation Methods",
				JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			try {
				PolicyMutator policyMutator = new PolicyMutator(
						xpa.getWorkingPolicyFilePath());
				if (boxPTT.isSelected()) {
					policyMutator.createPolicyTargetTrueMutants();
				}
				if (boxPTF.isSelected()) {
					policyMutator.createPolicyTargetFalseMutants();
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
				}
				if (boxRTF.isSelected()) {
					policyMutator.createRuleTargetFalseMutants();
				}
				if (boxRCT.isSelected()) {
					policyMutator.createRuleConditionTrueMutants();
				}
				if (boxRCF.isSelected()) {
					policyMutator.createRuleConditionFalseMutants();
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
				if (boxRNF.isSelected()) {
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
}
