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
		System.out.println("test\n");

		String PolicyFilePath = "Experiments//conference3//conference3.xml";
		String testSuiteSpreadSheetFile = "Experiments//conference3//test_suites//conference3_MCDCCoverage_NoError//conference3_MCDCCoverage_NoError.xls";
		ExperimentOnRepair experiment = new ExperimentOnRepair(PolicyFilePath,
				testSuiteSpreadSheetFile);
		long startTime = System.currentTimeMillis();
		experiment.startExperiment("repairRandomOrder");
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

	public void startExperiment(String repairMethod) throws Exception {
		PolicyRepairer repairer = new PolicyRepairer(testSuiteSpreadSheetFile);
		Class<?> cls = repairer.getClass();
		Method method = cls.getMethod(repairMethod, String.class);
		for (PolicyMutant mutant : this.mutantList) {
			PolicyMutant correctedPolicy = (PolicyMutant) method.invoke(repairer, mutant.getMutantFilePath());
//			PolicyMutant correctedPolicy = repairer.repairRandomOrder(mutant
//					.getMutantFilePath());
			Test.showRepairResult(correctedPolicy, mutant.getMutantFilePath());
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
