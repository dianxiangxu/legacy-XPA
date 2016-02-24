package org.sag.repair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sag.faultlocalization.FaultLocalizationExperiment;
import org.sag.mutation.PolicyMutant;
import org.sag.mutation.PolicyMutator;
import org.wso2.balana.Rule;

public class ExperimentMultiFault {

	public static void main(String[] args) throws Exception {
		String[] policy = { "conference3", "fedora-rule3", "itrust3",
				"kmarket-blue-policy", "obligation3", "pluto3" };
		String[] testsuite = { "Basic", "Exclusive", "Pair", "PDpair",
				"DecisionCoverage", "RuleLevel", "MCDCCoverage" };
		int policyNumber = 0;
		int testsuiteNumber = 6;

		String testSuiteSpreadSheetFile = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "test_suites"
				+ File.separator + policy[policyNumber] + "_"
				+ testsuite[testsuiteNumber] + File.separator
				+ policy[policyNumber] + "_" + testsuite[testsuiteNumber]
				+ ".xls";
		String policyMutantSpreadsheetFil = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "mutants"
				+ File.separator + policy[policyNumber] + "_mutants.xls";
		String experimentResultFileName = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "repair"
				+ File.separator + policy[policyNumber] + "_"
				+ testsuite[testsuiteNumber] + "_repair.csv";
		String policyFile = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + policy[policyNumber]
				+ ".xml";
		int numFaults = 2;
		// multiple faults
		List<String> createMutantMethods = new ArrayList<String>();
		createMutantMethods.add("createPolicyTargetTrueMutants");// PTT
		createMutantMethods.add("createPolicyTargetFalseMutants");// PTF
		// createMutantMethods.add("createCombiningAlgorithmMutants");//CRC
		// //comment out because cannot localize
		createMutantMethods.add("createRuleEffectFlippingMutants");// CRE
		// createMutantMethods.add("createRemoveRuleMutants");//RER
		// createMutantMethods.add("createAddNewRuleMutants");//ANR
		createMutantMethods.add("createRuleTargetTrueMutants");// RTT
		createMutantMethods.add("createRuleTargetFalseMutants");// RTF
		createMutantMethods.add("createRuleConditionTrueMutants");// RCT
		createMutantMethods.add("createRuleConditionFalseMutants");// RCF
		// createMutantMethods.add("createFirstPermitRuleMutants");//FPR
		// createMutantMethods.add("createFirstDenyRuleMutants");//FDR
		// createMutantMethods.add("createRuleTypeReplacedMutants");//RTR
		createMutantMethods.add("createAddNotFunctionMutants");// ANF
		createMutantMethods.add("createRemoveNotFunctionMutants");// RNF
		createMutantMethods.add("createRemoveParallelTargetElementMutants");// RPTE
		// createMutantMethods.add("createRemoveParallelConditionElementMutants");//RPCE

		List<PolicyMutant> mutantList = FaultLocalizationExperiment
				.createMultiFaultMutants(policyFile, numFaults,
						createMutantMethods);
		// String MutantsCSVFileName = createMutantsCSVFile(mutantList);

	}

	ExperimentMultiFault(PolicyMutant policyToRepair,
			String faultLocalizeMethod, String testSuiteFile) throws Exception {

		List<Integer> suspicionRank = PolicyRepairer.getSuspicionRank(
				policyToRepair, faultLocalizeMethod, testSuiteFile);
		for (int bugPosition : suspicionRank) {
			PolicyMutator mutator = new PolicyMutator(policyToRepair);
			List<Rule> ruleList = mutator.getRuleList();

		}
	}

}
