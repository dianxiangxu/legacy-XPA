package org.sag.faultlocalization;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.sag.coverage.PolicySpreadSheetTestRecord;
import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.mutation.PolicyMutant;
import org.sag.mutation.PolicySpreadSheetMutantSuite;


public class FaultLocalizationExperiment {
	
	private ArrayList<PolicyMutant> policyMutants; 
	//one dimension for different policy mutants, the other for different fault localizers
	private ArrayList<ArrayList<SpectrumBasedDiagnosisResults>> experimentResults;
	
	public FaultLocalizationExperiment(String testSuiteSpreadSheetFile, String policyMutantSpreadsheetFile, String experimentResultFileName) throws Exception{
		ArrayList<PolicySpreadSheetTestRecord> testSuite = PolicySpreadSheetTestSuite.readTestSuite(testSuiteSpreadSheetFile);
		experimentResults = new ArrayList<ArrayList<SpectrumBasedDiagnosisResults>>();
		policyMutants = PolicySpreadSheetMutantSuite.readMutantSuite(policyMutantSpreadsheetFile);
		for (PolicyMutant policyMutant: policyMutants ){
			try {
				experimentResults.add(policyMutant.run(testSuite));
			}
			catch (Exception e) {
				e.printStackTrace();
			}		
		}
		if (experimentResults.size()>0) {
			writeExperimentResult(experimentResultFileName);
		}
	}

	public void writeExperimentResult(String fileName){
		HSSFWorkbook workBook = new HSSFWorkbook();
		workBook.createSheet("mutation testing results");
		Sheet sheet = workBook.getSheetAt(0);
		Row row = sheet.createRow(0);
		writeTitleRow(row, experimentResults.get(0));
		for (int mutantIndex =0; mutantIndex<experimentResults.size(); mutantIndex++){
			row = sheet.createRow(mutantIndex+1);
			writeMutantResultsRow(row, policyMutants.get(mutantIndex).getNumber(), experimentResults.get(mutantIndex)); 
		}
		writeAverageRow(sheet.createRow(experimentResults.size()+2));
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			workBook.write(out);
			out.close();
		}
		catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	private void writeMutantResultsRow(Row row,String mutantNumber, ArrayList<SpectrumBasedDiagnosisResults> results){
		Cell cell = row.createCell(0);
		cell.setCellValue(mutantNumber);
		for (int i=0; i<results.size(); i++){
			cell = row.createCell(i+1);
			cell.setCellValue(results.get(i).getScore());
		}		
	}

	private void writeAverageRow(Row row){
		double[] totals = new double[(experimentResults.get(0).size())];
		for (int i=0; i<totals.length; i++){
			totals[i]=0.0;
		}
		for (int mutantIndex =0; mutantIndex<experimentResults.size(); mutantIndex++){
			ArrayList<SpectrumBasedDiagnosisResults> results = experimentResults.get(mutantIndex);
			for (int i=0; i<results.size(); i++){
				totals[i]+= results.get(i).getScore();
			}
		}
		Cell cell = row.createCell(0);
		cell.setCellValue("Average");
		for (int i=0; i<totals.length; i++){
			cell = row.createCell(i+1);
			if (experimentResults.size()>0){
				cell.setCellValue(String.format( "%.3f", totals[i]/experimentResults.size()));
			}
		}		
	}

	private void writeTitleRow(Row row, ArrayList<SpectrumBasedDiagnosisResults> results){
		Cell cell = row.createCell(0);
		cell.setCellValue("");
		for (int i=0; i<results.size(); i++){
			cell = row.createCell(i+1);
			cell.setCellValue(results.get(i).getMethodName());
		}		
	}
	
	public static void main(String[] args) throws Exception{
		
		// Exclusive - kmarket-blue-policy 
//		new FaultLocalizationExperiment("Experiments//kmarket-blue-policy//test_suites//kmarket-blue-policy_Exclusive//kmarket-blue-policy_Exclusive.xls",
//										"Experiments//kmarket-blue-policy//mutants//kmarket-blue-policy_mutants.xls",
//										"Experiments//kmarket-blue-policy//fault-localization//Exclusive_experiment.xls");
		// Basic - conference3
//		new FaultLocalizationExperiment("Experiments//conference3//test_suites//conference3_Basic//conference3_Basic.xls",
//				"Experiments//conference3//mutants//conference3_mutants.xls",
//				"Experiments//conference3//fault-localization//conference3_Basic_Fault-localiazation.xls");
		
		// General
		String[] policy = {"conference3", "fedora-rule3", "itrust3", "kmarket-blue-policy", "obligation3", "pluto3"};
		String[] testsuite = {"Basic", "Exclusive", "Pair", "PDpair", "RuleCoverage", "RuleLevel"};
		int policyNumber = 3;
		int testsuiteNumber = 5;
		
		new FaultLocalizationExperiment("Experiments//" + policy[policyNumber]+ "//test_suites//" + policy[policyNumber] + "_" + testsuite[testsuiteNumber] + "//" + policy[policyNumber] + "_" + testsuite[testsuiteNumber] + ".xls",
				"Experiments//" + policy[policyNumber] + "//mutants//" + policy[policyNumber] + "_mutants.xls",
				"Experiments//" + policy[policyNumber] + "//fault-localization//" + policy[policyNumber] + "_" + testsuite[testsuiteNumber] + "_fault-localiazation.xls");
		
	}
	

}
