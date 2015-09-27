package org.sag.repair;

import java.util.ArrayList;
import java.util.List;

import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.mutation.PolicyMutant;
import org.sag.mutation.PolicyMutator;
import org.sag.mutation.PolicyMutatorByPosition;

public class PolicyRepairer {
	String testSuiteFile = null;
	
	public PolicyRepairer(String testSuiteFile) {
		this.testSuiteFile = testSuiteFile;
	}
	
	private PolicyMutant find1stCorrectMutant(List<PolicyMutant> mutantList) throws Exception	{
		for(PolicyMutant mutant: mutantList) {
			//System.out.println(mutant.getMutantFilePath() + "\n");
			PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteFile, mutant.getMutantFilePath());
			boolean[] testResults = testSuite.runAllTestsOnMutant();
			boolean is_repaired = booleanArrayAnd(testResults);
			if(is_repaired) {
				return mutant;
			}
		}
		return null;
	}
	
	private List<PolicyMutant> findAllCorrectMutants(List<PolicyMutant> mutantList) throws Exception	{
		List<PolicyMutant> correctMutants = new ArrayList<PolicyMutant>();
		for(PolicyMutant mutant: mutantList) {
			//System.out.println(mutant.getMutantFilePath() + "\n");
			PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteFile, mutant.getMutantFilePath());
			boolean[] testResults = testSuite.runAllTestsOnMutant();
			boolean is_repaired = booleanArrayAnd(testResults);
			if(is_repaired) {
				correctMutants.add(mutant);
			}
		}
		return correctMutants;
	}
	
	/**
	 * @param policyFileToRepair, file path of the policy file to be repaired
	 * @return file path of repaired file; null if cannot be repaired
	 * @throws Exception
	 * generate all mutants at once and check which one can pass the test suite
	 */
	public PolicyMutant repair(String policyFileToRepair) throws Exception {
		PolicyMutatorByPosition mutator = new PolicyMutatorByPosition(policyFileToRepair);
		mutator.createAllMutants();
		List<PolicyMutant> mutants = mutator.getMutantList();
		return find1stCorrectMutant(mutants);
	}
	
	public void testByPosition(String policyFileToRepair) throws Exception {
		PolicyMutator mutator = new PolicyMutator(policyFileToRepair);
		mutator.createAllMutants();
		List<PolicyMutant> mutants = mutator.getMutantList();
		for(PolicyMutant mutant: findAllCorrectMutants(mutants)) {
			System.out.println(mutant.getMutantFilePath());
		}
		
		System.out.println("=================================");
		
		PolicyMutatorByPosition mutatorByPosition = new PolicyMutatorByPosition(policyFileToRepair);
		mutatorByPosition.createAllMutants();
		List<PolicyMutant> mutantsByPosition = mutatorByPosition.getMutantList();
		for(PolicyMutant mutant: findAllCorrectMutants(mutants)) {
			System.out.println(mutant.getMutantFilePath());
		}
		
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
	
//	/**
//	 * @param policyFileToRepair, file path of the policy file to be repaired
//	 * @return file path of repaired file; null if cannot be repaired
//	 * @throws Exception
//	 * generate a mutant a time and check whether it can pass the test suite
//	 */
//	public PolicyMutant repairOneByOne(String policyFileToRepair) throws Exception {
//		List<PolicyMutant> mutantList = null;
//		PolicyMutant correctMutant = null;
//		mutator = new PolicyMutatorByPosition(policyFileToRepair);
//		// PTT
//		mutantList = ((PolicyMutatorByPosition) mutator).createPolicyTargetTrueMutants();
//		correctMutant = find1stCorrectMutant(mutantList);
//		if(correctMutant != null) {
//			return correctMutant;
//		}
//		// PTF
//		mutantList = ((PolicyMutatorByPosition) mutator).createPolicyTargetTrueMutants();
//		correctMutant = find1stCorrectMutant(mutantList);
//		if(correctMutant != null) {
//			return correctMutant;
//		}
//		// CRC
//		mutantList = ((PolicyMutatorByPosition) mutator).createCombiningAlgorithmMutants();
//		correctMutant = find1stCorrectMutant(mutantList);
//		if(correctMutant != null) {
//			return correctMutant;
//		}
//		
//		return null;
//	}

}
