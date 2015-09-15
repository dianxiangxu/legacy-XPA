package org.sag.repair;

import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.mutation.PolicyMutator;

public class PolicyRepairer {
	String testSuiteFile = null;
	private PolicyMutator mutator = null;
	public PolicyRepairer(String testSuiteFile) {
		this.testSuiteFile = testSuiteFile;
	}
	public String repair(String policyFileToRepair) {
		PolicyMutator policyMutator = new PolicyMutator(policyFileToRepair);
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
		PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteFile, mutant);
	}

}
