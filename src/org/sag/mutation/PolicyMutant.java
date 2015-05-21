package org.sag.mutation;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import org.sag.coverage.PolicyCoverageFactory;
import org.sag.coverage.PolicySpreadSheetTestRecord;
import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.faultlocalization.SpectrumBasedDiagnosisResults;
import org.sag.faultlocalization.SpectrumBasedFaultLocalizer;

public class PolicyMutant {
	private String number;
	private String mutantFilePath;
	// change to the following if multiple faults need to be considered
	// int[] faultLocations 
	private int faultLocation;

	private String testResult;

	public PolicyMutant(String number, String fileName, int bugPosition){
		this.number = number;
		this.mutantFilePath = fileName;
		this.faultLocation = bugPosition;
	}
	
	public ArrayList<SpectrumBasedDiagnosisResults> run(ArrayList<PolicySpreadSheetTestRecord> testCases) throws Exception{
		PolicyCoverageFactory.init();
		// Test
			//System.out.println(mutantFilePath);
		new PolicySpreadSheetTestSuite(testCases, mutantFilePath).runAllTests();
		return SpectrumBasedFaultLocalizer.applyAllFaultLocalizersToPolicyMutant(faultLocation);
	}

	public String getNumber(){
		return number;
	}

	public String getMutantFilePath(){
		return mutantFilePath;
	}
	
	public String getMutantFilePath(String directory){
		if (directory.equals(""))
			return mutantFilePath;
		else
			return directory+File.separator+mutantFilePath;
	}
	
	public void removeDirectoryFromFilePath(){
		mutantFilePath = new File(mutantFilePath).getName();
	}
	
	public int getFaultLocation(){
		return faultLocation;
	}

	public String getTestResult(){
		return testResult;
	}
	
	public void setTestResult(String result){
		this.testResult = result;
	}
	
	public Vector<Object> getMutantVector(){
		Vector<Object> vector = new Vector<Object>();
		vector.add("");		// sequence number
		vector.add(number);	// mutant name
		vector.add(mutantFilePath);
		vector.add(faultLocation);
		vector.add(testResult);	
//		vector.add(mutantString);
		return vector;
	}
}
