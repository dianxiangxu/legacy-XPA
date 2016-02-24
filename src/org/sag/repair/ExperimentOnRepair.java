/**
 * 
 */
package org.sag.repair;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.sag.mutation.PolicyMutator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.sag.mutation.PolicyMutant;

/**
 * @author speng
 * 
 */
public class ExperimentOnRepair {
	private String policyFilePath;
	private String testSuiteSpreadSheetFile;
	private List<PolicyMutant> mutantList;
	private static List<Integer> numTriesList;
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
//		String PolicyFilePath = "Experiments//conference3//conference3.xml";
//		String testSuiteSpreadSheetFile = "Experiments//conference3//test_suites//conference3_MCDCCoverage//conference3_MCDCCoverage.xls";
//		String resultsFileName = "Experiments//conference3//conference3_repair_statistics.xls";
//		String PolicyFilePath = "Experiments//fedora-rule3//fedora-rule3.xml";
//		String testSuiteSpreadSheetFile = "Experiments//fedora-rule3//test_suites//fedora-rule3_MCDCCoverage//fedora-rule3_MCDCCoverage.xls";
//		String resultsFileName = "Experiments//fedora-rule3//fedora-rule3_repair_statistics.xls";
		String PolicyFilePath = "Experiments//itrust3//itrust3.xml";
		String testSuiteSpreadSheetFile = "Experiments//itrust3//test_suites//itrust3_MCDCCoverage//itrust3_MCDCCoverage.xls";
		String resultsFileName = "Experiments//itrust3//itrust3_repair_statistics.xls";
		ExperimentOnRepair experiment = new ExperimentOnRepair(PolicyFilePath,
				testSuiteSpreadSheetFile);
		List<List<String>> repairMethodPairList = PolicyRepairer.getRepairMethodPairList();
		List<List<Integer>> allNumTiresLists = new ArrayList<List<Integer>>();
		List<List<PolicyMutant>> allCorrectedPolicyLists  = new ArrayList<List<PolicyMutant>>();
		List<Long> durationList = new ArrayList<Long>();

		for(List<String> repairMethodPair: repairMethodPairList) {
			long startTime = System.currentTimeMillis();
//			experiment.startExperiment("repairRandomOrder", null);
			List<PolicyMutant> correctedPolicyList = experiment.startExperiment(repairMethodPair.get(0), 
					repairMethodPair.get(1));
			allCorrectedPolicyLists.add(correctedPolicyList);
			allNumTiresLists.add(numTriesList);
//			experiment.startExperiment("repairSmartly", "sokal");
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			System.out.printf("running time: " + duration + " milliseconds\n");
			durationList.add(duration);
		}

		
		writeToExcelFile(resultsFileName, repairMethodPairList, 
				experiment.mutantList, allCorrectedPolicyLists, 
				durationList, allNumTiresLists);
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

	public List<PolicyMutant> startExperiment(String repairMethod, String faultLocalizeMethod) throws Exception {
		PolicyRepairer repairer = new PolicyRepairer(testSuiteSpreadSheetFile);
		List<PolicyMutant> correctedPolicyList = new ArrayList<PolicyMutant>();
		numTriesList = new ArrayList<Integer>();
		PolicyMutant correctedPolicy;
		for (PolicyMutant mutant : this.mutantList) {
			System.out.println("bugPosition:\t" + Arrays.toString(mutant.getFaultLocation()));
			switch (repairMethod) {
			case "repairRandomOrder":
				correctedPolicy = repairer.repairRandomOrder(mutant);
				break;
			case "repairOneByOne":
				correctedPolicy = repairer.repairOneByOne(mutant);
				break;
			case "repairSmartly":
				correctedPolicy = repairer.repairSmartly(mutant, faultLocalizeMethod);
				break;
			default:
				throw new IllegalArgumentException("wrong  repairMethod" + repairMethod);
			}			
			correctedPolicyList.add(correctedPolicy);
			numTriesList.add(repairer.getNumTriesBeforSucceed());
//			Test.showRepairResult(correctedPolicy, mutant.getMutantFilePath());
//			System.out.println("==========");
		}
		return correctedPolicyList;
	}
	
	
	private List<PolicyMutant> debug() throws Exception {
		PolicyRepairer repairer = new PolicyRepairer(testSuiteSpreadSheetFile);

		List<PolicyMutant> correctedPolicyList = new ArrayList<PolicyMutant>();
		numTriesList = new ArrayList<Integer>();
		PolicyMutant correctedPolicy;
		
		for (PolicyMutant mutant : this.mutantList) {
			correctedPolicy = repairer.repairRandomOrder(mutant);
			correctedPolicyList.add(correctedPolicy);
			numTriesList.add(repairer.getNumTriesBeforSucceed());
		}
		return correctedPolicyList;
	}
	

