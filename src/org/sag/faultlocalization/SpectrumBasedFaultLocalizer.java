package org.sag.faultlocalization;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import org.sag.coverage.PolicyCoverage;
import org.sag.coverage.PolicyCoverageFactory;
import org.sag.coverage.RuleCoverage.RuleDecisionCoverage;

public class SpectrumBasedFaultLocalizer {
	private int[][] ruleMatrix;
	private int[] verdicts;
	
	private double[] s;
	
	public SpectrumBasedFaultLocalizer(ArrayList<PolicyCoverage> policyCoverages){
		int numberOfRules= getNumberOfRules(policyCoverages);
		ruleMatrix = new int[policyCoverages.size()][numberOfRules];
		for (int testNo=0; testNo<ruleMatrix.length; testNo++){
			PolicyCoverage policyCoverage = policyCoverages.get(testNo);
			int numberOfCoveredRules = policyCoverage.getRuleCoverages().size();
			for (int ruleNo=0; ruleNo<numberOfRules; ruleNo++) {
				if (ruleNo<numberOfCoveredRules){
//					ruleMatrix[testNo][ruleNo] = policyCoverage.getRuleCoverages().get(ruleNo).getRuleDecisionCoverage()!=RuleDecisionCoverage.INDETERMINATE? 1: 0; // Reachable 
					ruleMatrix[testNo][ruleNo] = policyCoverage.getRuleCoverages().get(ruleNo).getRuleDecisionCoverage()==RuleDecisionCoverage.EFFECT? 1: 0; // Firable
//					ruleMatrix[testNo][ruleNo] = policyCoverage.getRuleCoverages().get(ruleNo).getTargetConditionCoverage(); // Target/Condition Coverage. 0/1/2
					//System.out.println("Test Number " + testNo + " Rule Number " + ruleNo + " Score = " + ruleMatrix[testNo][ruleNo]);
				} else {
					ruleMatrix[testNo][ruleNo] = 0;		
				}
			}
		}
		verdicts = new int[policyCoverages.size()];
		for (int testNo=0; testNo<ruleMatrix.length; testNo++){
			PolicyCoverage policyCoverage = policyCoverages.get(testNo);
			verdicts[testNo] = policyCoverage.getDecision() == policyCoverage.getOracle()? 0: 1; 
		}
		printMatrix(ruleMatrix);
		System.out.println(Arrays.toString(verdicts));
		s = new double[numberOfRules];
	}
	
    private static void printMatrix(int[][] matrix) {
    	for (int i = 0; i < matrix.length; i++)
    		System.out.println(i + ": " + Arrays.toString(matrix[i]));
    }
    
	private int getNumberOfRules(ArrayList<PolicyCoverage> policyCoverages){
		if (policyCoverages.size()>0)
			return policyCoverages.get(0).getNumberOfRules();
		else
			return 1;
	}
	
	public static ArrayList<SpectrumBasedDiagnosisResults> applyAllFaultLocalizers(){
		ArrayList<SpectrumBasedDiagnosisResults> allResults = new ArrayList<SpectrumBasedDiagnosisResults>();
		SpectrumBasedFaultLocalizer faultLocalizer = new SpectrumBasedFaultLocalizer(PolicyCoverageFactory.policyCoverages);
		faultLocalizer.jaccard();
		allResults.add(new SpectrumBasedDiagnosisResults("Jaccard", faultLocalizer.s));
		faultLocalizer.tarantula();
		allResults.add(new SpectrumBasedDiagnosisResults("Tarantula", faultLocalizer.s));
		faultLocalizer.ochiai();
		allResults.add(new SpectrumBasedDiagnosisResults("Ochiai", faultLocalizer.s));
		faultLocalizer.ochiai2();
		allResults.add(new SpectrumBasedDiagnosisResults("Ochiai2", faultLocalizer.s));
		return allResults;
	}

	public static ArrayList<SpectrumBasedDiagnosisResults> applyAllFaultLocalizersToPolicyMutant(int bugPosition){
		ArrayList<SpectrumBasedDiagnosisResults> allResults = new ArrayList<SpectrumBasedDiagnosisResults>();
		SpectrumBasedFaultLocalizer faultLocalizer = new SpectrumBasedFaultLocalizer(PolicyCoverageFactory.policyCoverages);
		faultLocalizer.jaccard();
		allResults.add(new SpectrumBasedDiagnosisResults("Jaccard", faultLocalizer.s, bugPosition));
		faultLocalizer.tarantula();
		allResults.add(new SpectrumBasedDiagnosisResults("Tarantula", faultLocalizer.s, bugPosition));
		faultLocalizer.ochiai();
		allResults.add(new SpectrumBasedDiagnosisResults("Ochiai", faultLocalizer.s, bugPosition));
		faultLocalizer.ochiai2();
		allResults.add(new SpectrumBasedDiagnosisResults("Ochiai2", faultLocalizer.s, bugPosition));
		faultLocalizer.cbi();
		allResults.add(new SpectrumBasedDiagnosisResults("cbi", faultLocalizer.s, bugPosition));
		faultLocalizer.hamann();
		allResults.add(new SpectrumBasedDiagnosisResults("hamann", faultLocalizer.s, bugPosition));
		faultLocalizer.simpleMatching();
		allResults.add(new SpectrumBasedDiagnosisResults("simpleMatching", faultLocalizer.s, bugPosition));
		faultLocalizer.sokal();
		allResults.add(new SpectrumBasedDiagnosisResults("sokal", faultLocalizer.s, bugPosition));
		faultLocalizer.naish2();
		allResults.add(new SpectrumBasedDiagnosisResults("naish2", faultLocalizer.s, bugPosition));
		faultLocalizer.goodman();
		allResults.add(new SpectrumBasedDiagnosisResults("goodman", faultLocalizer.s, bugPosition));
		faultLocalizer.sorensenDice();
		allResults.add(new SpectrumBasedDiagnosisResults("sorensenDice", faultLocalizer.s, bugPosition));
		faultLocalizer.anderberg();
		allResults.add(new SpectrumBasedDiagnosisResults("anderberg", faultLocalizer.s, bugPosition));
		faultLocalizer.euclid();
		allResults.add(new SpectrumBasedDiagnosisResults("euclid", faultLocalizer.s, bugPosition));
		faultLocalizer.rogersTanimoto();
		allResults.add(new SpectrumBasedDiagnosisResults("rogersTanimoto", faultLocalizer.s, bugPosition));
		
/*		for (SpectrumBasedDiagnosisResults results: allResults){
			results.printResults();
		}
*/		
		return allResults;
	}
	
