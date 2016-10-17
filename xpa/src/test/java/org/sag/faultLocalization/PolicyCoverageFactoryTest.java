package org.sag.faultLocalization;

import org.junit.Assert;
import org.junit.Test;
import org.sag.coverage.*;
import org.sag.policyUtils.PolicyLoader;
import org.wso2.balana.AbstractPolicy;

import java.io.File;
import java.util.List;

/**
 * This class tests if the coverage information collected by {@link PolicyCoverageFactory} is correct.
 * Created by shuaipeng on 10/14/16.
 */
public class PolicyCoverageFactoryTest {
    @Test
    public void coverageMatrixTest() {
        File csvFile = new File("experiments/conference3/test_suites/conference3_MCDCCoverage/conference3_MCDCCoverage.csv");
        TestSuite testSuite = TestSuite.loadTestSuite(csvFile);
        String fileName = "org/sag/policies/conference3.xml";
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        List<Boolean> results = testSuite.runTests(policy);
        for (boolean result : results)
            Assert.assertTrue(result);
        List<List<Coverage>> coverageMatrix = PolicyCoverageFactory.getCoverageMatrix();
        // convert the coverage matrix to a matrix of integer to ease testing
        int numTests = coverageMatrix.size();
        int numElems = 0;
        for (List<Coverage> row : coverageMatrix)
            numElems = Math.max(numElems, row.size());
        int[][] matrix = new int[numTests][numElems];
        for (int i = 0; i < coverageMatrix.size(); i++) {
            List<Coverage> row = coverageMatrix.get(i);
            for (int j = 0; j < row.size(); j++) {
                Coverage coverage = row.get(j);
                if (coverage instanceof TargetCoverage) {
                    if (((TargetCoverage) coverage).getMatchResult() == TargetCoverage.TargetMatchResult.MATCH)
                        matrix[i][j] = 1;
                } else if (coverage instanceof RuleCoverage) {
                    if (((RuleCoverage) coverage).getRuleDecisionCoverage() == RuleCoverage.RuleDecisionCoverage.EFFECT)
                        matrix[i][j] = 1;
                }
            }
        }
        int[][] expectedMatrix = new int[][]{{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
        Assert.assertEquals(matrix.length, expectedMatrix.length);
        for (int i = 0; i < matrix.length; i++)
            Assert.assertArrayEquals(matrix[i], expectedMatrix[i]);
    }

}
