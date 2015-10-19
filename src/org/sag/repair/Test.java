package org.sag.repair;

import java.util.List;

import org.sag.coverage.PolicyCoverageFactory;
import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.faultlocalization.SpectrumBasedDiagnosisResults;
import org.sag.faultlocalization.SpectrumBasedFaultLocalizer;
import org.sag.mutation.PolicyMutant;
import org.sag.mutation.PolicyMutator;
import org.sag.mutation.PolicySpreadSheetMutantSuite;

public class Test {

	public static void main(String[] args) throws Exception {
		//String PolicyFilePath = "Experiments//conference3//conference3.xml";
//		String PolicyFilePath = "Experiments//conference3//mutants//conference3_ANR2.xml";
		
//		genMutantsAndTest(PolicyFilePath);
		
//		runTestsuiteOnPolicy(PolicyFilePath);
		
//		testRepair();
		testFaultLocalizer();
		
		
//		String policyFileToRepair = "Experiments//conference3//test_suites//conference3_MCDCCoverage_NoError//conference3_MCDCCoverage_NoError.xls";
//		compareByPosition(policyFileToRepair);
	}
	
	static void compareByPosition(String policyFileToRepair) throws Exception {
		String testSuiteSpreadSheetFile = "Experiments//conference3//test_suites//conference3_MCDCCoverage_NoError//conference3_MCDCCoverage_NoError.xls";
		//String policyFileToRepair = "Experiments//conference3//mutants//conference3_ANR2.xml";
		PolicyRepairer repairer = new PolicyRepairer(testSuiteSpreadSheetFile);
		repairer.testByPosition(policyFileToRepair);
	}
	
	static void testRepair() throws Exception {
		String testSuiteSpreadSheetFile = "Experiments//conference3//test_suites//conference3_MCDCCoverage_NoError//conference3_MCDCCoverage_NoError.xls";
		String policyFileToRepair = "Experiments//conference3//mutants//conference3_ANR4.xml";
		PolicyRepairer repairer = new PolicyRepairer(testSuiteSpreadSheetFile);
//		PolicyMutant correctedPolicy = repairer.repair(policyFileToRepair);
//		PolicyMutant correctedPolicy = repairer.repairOneByOne_new(policyFileToRepair);
		PolicyMutant correctedPolicy = repairer.repairRandomOrder(policyFileToRepair);
		showRepairResult(correctedPolicy, policyFileToRepair);
	}
	
	/**
	 * for given policy file, generate some mutants and test the mutants, write test results
	 * to a spread sheet file
	 * @param PolicyFilePath 
	 * @throws Exception
	 */
	static void genMutantsAndTest(String PolicyFilePath) throws Exception {
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
	
	/**
	 * run testsuite on given policy
	 * @param policyfile
	 * @throws Exception
	 */
	static void runTestsuiteOnPolicy(String policyfile) throws Exception {
		String testSuiteFile = "Experiments//conference3//test_suites//conference3_MCDCCoverage_NoError//conference3_MCDCCoverage_NoError.xls";
		PolicySpreadSheetTestSuite policySpreadSheetTestSuite = new PolicySpreadSheetTestSuite(testSuiteFile, policyfile);
		policySpreadSheetTestSuite.runAllTests();
	}
	
	static void testFaultLocalizer() throws Exception {
		String policyFileToRepair = "Experiments//conference3//mutants//conference3_ANR4.xml";
		String testSuiteSpreadSheetFile = "Experiments//conference3//test_suites//conference3_MCDCCoverage_NoError//conference3_MCDCCoverage_NoError.xls";
		PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteSpreadSheetFile,
				policyFileToRepair);
		testSuite.runAllTests();
		System.out.println("\n");
		testSuite.generateJUnitFile("src", 
				"org.sag.coverage", "KmarketGeneratedTests");
		//PolicyCoverageFactory.writeCoverageToSpreadSheet("tests//coverage.xls");
		PolicyRepairer repairer = new PolicyRepairer(testSuiteSpreadSheetFile);
		for (SpectrumBasedDiagnosisResults results: SpectrumBasedFaultLocalizer.applyAllFaultLocalizers()){
			results.printCoefficients();
			List<Integer> suspicionRank = results.getRuleIndexRankedBySuspicion();
			PolicyMutant correctedPolicy = repairer.repairBySuspicionRank(policyFileToRepair, suspicionRank);
			showRepairResult(correctedPolicy, policyFileToRepair);
		}
		
		
	}
	
	static public void showRepairResult(PolicyMutant correctedPolicy, String policyFileToRepair) throws Exception {
		if(correctedPolicy == null) {
			System.out.println("cannot repair\n");
		} else {
			System.out.format("\npolicy File To Repair: %s\n", policyFileToRepair);
			runTestsuiteOnPolicy(policyFileToRepair);
			System.out.format("\nrepaired file: %s\n",correctedPolicy.getMutantFilePath());
			runTestsuiteOnPolicy(correctedPolicy.getMutantFilePath());
		}
	}

}
