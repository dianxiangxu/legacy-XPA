package org.sag.repair;

import java.util.ArrayList;

import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.mutation.PolicyMutant;
import org.sag.mutation.PolicyMutator;

public class PolicyRepairer {
	String testSuiteFile = null;
	private PolicyMutator mutator = null;
	public PolicyRepairer(String testSuiteFile) {
		this.testSuiteFile = testSuiteFile;
	}
	public String repair(String policyFileToRepair) throws Exception {
		PolicyMutator policyMutator = new PolicyMutator(policyFileToRepair);
		policyMutator.createAllMutants();
		ArrayList<PolicyMutant> mutants = policyMutator.getMutantList();
		System.out.println(policyMutator.getMutantFileNameBase());
		//PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteFile, mutant);
		return "";
	}

}
