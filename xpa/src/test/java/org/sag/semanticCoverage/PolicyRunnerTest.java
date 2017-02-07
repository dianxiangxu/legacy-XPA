package org.sag.semanticCoverage;

import org.junit.Test;
import org.sag.policyUtils.PolicyLoader;
import org.wso2.balana.AbstractPolicy;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * tests on {@link PolicyRunner}
 * Created by shuaipeng on 9/8/16.
 */
public class PolicyRunnerTest {
    @Test
    public void runTestOnPolicyConference() throws Exception {
        String testsCSVfileName = "org/sag/policies/conference3/test_suites/conference3_MCDCCoverage/conference3_MCDCCoverage.csv";
        File testsCSVfile = new File(PolicyRunnerTest.class.getClassLoader().getResource(testsCSVfileName).getFile());
        TestSuite testSuite = TestSuite.loadTestSuite(testsCSVfile);
        String fileName = "org/sag/policies/conference3/conference3.xml";
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        List<Boolean> results = testSuite.runTests(policy);
        for (boolean result : results)
            assertTrue(result);
    }

    @Test
    public void runTestOnPolicySetItrust() throws Exception {
        String csvFileName = "org/sag/policies/itrust3/test_suites/itrust3_MCDCCoverage/itrust3_MCDCCoverage.csv";
        File csvFile = new File(PolicyRunnerTest.class.getClassLoader().getResource(csvFileName).getFile());
        TestSuite testSuite = TestSuite.loadTestSuite(csvFile);
        String fileName = "org/sag/policies/itrust3/itrustPolicySet.xml";
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        List<Boolean> results = testSuite.runTests(policy);
        for (boolean result : results)
            assertTrue(result);
    }

    @Test
    public void runTestOnPolicySetHL7() throws Exception {
        String testsCSVfileName = "org/sag/policies/HL7/test_suites/manual/HL7.csv";
        File testsCSVfile = new File(PolicyRunnerTest.class.getClassLoader().getResource(testsCSVfileName).getFile());
        TestSuite testSuite = TestSuite.loadTestSuite(testsCSVfile);
        String fileName = "org/sag/policies/HL7/HL7.xml";
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        List<Boolean> results = testSuite.runTests(policy);
        for (boolean result : results)
            assertTrue(result);
    }
}