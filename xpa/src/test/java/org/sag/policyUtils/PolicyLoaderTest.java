package org.sag.policyUtils;

import org.junit.Test;
import org.wso2.balana.AbstractPolicy;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * unit test for <code>PolicyLoader</code>
 * Created by shuaipeng on 9/7/16.
 */
public class PolicyLoaderTest {
    @org.junit.Test
    public void getPolicy() throws Exception {
        //Get file from resources folder
        String fileName = "org/sag/policies/conference3.xml";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        assertEquals(policy.getId().toString(), "conference");
    }

    @Test
    public void getPolicySet() throws Exception {
        String fileName = "org/sag/policies/policysetExample.xml";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        assertEquals(policy.getId().toString(), "urn:oasis:names:tc:xacml:3.0:example:policysetid:1");
    }

}