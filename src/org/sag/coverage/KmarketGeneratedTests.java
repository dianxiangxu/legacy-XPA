package org.sag.coverage;
import static org.junit.Assert.*;
import org.sag.coverage.PolicyRunner;
import org.junit.Test;

public class KmarketGeneratedTests {
	PolicyRunner policyRunner;

	public KmarketGeneratedTests(){
		try{
			policyRunner = new PolicyRunner("tests//kmarket-blue-policy.xml");
		}
		catch (Exception e){}
	}

	@Test
	public void test1()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 1", "request1.txt", "deny"));
	}

	@Test
	public void test2()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 2", "request2.txt", "deny"));
	}

}