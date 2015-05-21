package org.sag.coverage;

import org.sag.faultlocalization.SpectrumBasedDiagnosisResults;
import org.sag.faultlocalization.SpectrumBasedFaultLocalizer;

public class CoverageDemoKmarketSpreadsheetTests {
	public static void main(String[] args) throws Exception{
		PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite("tests//kmarket-tests.xls","tests//kmarket-blue-policy.xml");
		testSuite.runAllTests();
		System.out.println("\n");
		testSuite.generateJUnitFile("src", 
				"org.sag.coverage", "KmarketGeneratedTests");
		PolicyCoverageFactory.writeCoverageToSpreadSheet("tests//coverage.xls");
		for (SpectrumBasedDiagnosisResults results: SpectrumBasedFaultLocalizer.applyAllFaultLocalizers()){
			results.printCoefficients();
		}
	}
}
