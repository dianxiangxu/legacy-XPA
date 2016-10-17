package org.sag.coverage;

import org.junit.Ignore;
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
        File csvFile = new File("experiments/conference3/test_suites/conference3_MCDCCoverage/conference3_MCDCCoverage.csv");
        TestSuite testSuite = TestSuite.loadTestSuite(csvFile);
        String fileName = "org/sag/policies/conference3.xml";
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        List<Boolean> results = testSuite.runTests(policy);
        for (boolean result : results)
            assertTrue(result);
    }

    @Test
    @Ignore
    public void runTestOnPolicyKmarketBlue() throws Exception {
        File csvFile = new File("experiments/kmarket-blue-policy/test_suites/kmarket-blue-policy_MCDCCoverage/kmarket-blue-policy_MCDCCoverage.csv");
        TestSuite testSuite = TestSuite.loadTestSuite(csvFile);
        String fileName = "org/sag/policies/alfa.xml";
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        List<Boolean> results = testSuite.runTests(policy);
        for (boolean result : results)
            assertTrue(result);
    }

    @Test
    public void runTestOnPolicySetItrust() throws Exception {
        File csvFile = new File("experiments/itrust3/test_suites/itrust3_MCDCCoverage/itrust3_MCDCCoverage.csv");
        TestSuite testSuite = TestSuite.loadTestSuite(csvFile);
        String fileName = "org/sag/policies/itrustPolicySet.xml";
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        List<Boolean> results = testSuite.runTests(policy);
        for (boolean result : results)
            assertTrue(result);
    }

    @Test
    public void runTestOnPolicySetHL7() throws Exception {
        File csvFile = new File("experiments/HL7/test_suites/manual/HL7.csv");
        TestSuite testSuite = TestSuite.loadTestSuite(csvFile);
        String fileName = "org/sag/policies/HL7.xml";
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        List<Boolean> results = testSuite.runTests(policy);
        for (boolean result : results)
            assertTrue(result);
    }
}