	private List<PolicyMutant> createSelectedMutants() throws Exception {
		//comment out some mutants that cannot be repaired
		PolicyMutator policyMutator = new PolicyMutator(this.policyFilePath);
		policyMutator.createPolicyTargetTrueMutants();
		policyMutator.createPolicyTargetFalseMutants();
		policyMutator.createCombiningAlgorithmMutants();
		policyMutator.createRuleEffectFlippingMutants();
		policyMutator.createRemoveRuleMutants();
		policyMutator.createAddNewRuleMutants();
		//policyMutator.createRuleTargetTrueMutants();
		policyMutator.createRuleTargetFalseMutants();
		policyMutator.createRuleConditionTrueMutants();
		policyMutator.createRuleConditionFalseMutants();
		policyMutator.createFirstPermitRuleMutants();
		policyMutator.createFirstDenyRuleMutants();
		policyMutator.createRuleTypeReplacedMutants();
		policyMutator.createFlipComparisonFunctionMutants();
		policyMutator.createAddNotFunctionMutants();
		policyMutator.createRemoveNotFunctionMutants();
//		policyMutator.createRemoveParallelTargetElementMutants();
		policyMutator.createRemoveParallelConditionElementMutants();
		return policyMutator.getMutantList();
	}
	
	public static void writeToExcelFile(String fileName, List<List<String>> repairMethodPairList, 
			List<PolicyMutant> mutantList, List<List<PolicyMutant>> allCorrectedPolicyLists, 
			List<Long> durationList, List<List<Integer>> allNumTiresLists) {
		HSSFWorkbook workBook = new HSSFWorkbook();
		workBook.createSheet("experiment on repair");
		Sheet sheet = workBook.getSheetAt(0);
		writeTitleRow(sheet, 0, mutantList);
		int rowIndex = 1;
		for (int i = 0; i < allCorrectedPolicyLists.size(); i++) {
			writeTestRow(sheet, rowIndex, repairMethodPairList.get(i), mutantList, 
					allCorrectedPolicyLists.get(i), durationList.get(i), allNumTiresLists.get(i));
			rowIndex++;
		}
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			workBook.write(out);
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	private static void writeTitleRow(Sheet sheet, int rowIndex, List<PolicyMutant> mutantList) {
		Row titleRow = sheet.createRow(rowIndex);
		
		Row testRow = sheet.createRow(rowIndex);
		int colIndex = 0;
		titleRow.createCell(colIndex).setCellValue("repair method");
		colIndex++;
		titleRow.createCell(colIndex).setCellValue("fault localizer");
		colIndex++;
		titleRow.createCell(colIndex).setCellValue("time spent(ms)");
		colIndex++;
		titleRow.createCell(colIndex).setCellValue("repaired/total");
		colIndex++;
		for(PolicyMutant mutant: mutantList) {
			testRow.createCell(colIndex).setCellValue(new File(mutant.getMutantFilePath()).getName());
			colIndex++;
		}
	}
	
	private static void writeTestRow(Sheet sheet, int rowIndex, List<String> repairMethodPair, 
			List<PolicyMutant> mutantList, List<PolicyMutant> correctedPolicyList, 
			long duration, List<Integer> numTiresList) {
		Row testRow = sheet.createRow(rowIndex);
		int colIndex = 0;
		for(String str: repairMethodPair) {
			testRow.createCell(colIndex).setCellValue(str);
			colIndex++;
		}

		testRow.createCell(colIndex).setCellValue(duration);
		colIndex++;
		
		int numRepaired = 0, total = 0;
		for(PolicyMutant correctedPolicy: correctedPolicyList) {
			if(correctedPolicy != null) {
				numRepaired++;
			}
			total++;
		}
		testRow.createCell(colIndex).setCellValue(numRepaired + "/" + total);
		colIndex++;
		
		for(int i = 0; i < correctedPolicyList.size(); i++) {
			PolicyMutant correctedPolicy = correctedPolicyList.get(i);
			int numTries = numTiresList.get(i);
			testRow.createCell(colIndex).setCellValue( correctedPolicy == null ? "cannot repair" : numTries + " tries");
			colIndex++;
		}
		int sum = 0;
		for(int numTries: numTiresList) {
			sum += numTries;
		}
		testRow.createCell(colIndex).setCellValue(sum + " total tries");
	}
}
