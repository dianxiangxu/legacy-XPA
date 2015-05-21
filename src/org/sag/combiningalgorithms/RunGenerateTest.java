package org.sag.combiningalgorithms;

import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.faultlocalization.SpectrumBasedDiagnosisResults;
import org.sag.faultlocalization.SpectrumBasedFaultLocalizer;

public class RunGenerateTest {
	public static void main(String[] args) throws Exception {
		PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(
				"//home//nshen//xpa//branch//XPA//GenTests//byOne_pluto.xls",
				"//home//nshen//xpa//branch//XPA//GenTests//pluto3.xml");
		testSuite.runAllTests();
		System.out.println("\n");

	}
}
