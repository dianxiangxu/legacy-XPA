package org.sag.repair;

import java.util.ArrayList;

import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.mutation.PolicyMutant;
import org.sag.mutation.PolicyMutator;

public class PolicyRepairer {
	String testSuiteFile = null;
	private PolicyMutator mutator;
	
	public PolicyRepairer(String testSuiteFile) {
		this.testSuiteFile = testSuiteFile;
	}
	
	/**
	 * @param policyFileToRepair, file path of the policy file to be repaired
	 * @return file path of repaired file; null if cannot be repaired
	 * @throws Exception
	 */
	public String repair(String policyFileToRepair) throws Exception {
		mutator = new PolicyMutator(policyFileToRepair);
		mutator.createAllMutants();
		ArrayList<PolicyMutant> mutants = mutator.getMutantList();
		System.out.println(mutator.getMutantFileNameBase());
		String repairedFile = null;
		for(PolicyMutant mutant : mutants) {
			//System.out.println(mutant.getMutantFilePath() + "\n");
			PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteFile, mutant.getMutantFilePath());
			boolean[] testResults = testSuite.runAllTestsOnMutant();
			boolean is_repaired = booleanArrayAnd(testResults);
			if(is_repaired) {
				repairedFile = mutant.getMutantFilePath();
				break;
			}
		}
		return repairedFile;
	}
	
	/**
	 * @param booleanArray
	 * @return result of logical AND on all elements of the boolean array
	 */
	private boolean booleanArrayAnd(boolean[] booleanArray) {
		boolean result = true;
		for(boolean b: booleanArray) {
			result = result && b;
		}
		return result;
	}

}
