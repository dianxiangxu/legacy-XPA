package org.sag.faultLocalization;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.sag.coverage.*;
import org.sag.mutation.Mutant;
import org.sag.policyUtils.PolicyLoader;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shuaipeng on 10/11/16.
 */
public class faultLocalizationExperiment {
    public static void main(String[] args) throws IOException {
        String mutantsCSVfileName = "org/sag/policies/conference3/mutants/mutants.csv";
        File mutantsCSVfile = new File(faultLocalizationExperiment.class.getClassLoader().getResource(mutantsCSVfileName).getFile());
        String faultLocalizeResultsFile = "experiments/conference3/fault-localization/conference3_faultLocalization.csv";
        FileUtils.forceMkdir(new File(FilenameUtils.getPath(faultLocalizeResultsFile)));
        CSVWriter writer = new CSVWriter(new FileWriter(faultLocalizeResultsFile), ',');
        List<String> faultLocalizeMethods = Arrays.asList("jaccard","tarantula","ochiai","ochiai2","cbi","hamann",
                "simpleMatching","sokal","naish2","goodman","sorensenDice","anderberg","euclid","rogersTanimoto");
        writeCSVTitleRow(writer, faultLocalizeMethods);

        try {
            List<Mutant> mutants = PolicyLoader.readMutantsCSVFile(mutantsCSVfile);
            File testsCSVfile = new File("experiments/conference3/test_suites/conference3_MCDCCoverage/conference3_MCDCCoverage.csv");
            TestSuite testSuite = TestSuite.loadTestSuite(testsCSVfile);
            for (Mutant mutant: mutants) {
                System.out.println(mutant.getName());
                if (mutant.getName().trim().equals("MUTANT RTT1_1"))
                    System.out.println("");
                List<Boolean> results = testSuite.runTests(mutant);
                if (booleanListAnd(results))
                    continue;
                List<List<Coverage>> coverageMatrix = PolicyCoverageFactory.getCoverageMatrix();
                SpectrumBasedFaultLocalizer faultLocalizer = new SpectrumBasedFaultLocalizer(coverageMatrix);
                List<String> aveNumElemToInspcetList = new ArrayList<>();
                for (String faultLocalizeMethod: faultLocalizeMethods) {
                    SpectrumBasedDiagnosisResults diagnosisResults = new SpectrumBasedDiagnosisResults(
                            faultLocalizer.applyFaultLocalizeMethod(faultLocalizeMethod));
                    List<Integer> faultLocations = mutant.getFaultLocations();
                    aveNumElemToInspcetList.add(Double.toString(diagnosisResults.getAverageNumberOfElementsToInspect(faultLocations)));
                }
                String mutantName = mutant.getName();
                writeCSVResultRow(writer, mutantName, aveNumElemToInspcetList);
//                SpectrumBasedDiagnosisResults diagnosisResults = new SpectrumBasedDiagnosisResults(faultLocalizer.naish2());
//                System.out.println(diagnosisResults.getIndexRankedBySuspicion());
//                List<Integer> faultLocations = ((Mutant)mutant).getFaultLocations();
//                System.out.println(faultLocations.toString());
//                System.out.println(diagnosisResults.getAverageNumberOfElementsToInspect(faultLocations));
            }
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

    private static boolean booleanListAnd(List<Boolean> booleanList) {
        for (boolean b: booleanList)
            if (!b)
                return false;
        return true;
    }

    private static void writeCSVTitleRow(CSVWriter writer, List<String> faultLocalizeMethods) {
        String[] titles = new String[faultLocalizeMethods.size() + 1];
        titles[0] = "mutant";
        int index = 1;
        for (String faultLocalizeMethod : faultLocalizeMethods) {
            titles[index] = faultLocalizeMethod;
            index++;
        }
        writer.writeNext(titles);
    }

    private static void writeCSVResultRow(CSVWriter writer, String mutantName, List<String> columnValue)  {
        String[] entry = new String[columnValue.size() + 1];
        entry[0] = mutantName;
        int index = 1;
        for (String duration: columnValue) {
            entry[index] = duration;
            index ++;
        }
        writer.writeNext(entry);
        writer.flushQuietly();
    }

    private void runTest() throws ParserConfigurationException, ParsingException, SAXException, IOException {
//        File csvFile = new File("experiments/conference3/test_suites/conference3_MCDCCoverage/conference3_MCDCCoverage.csv");
        File csvFile = new File("experiments/HL7/test_suites/manual/HL7.csv");
        TestSuite testSuite = TestSuite.loadTestSuite(csvFile);
//        File file = new File("/media/shuaipeng/data/XPA/xpa/src/main/resources/org/sag/policies/conference3.xml");
        File file = new File("/media/shuaipeng/data/XPA/xpa/src/main/resources/org/sag/policies/HL7.xml");
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        List<Boolean> results = testSuite.runTests(policy);
        System.out.println(results.toString());
        List<List<Coverage>> coverageMatrix = PolicyCoverageFactory.getCoverageMatrix();
        System.out.println("there are " + coverageMatrix.size() + " tests");
        for (List<Coverage> row : coverageMatrix)
            System.out.println(row.size());
        for (int i = 0; i < coverageMatrix.size(); i++) {
            System.out.println("test No. " + i);
            List<Coverage> row = coverageMatrix.get(i);
            for (Coverage coverage : row) {
                if (coverage instanceof RuleCoverage) {
                    System.out.println("RuleCoverage");
                    RuleCoverage ruleCoverage = (RuleCoverage) coverage;
                    System.out.println(ruleCoverage.getRuleId());
                    System.out.println(ruleCoverage.getCombinedCoverage());
                    System.out.println(ruleCoverage.getRuleDecisionCoverage());
                } else if (coverage instanceof TargetCoverage) {
                    System.out.println("TargetCoverage");
                    TargetCoverage targetCoverage = (TargetCoverage) coverage;
                    System.out.println(targetCoverage.getMatchResult());
                }
            }
        }

        SpectrumBasedFaultLocalizer faultLocalizer = new SpectrumBasedFaultLocalizer(PolicyCoverageFactory.getCoverageMatrix());

    }
}
