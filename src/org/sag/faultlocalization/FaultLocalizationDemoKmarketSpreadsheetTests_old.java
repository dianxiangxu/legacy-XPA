package org.sag.faultlocalization;

import org.sag.coverage.PolicyCoverageFactory;
import org.sag.coverage.PolicySpreadSheetTestSuite;

public class FaultLocalizationDemoKmarketSpreadsheetTests_old {

	
	public static void main(String[] args) throws Exception{
		PolicySpreadSheetTestSuite testSuite = 
				new PolicySpreadSheetTestSuite("Experiments//kmarket-blue-policy//test_suites//kmarket-blue-policy_RuleLevel//kmarket-blue-policy_RuleLevel.xls",
				"Experiments//kmarket-blue-policy//mutants//kmarket-blue-policy_RER1.xml");
		testSuite.runAllTests();
		System.out.println("\n");
//		testSuite.generateJUnitFile("//Users//dianxiangxu//Documents//JavaProjects//WSO2-XACML//XPA//src", 
		testSuite.generateJUnitFile("src", 
				"org.sag.faultlocalization", "KmarketGeneratedTests");
		PolicyCoverageFactory.writeCoverageToSpreadSheet("Experiments//kmarket-blue-policy//fault-localization//coverage_RuleLevel_RER1.xls");
		for (SpectrumBasedDiagnosisResults results: SpectrumBasedFaultLocalizer.applyAllFaultLocalizers()){
			results.printCoefficients();
		}
	}

}