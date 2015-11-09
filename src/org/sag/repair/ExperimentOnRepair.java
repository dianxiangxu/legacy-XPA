/**
 * 
 */
package org.sag.repair;

import org.sag.mutation.PolicyMutator;

import java.lang.reflect.Method;
import java.util.List;

import org.sag.mutation.PolicyMutant;

/**
 * @author speng
 * 
 */
public class ExperimentOnRepair {
	private String policyFilePath;
	private String testSuiteSpreadSheetFile;
	private List<PolicyMutant> mutantList;
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String PolicyFilePath = "Experiments//conference3//conference3.xml";
		String testSuiteSpreadSheetFile = "Experiments//conference3//test_suites//conference3_MCDCCoverage_NoError//conference3_MCDCCoverage_NoError.xls";
		ExperimentOnRepair experiment = new ExperimentOnRepair(PolicyFilePath,
				testSuiteSpreadSheetFile);
		long startTime = System.currentTimeMillis();
//		experiment.startExperiment("repairRandomOrder", null);
		experiment.startExperiment("repairSmartly", "jaccard");
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.printf("running time: " + duration + " milliseconds\n");
//		System.out.printf("running time: %03d milliseconds\n", duration);
		// System.out.printf("%d:%02d:%02d.%d\n", duration/1e3/3600,
		// duration/1e3%3600/60, duration/1e3%60, duration%1e3);

	}

	/**
	 * @throws Exception 
	 * 
	 */
	public ExperimentOnRepair(String policyFilePath,
			String testSuiteSpreadSheetFile) throws Exception {
		this.policyFilePath = policyFilePath;
		this.testSuiteSpreadSheetFile = testSuiteSpreadSheetFile;
		this.mutantList = createSelectedMutants();
	}

	public void startExperiment(String repairMethod, String faultLocalizeMethod) throws Exception {
		PolicyRepairer repairer = new PolicyRepairer(testSuiteSpreadSheetFile);
		Class<?> cls = repairer.getClass();
		PolicyMutant correctedPolicy;
		if(faultLocalizeMethod != null) {
			Method method = cls.getMethod(repairMethod, String.class, String.class);
			for (PolicyMutant mutant : this.mutantList) {
				correctedPolicy = (PolicyMutant) method.invoke(repairer, mutant.getMutantFilePath(), faultLocalizeMethod);
				Test.showRepairResult(correctedPolicy, mutant.getMutantFilePath());
			}
		} else {
			for (PolicyMutant mutant : this.mutantList) {
				Method method = cls.getMethod(repairMethod, String.class);
				correctedPolicy = (PolicyMutant) method.invoke(repairer, mutant.getMutantFilePath());
				Test.showRepairResult(correctedPolicy, mutant.getMutantFilePath());
			}
		}
	}

	private List<PolicyMutant> createSelectedMutants() throws Exception {
		PolicyMutator policyMutator = new PolicyMutator(this.policyFilePath);
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
		return policyMutator.getMutantList();
	}
}
