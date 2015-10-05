package org.sag.repair;

import java.util.ArrayList;
import java.util.List;

import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.mutation.PolicyMutant;
import org.sag.mutation.PolicyMutator;
import org.sag.mutation.PolicyMutatorByPosition;
import org.wso2.balana.PolicyTreeElement;
import org.wso2.balana.Rule;
import org.wso2.balana.combine.CombinerElement;

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
		for(PolicyMutant mutant: findAllCorrectMutants(mutantsByPosition)) {
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
	
	/**
	 * @param policyFileToRepair, file path of the policy file to be repaired
	 * @return file path of repaired file; null if cannot be repaired
	 * @throws Exception
	 * generate a mutant a time and check whether it can pass the test suite
	 */
	public PolicyMutant repairOneByOne(String policyFileToRepair) throws Exception {
		List<PolicyMutant> mutantList = null;
		PolicyMutant correctMutant = null;
		mutator = new PolicyMutatorByPosition(policyFileToRepair);
		
		//create mutant methods who's bugPosition == -1
		// CRC
		mutantList = mutator.createCombiningAlgorithmMutants();
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		
		//create mutant methods who's bugPosition == 0
		// PTT
		mutantList = mutator.createPolicyTargetTrueMutants();
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		// PTF
		mutantList = mutator.createPolicyTargetTrueMutants();
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//FPR
		mutantList = mutator.createFirstPermitRuleMutants();
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//FDR
		mutantList = mutator.createFirstDenyRuleMutants();
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}

		//create mutant methods who's bugpositon depend on rule
		//final int mutantIndex=1;
		int ruleIndex = 1;
		int maxRules = mutator.getPolicy().getChildElements().size();
		for (CombinerElement rule : mutator.getPolicy().getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				//CRE
				mutantList = createRuleEffectFlippingMutants(myrule, ruleIndex);
				correctMutant = find1stCorrectMutant(mutantList);
				if(correctMutant != null) {
					return correctMutant;
				}
				//ANR
				mutantList = createAddNewRuleMutants(myrule, ruleIndex);
				correctMutant = find1stCorrectMutant(mutantList);
				if(correctMutant != null) {
					return correctMutant;
				}
				//RTT
				mutantList = createRuleTargetTrueMutants(myrule, ruleIndex);
				correctMutant = find1stCorrectMutant(mutantList);
				if(correctMutant != null) {
					return correctMutant;
				}
				//RTF
				mutantList = createRuleTargetFalseMutants(myrule, ruleIndex);
				correctMutant = find1stCorrectMutant(mutantList);
				if(correctMutant != null) {
					return correctMutant;
				}
				//RCT
				mutantList = createRuleConditionTrueMutants(myrule, ruleIndex);
				correctMutant = find1stCorrectMutant(mutantList);
				if(correctMutant != null) {
					return correctMutant;
				}
				//RCF
				mutantList = createRuleConditionFalseMutants(myrule, ruleIndex);
				correctMutant = find1stCorrectMutant(mutantList);
				if(correctMutant != null) {
					return correctMutant;
				}
				
				
				
				
				
				//RER
				//BECAREFUL!!! bugPosition is maxRule
				mutantList = createRemoveRuleMutants(myrule, ruleIndex, maxRules);
				correctMutant = find1stCorrectMutant(mutantList);
				if(correctMutant != null) {
					return correctMutant;
				}
				ruleIndex++;
			}
		}
		return null;
	}

}