	public static SpectrumBasedDiagnosisResults applyOneFaultLocalizerToPolicyMutant(String faultLocalizeMethod) 
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		SpectrumBasedFaultLocalizer faultLocalizer = new SpectrumBasedFaultLocalizer(PolicyCoverageFactory.policyCoverages);
		Class<?> cls = faultLocalizer.getClass();
		Method method = cls.getDeclaredMethod(faultLocalizeMethod);
		method.invoke(faultLocalizer);
		SpectrumBasedDiagnosisResults res = new SpectrumBasedDiagnosisResults(faultLocalizeMethod, faultLocalizer.s);
		return res;
	}

	// aep = a10: passed test in which rule was executed
	// aef = a11: failed test in which rule was executed
	// anp = a00: passed test in which rule was not executed
	// anf = a01: failed test in which rule was not executed

	private void cbi(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			int a00 = apq(0,0,j);
			s[j] = a11+a10!=0? ((double)a11) / ((double)(a11+a10)) - ((double)(a11+a01))/((double)a11+a10+a01+a00): 0;
		}
	}

	private void hamann(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			int a00 = apq(0,0,j);
			double d = (double)a11+a10+a01+a00;
			s[j] = d!=0? (a11+a00-a01-a10)/d: 0;
		}
	}

	private void simpleMatching(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			int a00 = apq(0,0,j);
			double d = (double)a11+a10+a01+a00;
			s[j] = d!=0? (a11+a00)/d: 0;
		}
	}

	private void sokal(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			int a00 = apq(0,0,j);
			int n = 2*(a11+a00);
			int d = n+a10+a01;
			s[j] = d!=0? ((double)n)/((double)d): 0;
		}
	}

	private void rogersTanimoto(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			int a00 = apq(0,0,j);
			int n = a11+a00;
			int d = n+2*(a10+a01);
			s[j] = d!=0? ((double)n)/((double)d): 0;
		}
	}

	private void euclid(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a00 = apq(0,0,j);
			s[j] = Math.sqrt(a11+a00);
		}
	}

	private void anderberg(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			s[j] = a11+2*a01+2*a10!=0 ? ((double)a11) / ((double)(a11+2*a01+2*a10)): 0;
		}
	}

	private void sorensenDice(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			s[j] = 2*a11+a01+a10!=0 ? ((double)a11) / ((double)(2*a11+a01+a10)): 0;
		}
	}

	private void goodman(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			double d = (double)(2*a11+a01+a10);
			s[j] = d!=0 ? ((double)2*a11-a01-a10) / d: 0;
		}
	}

	private void jaccard(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			s[j] = a11+a01+a10!=0 ? ((double)a11) / ((double)(a11+a01+a10)): 0;
		}
	}

	private void naish2(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a10 = apq(1,0,j);
			int a00 = apq(0,0,j);
			s[j] = a11-((double)a10/((double)a10+a00+1));
		}
	}

	private void tarantula(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			int a00 = apq(0,0,j);
			double x =  a11+a01!=0 ? ((double)a11) / ((double)(a11+a01)): 0;
			double y = a10+a00!=0? ((double)a10) / ((double)(a10+a00)): 0;
			s[j] = x+y!=0 ? x/ (x+y): 0;
		}
	}

	private void ochiai(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			double d = Math.sqrt((a11+a01)*(a11+a10));
			s[j] = d!=0 ? ((double)a11)/d: 0;
		}
	}

	private void ochiai2(){
		for (int j=0; j<ruleMatrix[0].length; j++) {
			int a11 = apq(1,1,j);
			int a01 = apq(0,1,j);
			int a10 = apq(1,0,j);
			int a00 = apq(0,0,j);
			double d = Math.sqrt((a11+a10)*(a00+a01)*(a11+a01)*(a10+a00));
			s[j] = d!=0 ? ((double)a11*a00)/d: 0;
		}
	}

/*
	private int apq(int p, int q, int j){
		int sum=0;
		for (int testIndex=0; testIndex<ruleMatrix.length; testIndex++){
			if (ruleMatrix[testIndex][j]==p && verdicts[testIndex]==q)
				sum++;
		}
		return sum;
	}
*/
	
	// 1/13/15 Jimmy
	private int apq(int p, int q, int j) {
		int sum = 0;
		for (int testIndex=0; testIndex<ruleMatrix.length; testIndex++) {
			if (p==0) {
				if (ruleMatrix[testIndex][j]==p && verdicts[testIndex]==q)
					sum = sum + 1; // Target&&Condition both Not Evaluated (Score=1).
			} else {
				if (verdicts[testIndex]==q) {
					if (ruleMatrix[testIndex][j]==1)
					sum += 1;
					if (ruleMatrix[testIndex][j]==2)
					sum += 2;
				}
			}
		}
		return sum;
	}
		
}
