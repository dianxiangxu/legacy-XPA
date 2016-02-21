package org.sag.repair;

import java.io.File;
import java.util.List;

import org.sag.mutation.PolicyMutant;

public class ExperimentMultiFault {

	public static void main(String[] args) {
		String[] policy = {"conference3", "fedora-rule3", "itrust3", "kmarket-blue-policy", "obligation3", "pluto3"};
		String[] testsuite = {"Basic", "Exclusive", "Pair", "PDpair", "DecisionCoverage", "RuleLevel", "MCDCCoverage"};
		int policyNumber = 0;
		int testsuiteNumber = 6;
		
		String testSuiteSpreadSheetFile = "Experiments" + File.separator + policy[policyNumber]+ File.separator + "test_suites" + File.separator + policy[policyNumber] + "_" + testsuite[testsuiteNumber] + File.separator + policy[policyNumber] + "_" + testsuite[testsuiteNumber] + ".xls";
		String policyMutantSpreadsheetFil = "Experiments" + File.separator + policy[policyNumber] + File.separator + "mutants" + File.separator + policy[policyNumber] + "_mutants.xls";
		String experimentResultFileName = "Experiments" + File.separator + policy[policyNumber] + File.separator + "repair" + File.separator + policy[policyNumber] + "_" + testsuite[testsuiteNumber] + "_repair.csv";
		String policyFile = "Experiments" + File.separator + policy[policyNumber] + File.separator + policy[policyNumber] + ".xml";	
		int numFaults = 2;
		List<PolicyMutant> mutantList = createMultiFaultMutants(policyFile, numFaults, createMutantMethods);	
		String MutantsCSVFileName = createMutantsCSVFile(mutantList);	

	}

}
