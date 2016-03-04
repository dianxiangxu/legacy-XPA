package org.sag.repair;

import java.util.List;

import org.sag.mutation.PolicyMutant;

public class MutantNode implements Comparable<MutantNode> {
	private PolicyMutant mutant;
	private String testSuiteFile;
	private String faultLocalizaMethod;
	private MutantNode parent;
	private int totalRank;
	
	MutantNode(MutantNode parent, PolicyMutant mutant, String testSuiteFile, String faultLocalizaMethod, int rank) {
		this.setMutant(mutant);
		this.testSuiteFile = testSuiteFile;
		this.faultLocalizaMethod = faultLocalizaMethod;
		this.parent = parent;
		if (parent != null)
			this.totalRank = parent.getTotalRank() + rank;
		else
			this.totalRank = rank;
	}

	PolicyMutant getMutant() {
		return mutant;
	}

	void setMutant(PolicyMutant mutant) {
		this.mutant = mutant;
	}
	
	List<Boolean> getTestResult() throws Exception {
		return PolicyRepairer.getTestResults(testSuiteFile, mutant.getMutantFilePath());
	}
	
	List<Integer> getSuspicionRank() throws Exception {
		return PolicyRepairer.getSuspicionRank(mutant, faultLocalizaMethod, testSuiteFile);
	}


	@Override
	public int compareTo(MutantNode other) {
		// TODO Auto-generated method stub
		return this.getTotalRank() - other.getTotalRank();
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		MutantNode other = (MutantNode) obj;
		return this.getTotalRank() == other.getTotalRank();
	}

	int getTotalRank() {
		return totalRank;
	}
	
	boolean isPromising() {
		//TODO
		return false;
	}
	
	private boolean failedTestsIsSubset() {
		//TODO
		return false;
	}
}
