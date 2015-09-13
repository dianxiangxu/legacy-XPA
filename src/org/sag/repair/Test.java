package org.sag.repair;

import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.mutation.PolicyMutator;
import org.sag.mutation.PolicySpreadSheetMutantSuite;

public class Test {

	public static void main(String[] args) throws Exception {
		simpleRepair();
	}
	
	static void simpleRepair() throws Exception {
		String PolicyFilePath = "Experiments//conference3//conference3.xml";
		PolicyMutator policyMutator = new PolicyMutator(PolicyFilePath);
		policyMutator.createPolicyTargetTrueMutants();
		policyMutator.createPolicyTargetFalseMutants();
		policyMutator.createCombiningAlgorithmMutants();
		policyMutator.createRuleEffectFlippingMutants();
		policyMutator.createRemoveRuleMutants();
		policyMutator.createAddNewRuleMutants();
		policyMutator.createRuleTargetTrueMutants();
		policyMutator.createRuleTargetFalseMutants();
		policyMutator.createRuleConditionTrueMutants();
		policyMutator.createRuleConditionFalseMutants();
		policyMutator.createFirstPermitRuleMutants();
		policyMutator.createFirstDenyRuleMutants();
		policyMutator.createRuleTypeReplacedMutants();
		policyMutator.createFlipComparisonFunctionMutants();
		policyMutator.createAddNotFunctionMutants();
		policyMutator.createRemoveNotFunctionMutants();
		policyMutator.createRemoveParallelTargetElementMutants();
		policyMutator.createRemoveParallelConditionElementMutants();
		
		PolicySpreadSheetMutantSuite mutantSuite = policyMutator.generateMutants(); // write to spreadsheet
		
		String outputFileName = "Experiments//conference3//test_suites//conference3_MCDCCoverage_NoError//MutationTestingResults.xls";
		String testSuiteSpreadSheetFile = "Experiments//conference3//test_suites//conference3_MCDCCoverage_NoError//conference3_MCDCCoverage_NoError.xls";
		PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteSpreadSheetFile,
				PolicyFilePath);
		mutantSuite.runAndWriteDetectionInfoToExcelFile(outputFileName, testSuite);
	}
	
	static void simpleRun() throws Exception {
		//String policyfile = "Experiments//conference3//conference3.xml";
		String mutantfile = "Experiments//conference3//mutants//conference3_ANR1.xml";
		String testSuiteFile = "Experiments//conference3//test_suites//conference3_MCDCCoverage//conference3_MCDCCoverage.xls";
		PolicySpreadSheetTestSuite policySpreadSheetTestSuite = new PolicySpreadSheetTestSuite(testSuiteFile, mutantfile);
		policySpreadSheetTestSuite.runAllTests();
	}

}
