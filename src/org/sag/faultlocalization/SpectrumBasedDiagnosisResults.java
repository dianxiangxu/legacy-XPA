
package org.sag.faultlocalization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpectrumBasedDiagnosisResults {
	public static enum DebuggingStyle {TOPDOWN, BOTTOMUP};
	public static enum ScoreType {COUNT, PERCENTAGE};

//	public DebuggingStyle debuggingStyle = DebuggingStyle.TOPDOWN;
	public DebuggingStyle debuggingStyle = null;
	public ScoreType scoreType = ScoreType.COUNT;
	private double score;
	private String methodName;
	private PolicyElementCoefficient[] ruleCoefficients;
	/**
	 * @return the rule indexes ranked by their suspicion
	 */
	public List<Integer> getRuleIndexRankedBySuspicion() {
		List<Integer> ruleIndexRankedBySuspicion = new ArrayList<Integer>();
		for(PolicyElementCoefficient coefficient: ruleCoefficients) {
			ruleIndexRankedBySuspicion.add(coefficient.getElementIndex());
		}
		return ruleIndexRankedBySuspicion;
	}

	public SpectrumBasedDiagnosisResults(String methodName, double[] s){
		this.methodName = methodName;
		ruleCoefficients = new PolicyElementCoefficient[s.length];
		for (int index=0; index<s.length; index++) {
			ruleCoefficients[index] = new PolicyElementCoefficient(s[index], index);
		}
		Arrays.sort(ruleCoefficients);
		rankSuspicion(); 
	}
	/**
	 * call other methods to set the score
	 * @param methodName
	 * @param s, an array of coefficient of rules
	 * @param bugPositions
	 */
	public SpectrumBasedDiagnosisResults(String methodName, double[] s,
			int[] bugPositions) {
		this(methodName, s);
		if (bugPositions.length == 1) {
			int bugPosition = bugPositions[0];
			if (bugPosition >= 0) {
				if (scoreType == ScoreType.PERCENTAGE)
					percentageOfRulesInspected(bugPosition);
				else {
					if (debuggingStyle == null) {
						numberOfRulesInspected(bugPosition);
					} else {
						if (debuggingStyle == DebuggingStyle.TOPDOWN)
							topdownDebuggingSaving(bugPosition);
						else
							bottomupDebuggingSaving(bugPosition);
					}
				}
			}
		}
	}

	/**
	 * set rank for each element in ruleCoefficients
	 * note that if two element in ruleCoefficients have almost the same coefficient,
	 * set their rank as the same. For example, [0.9, 0.9, 0.8, 0.7, 0.7] will have rank
	 * [2, 2, 3, 5, 5]. The rank is not [1, 1, 3, 4, 4] because we want to know worst 
	 * case performance.
	 */
	private void rankSuspicion(){
		ruleCoefficients[ruleCoefficients.length-1].setRank(ruleCoefficients.length);
		for (int index=ruleCoefficients.length-2; index>=0; index--) {
			if (ruleCoefficients[index].approximateEqual(ruleCoefficients[index+1]))
				ruleCoefficients[index].setRank(ruleCoefficients[index+1].getRank());
			else 
				ruleCoefficients[index].setRank(index+1);
		}
	}
	/**
	 * set score for scoreType == ScoreType.PERCENTAGE
	 * @param bugPosition
	 */
	private void percentageOfRulesInspected(int bugPosition){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getElementIndex()==bugPosition) { 
				score = ((double)(ruleCoefficients[index].getRank()))/ruleCoefficients.length;
//System.out.println("\nCalculated accuracy: "+accuracy);				
				return;
			}
		}
	}
	/**
	 * set score for scoreType == ScoreType.COUNT and debuggingStyle ==null
	 * @param bugPosition
	 */
	private void numberOfRulesInspected(int bugPosition){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getElementIndex()==bugPosition) { 
				score = ruleCoefficients[index].getRank();
//System.out.println("\nCalculated accuracy: "+accuracy);				
				return;
			}
		}
	}

	private void bottomupDebuggingSaving(int bugPosition){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getElementIndex()==bugPosition) { 
				score = (ruleCoefficients.length - bugPosition+1) - ruleCoefficients[index].getRank() ;
//System.out.println("\nCalculated accuracy: "+accuracy);				
				return;
			}
		}
	}

	private void topdownDebuggingSaving(int bugPosition){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getElementIndex()==bugPosition) { 
				score = bugPosition - ruleCoefficients[index].getRank();
//System.out.println("\nCalculated accuracy: "+accuracy);				
				return;
			}
		}
	}

	
/*	
	private void calculateAccuracy(int bugPosition){
		for (int index=0; index < results.length; index++){
			if (results[index].getRuleIndex()==bugPosition-1) { 
				int first = index;
				while (first>0 && approximateEqual(results[first].getCoefficient(),results[first-1].getCoefficient())){
					first--;
				}
				int last = index;
				while (last<results.length-1 && approximateEqual(results[last].getCoefficient(),results[last+1].getCoefficient())){
					last++;
				}
				accuracy += ((double)(first+last))/2.0;
//System.out.println("\nCalculated accuracy: "+accuracy);				
// an alternative: 
// accuracy = begin;				
				return;
			}
		}
	}
*/
	
	public double getScore(){
		return score;
	}
	
	public String getMethodName(){
		return methodName;
	}

	public void printCoefficients(){
		System.out.println(methodName);
		for (PolicyElementCoefficient coefficient: ruleCoefficients) {
			System.out.println("\tRule "+(coefficient.getElementIndex()+1)+" ("+coefficient.getRank()+": "+ String.format( "%.3f", coefficient.getCoefficient())+")");
		}
	}
		
}
