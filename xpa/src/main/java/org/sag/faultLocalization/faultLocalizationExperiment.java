package org.sag.faultLocalization;

import org.sag.coverage.*;
import org.sag.policyUtils.PolicyLoader;
import org.wso2.balana.AbstractPolicy;

import java.io.File;
import java.util.List;

/**
 * Created by shuaipeng on 10/11/16.
 */
public class faultLocalizationExperiment {
    public static void main(String[] args) {
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
