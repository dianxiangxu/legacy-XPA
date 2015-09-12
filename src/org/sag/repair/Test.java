package org.sag.repair;

import org.sag.coverage.PolicySpreadSheetTestSuite;

public class Test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String policyfile = "Experiments//conference3//conference3.xml";
		String mutantfile = "Experiments//conference3//mutants//conference3_ANR1.xml";
		String testSuiteFile = "Experiments//conference3//test_suites//conference3_MCDCCoverage//conference3_MCDCCoverage.xls";
		PolicySpreadSheetTestSuite policySpreadSheetTestSuite = new PolicySpreadSheetTestSuite(testSuiteFile, mutantfile);
		policySpreadSheetTestSuite.runAllTests();

	}

}
