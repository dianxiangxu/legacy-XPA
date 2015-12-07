package org.sag.repair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.faultlocalization.SpectrumBasedDiagnosisResults;
import org.sag.faultlocalization.SpectrumBasedFaultLocalizer;
import org.sag.faultlocalization.TestCellResult;
import org.sag.mutation.PolicyMutant;
import org.sag.mutation.PolicyMutator;
import org.sag.mutation.PolicyMutatorByPosition;
import org.wso2.balana.PolicyTreeElement;
import org.wso2.balana.Rule;
import org.wso2.balana.combine.CombinerElement;

public class PolicyRepairer {
	String testSuiteFile = null;
	private int numTriesBeforSucceed;
	@SuppressWarnings("serial")
	static private List<List<String>> repairMethodPairList = new ArrayList<List<String>>() {
		{
			add(new ArrayList<String>(Arrays.asList("repairRandomOrder", null)));
			add(new ArrayList<String>(Arrays.asList("repairOneByOne", null)));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "jaccard")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "tarantula")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "ochiai")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "ochiai2")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "cbi")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "hamann")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "simpleMatching")));
//			add(new ArrayList<String>(Arrays.asList("repairSmartly", "sokal")));
//			add(new ArrayList<String>(Arrays.asList("repairSmartly", "naish2")));
//			add(new ArrayList<String>(Arrays.asList("repairSmartly", "goodman")));
//			add(new ArrayList<String>(Arrays.asList("repairSmartly", "sorensenDice")));
//			add(new ArrayList<String>(Arrays.asList("repairSmartly", "anderberg")));
//			add(new ArrayList<String>(Arrays.asList("repairSmartly", "euclid")));
//			add(new ArrayList<String>(Arrays.asList("repairSmartly", "rogersTanimoto")));
			
		}
	};
	
	public PolicyRepairer(String testSuiteFile) {
		this.testSuiteFile = testSuiteFile;
	}
	
	private PolicyMutant find1stCorrectMutant(List<PolicyMutant> mutantList) throws Exception	{
		if(mutantList == null) {
			return null;
		}
		for(PolicyMutant mutant: mutantList) {
			//System.out.println(mutant.getMutantFilePath() + "\n");
			PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteFile, mutant.getMutantFilePath());
			TestCellResult[] testCellResults = testSuite.runAllTestsOnMutant();
			List<Boolean> testResults = new ArrayList<Boolean>();
			for(TestCellResult res : testCellResults) {
				testResults.add(res.getVerdict());
			}
			boolean is_repaired = booleanListAnd(testResults);
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
			TestCellResult[] testCellResults = testSuite.runAllTestsOnMutant();
			List<Boolean> testResults = new ArrayList<Boolean>();
			for(TestCellResult res : testCellResults) {
				testResults.add(res.getVerdict());
			}
			boolean is_repaired = booleanListAnd(testResults);
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
	 * @param booleanList
	 * @return result of logical AND on all elements of the boolean array
	 */
	private boolean booleanListAnd(List<Boolean> booleanList) {
		boolean result = true;
		for(boolean b: booleanList) {
			result = result && b;
		}
		return result;
	}
	
	
	/**
	 * @param policyFileToRepair, file path of the policy file to be repaired
	 * @return file path of repaired file; null if cannot be repaired
	 * @throws Exception
	 * use fault localizer to find bugPosition, then generate mutants accordingly
	 * to repair
	 */
	public PolicyMutant repairSmartly(String policyFileToRepair, String faultLocalizeMethod) throws Exception {
		List<Integer> suspicionRank = new ArrayList<Integer>();
		PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(this.testSuiteFile,
				policyFileToRepair);
		testSuite.runAllTests();//we need to run tests to get coverage information, which is in turn used to get suspicion rank
		SpectrumBasedDiagnosisResults diagnosisResults = 
				SpectrumBasedFaultLocalizer.applyOneFaultLocalizerToPolicyMutant(faultLocalizeMethod);
		suspicionRank = diagnosisResults.getRuleIndexRankedBySuspicion();
		suspicionRank.add(0, -1);// a temporary solution for fault in combining algorithms
		return repairBySuspicionRank(policyFileToRepair, suspicionRank);
	}


	
	public PolicyMutant repairRandomOrder(String policyFileToRepair) throws Exception {
		List<Integer> suspicionRank = new ArrayList<Integer>();
		PolicyMutatorByPosition mutator = new PolicyMutatorByPosition(policyFileToRepair);
		int maxRules = mutator.getPolicy().getChildElements().size();
		for(int bugPosition = -1; bugPosition <= this.getRuleList(mutator).size(); bugPosition++) {
			suspicionRank.add(bugPosition);
		}
		suspicionRank.add(maxRules);
		Collections.shuffle(suspicionRank);
		return repairBySuspicionRank(policyFileToRepair, suspicionRank);
	}
	/**
	 * @param policyFileToRepair, file path of the policy file to be repaired
	 * @return file path of repaired file; null if cannot be repaired
	 * @throws Exception
	 * generate a mutant a time and check whether it can pass the test suite
	 */
	public PolicyMutant repairOneByOne(String policyFileToRepair) throws Exception {
		List<Integer> suspicionRank = new ArrayList<Integer>();
		PolicyMutatorByPosition mutator = new PolicyMutatorByPosition(policyFileToRepair);
		int maxRules = mutator.getPolicy().getChildElements().size();
		for(int bugPosition = -1; bugPosition <= this.getRuleList(mutator).size(); bugPosition++) {
			suspicionRank.add(bugPosition);
		}
		suspicionRank.add(maxRules);
		return repairBySuspicionRank(policyFileToRepair, suspicionRank);
	}
	
	public PolicyMutant repairBySuspicionRank(String policyFileToRepair, List<Integer> suspicionRank) throws Exception {
		PolicyMutant correctMutant = null;
		PolicyMutatorByPosition mutator = new PolicyMutatorByPosition(policyFileToRepair);
		List<Rule> ruleList = getRuleList(mutator);
		int maxRules = mutator.getPolicy().getChildElements().size();
		//bugPosition equals to -1 indicates fault in combining algorithm
		int bugPosition = Integer.MAX_VALUE;
		for(int i = 0; i < suspicionRank.size(); i++) {
			bugPosition = suspicionRank.get(i);
			if(bugPosition == -1) {
				correctMutant = repairBugPositionCombiningAlgorithm(mutator);
				if(correctMutant != null) {
					break;
				}
			} else if (bugPosition == 0) {
				correctMutant = repairBugPositionPolicyTarget(mutator);
				if(correctMutant != null) {
					break;
				}
			} else if (bugPosition == maxRules) {
				correctMutant = repairBugPositionMaxRules(mutator);
				if(correctMutant != null) {
					break;
				}
			} else {
				//take care, ruleIndex begins from one
				if(bugPosition-1 >= ruleList.size()) {
					//this is caused by the RER(createRemoveRuleMutants())
					continue;
				}
				correctMutant = repairBugPositionRules(mutator, ruleList.get(bugPosition-1), bugPosition);
				if(correctMutant != null) {
					break;
				}
			}
		}
		this.setNumTriesBeforSucceed(suspicionRank.indexOf(bugPosition));
		System.out.printf("repaired in the %d-th try\n",this.getNumTriesBeforSucceed());
		return correctMutant;
	}
	
	public int getNumTriesBeforSucceed() {
		return numTriesBeforSucceed;
	}

	private void setNumTriesBeforSucceed(int numTriesBeforSucceed) {
		this.numTriesBeforSucceed = numTriesBeforSucceed;
	}

	private List<Rule> getRuleList(PolicyMutatorByPosition mutator) {
		List<Rule> ruleList = new ArrayList<Rule>();
		for (CombinerElement rule : mutator.getPolicy().getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				ruleList.add((Rule)tree);
			}
		}
		return ruleList;
	}
	
	private PolicyMutant repairBugPositionCombiningAlgorithm(PolicyMutatorByPosition mutator) throws Exception {
		//create mutant methods who's bugPosition == -1
		// CRC
		List<PolicyMutant> mutantList = mutator.createCombiningAlgorithmMutants();
		PolicyMutant correctMutant = find1stCorrectMutant(mutantList);
		return correctMutant;
	}
	
	private PolicyMutant repairBugPositionPolicyTarget(PolicyMutatorByPosition mutator) throws Exception {
		List<PolicyMutant> mutantList = null;
		PolicyMutant correctMutant = null;
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
		return correctMutant;
	}
	
	private PolicyMutant repairBugPositionMaxRules(PolicyMutatorByPosition mutator
			) throws Exception {
		List<Rule> ruleList = getRuleList(mutator);
		List<PolicyMutant> mutantList = null;
		PolicyMutant correctMutant = null;
		int maxRules = mutator.getPolicy().getChildElements().size();
		//RER
		//BECAREFUL!!! bugPosition is maxRules
		for(int ruleIndex = 1; ruleIndex <= ruleList.size(); ruleIndex++) {
			//take care, ruleIndex begins from one
			mutantList = mutator.createRemoveRuleMutants(ruleList.get(ruleIndex-1), ruleIndex, maxRules);
			correctMutant = find1stCorrectMutant(mutantList);
			if(correctMutant != null) {
				return correctMutant;
			}
		}
		return correctMutant;
	}
	
	private PolicyMutant repairBugPositionRules(PolicyMutatorByPosition mutator, 
			Rule myrule, int ruleIndex) throws Exception {
		List<PolicyMutant> mutantList = null;
		PolicyMutant correctMutant = null;
		//CRE
		mutantList = mutator.createRuleEffectFlippingMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//ANR
		mutantList = mutator.createAddNewRuleMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//RTT
		mutantList = mutator.createRuleTargetTrueMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//RTF
		mutantList = mutator.createRuleTargetFalseMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//RCT
		mutantList = mutator.createRuleConditionTrueMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//RCF
		mutantList = mutator.createRuleConditionFalseMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//ANF
		mutantList = mutator.createAddNotFunctionMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//RNF
		mutantList = mutator.createRemoveNotFunctionMutants(myrule,  ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//FCF
		mutantList = mutator.createFlipComparisonFunctionMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//CCF
		mutantList = mutator.createChangeComparisonFunctionMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		//RPTE
		mutantList = mutator.createRemoveParallelTargetElementMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		if(correctMutant != null) {
			return correctMutant;
		}
		return correctMutant;
	}

	public static List<List<String>> getRepairMethodPairList() {
		return PolicyRepairer.repairMethodPairList;
	}

}

