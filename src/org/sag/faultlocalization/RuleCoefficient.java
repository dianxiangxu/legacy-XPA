package org.sag.faultlocalization;

public class RuleCoefficient implements Comparable<RuleCoefficient> {
	private double coefficient;
	private int ruleIndex;
	private int rank;
	
	public RuleCoefficient(double coefficient, int ruleIndex){
		this.coefficient = coefficient;
		this.ruleIndex = ruleIndex;
	}
	
 	public int compareTo(RuleCoefficient other){
 		if (approximateEqual(this.coefficient, other.coefficient))
 			return 0;
 		else  // to sort in reverse order
 			return this.coefficient > other.coefficient? -1: 1;
  	}
 	
 	public double getCoefficient(){
 		return coefficient;
 	}
 	
 	public int getRuleIndex(){
 		return ruleIndex;
 	}
 	
 	public int getRank(){
 		return rank;
 	}
 	
 	public void setRank(int rank){
 		this.rank = rank;
 	}
 	
	public static boolean approximateEqual(double a, double b){
		return Math.abs(a-b)<0.0000000001;
	}

	public boolean approximateEqual(RuleCoefficient other){
		return approximateEqual(coefficient, other.coefficient);
	}

}
