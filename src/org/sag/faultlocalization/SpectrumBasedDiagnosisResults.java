
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
	
	private String methodName;
	private RuleCoefficient[] ruleCoefficients;
	/**
	 * @return the rule indexes ranked by their suspicion
	 */
	public List<Integer> getRuleIndexRankedBySuspicion() {
		List<Integer> ruleIndexRankedBySuspicion = new ArrayList<Integer>();
		for(RuleCoefficient coefficient: ruleCoefficients) {
			ruleIndexRankedBySuspicion.add(coefficient.getRuleIndex() + 1);
		}
		return ruleIndexRankedBySuspicion;
	}

	private double score;
	
	public SpectrumBasedDiagnosisResults(String methodName, double[] s){
		this.methodName = methodName;
		ruleCoefficients = new RuleCoefficient[s.length];
		for (int index=0; index<s.length; index++) {
			ruleCoefficients[index] = new RuleCoefficient(s[index], index);
		}
		Arrays.sort(ruleCoefficients);
		rankSuspicion(); 
	}

	public SpectrumBasedDiagnosisResults(String methodName, double[] s, int bugPosition){
		this(methodName, s);
		if (bugPosition>=0) {
			if (scoreType == ScoreType.PERCENTAGE)
				percentageOfRulesInspected(bugPosition);
			else {
			   if (debuggingStyle ==null) {
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

	private void rankSuspicion(){
		ruleCoefficients[ruleCoefficients.length-1].setRank(ruleCoefficients.length);
		for (int index=ruleCoefficients.length-2; index>=0; index--) {
			if (ruleCoefficients[index].approximateEqual(ruleCoefficients[index+1]))
				ruleCoefficients[index].setRank(ruleCoefficients[index+1].getRank());
			else 
				ruleCoefficients[index].setRank(index+1);
		}
	}
	
	private void percentageOfRulesInspected(int bugPosition){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getRuleIndex()==bugPosition-1) { 
				score = ((double)(ruleCoefficients[index].getRank()))/ruleCoefficients.length;
//System.out.println("\nCalculated accuracy: "+accuracy);				
				return;
			}
		}
	}

	private void numberOfRulesInspected(int bugPosition){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getRuleIndex()==bugPosition-1) { 
				score = ruleCoefficients[index].getRank();
//System.out.println("\nCalculated accuracy: "+accuracy);				
				return;
			}
		}
	}

	private void bottomupDebuggingSaving(int bugPosition){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getRuleIndex()==bugPosition-1) { 
				score = (ruleCoefficients.length - bugPosition+1) - ruleCoefficients[index].getRank() ;
//System.out.println("\nCalculated accuracy: "+accuracy);				
				return;
			}
		}
	}

	private void topdownDebuggingSaving(int bugPosition){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getRuleIndex()==bugPosition-1) { 
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
		for (RuleCoefficient coefficient: ruleCoefficients) {
			System.out.println("\tRule "+(coefficient.getRuleIndex()+1)+" ("+coefficient.getRank()+": "+ String.format( "%.3f", coefficient.getCoefficient())+")");
		}
	}
		
}
