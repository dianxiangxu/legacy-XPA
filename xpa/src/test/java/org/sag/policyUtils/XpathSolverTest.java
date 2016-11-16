package org.sag.policyUtils;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * tests {@link XpathSolver}
 * Created by shuaipeng on 10/20/16.
 */
public class XpathSolverTest {
    @Test
    public void getEntryListAbsoluteXPathTest() throws ParserConfigurationException, IOException, SAXException, ParsingException {
//        String fileName = "org/sag/policies/conference3.xml";
        String fileName = "org/sag/policies/HL7/HL7.xml";
        ClassLoader classLoader = XpathSolver.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        // by load the policy and then encode it back to string, we replace the namespace declaration with default namespace declaration
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        InputStream stream = IOUtils.toInputStream(policy.encode(), Charset.defaultCharset());
        Document doc = PolicyLoader.getDocument(stream, false);
        List<String> list = XpathSolver.getEntryListAbsoluteXPath(doc);
//        for (String entry: list)
//            System.out.println("\"" + entry + "\",");

        String[] expected = new String[]{
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Target[1]",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/Target[1]",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.updateNote']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.safetyHarness']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments']/Target[1]",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments.createAssessment']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments.safetyHarness']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory']/Target[1]",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory.readPaymentHistory']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory.safetyHarness']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/Target[1]",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/Target[1]",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords.readMedicalRecord']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords.explicitDenyAccess']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess']/Target[1]",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess.withAdditionalAuthority']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess']/Target[1]",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess.accessMedicalRecord']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/Target[1]",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.alternateRead']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.alternateUpdate']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.safetyHarness']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan']/Target[1]",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan.Id_10']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan.safetyHarness']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/Target[1]",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.clinicalObjectAccess']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.safetyHarness']",
        };

        Assert.assertArrayEquals(list.toArray(), expected);
    }

    @Test
    public void getEntryListRelativeXPathTestConference3() throws ParserConfigurationException, IOException, SAXException, ParsingException {
        String fileName = "org/sag/policies/conference3/conference3.xml";

        String[] expected = new String[]{
                "//Policy[@PolicyId='conference']/Target[1]",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule0']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule1']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule2']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule3']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule4']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule5']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule6']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule7']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule8']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule9']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule10']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule11']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule12']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule13']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule14']"
        };

        getEntryListRelativeXPathTest(fileName, expected);
    }

    @Test
    public void getEntryListRelativeXPathTestHL7() throws ParserConfigurationException, IOException, SAXException, ParsingException {
        String fileName = "org/sag/policies/HL7/HL7.xml";

        String[] expected = new String[]{
                "//PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Target[1]",
                "//Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/Target[1]",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote']",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.updateNote']",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.safetyHarness']",
                "//Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments']/Target[1]",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments.createAssessment']",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments.safetyHarness']",
                "//Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory']/Target[1]",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory.readPaymentHistory']",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory.safetyHarness']",
                "//PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/Target[1]",
                "//Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/Target[1]",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords.readMedicalRecord']",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords.explicitDenyAccess']",
                "//Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess']/Target[1]",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess.withAdditionalAuthority']",
                "//Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess']/Target[1]",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess.accessMedicalRecord']",
                "//Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/Target[1]",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.alternateRead']",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.alternateUpdate']",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.safetyHarness']",
                "//Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan']/Target[1]",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan.Id_10']",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan.safetyHarness']",
                "//Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/Target[1]",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.clinicalObjectAccess']",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess']",
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.safetyHarness']",
        };

        getEntryListRelativeXPathTest(fileName, expected);
    }

    private void getEntryListRelativeXPathTest(String fileName, String[] expected) throws ParserConfigurationException, IOException, SAXException, ParsingException {
        ClassLoader classLoader = XpathSolver.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        // by load the policy and then encode it back to string, we replace the namespace declaration with default namespace declaration
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        InputStream stream = IOUtils.toInputStream(policy.encode(), Charset.defaultCharset());
        Document doc = PolicyLoader.getDocument(stream, false);
        List<String> list = XpathSolver.getEntryListRelativeXPath(doc);
//        for (String entry: list)
//            System.out.println("\"" + entry + "\",");
        Assert.assertArrayEquals(list.toArray(), expected);
    }

    @Test
    public void evaluateXpathTestConference3() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ParsingException {
        String fileName = "org/sag/policies/conference3/conference3.xml";
        List<String> xpathList = Arrays.asList(
                "//Policy[@PolicyId='conference']/Target[1]",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule0']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule1']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule2']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule3']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule4']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule5']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule6']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule7']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule8']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule9']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule10']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule11']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule12']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule13']",
                "//Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule14']"
        );
        List<String> expectedXpathList = Arrays.asList(
                "/Policy[@PolicyId='conference']/Target[1]",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule0']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule1']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule2']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule3']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule4']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule5']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule6']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule7']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule8']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule9']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule10']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule11']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule12']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule13']",
                "/Policy[@PolicyId='conference']/Rule[@RuleId='urn:oasis:names:tc:xacml:1.0:Rule14']"
        );
        evaluateXpathTest(fileName, xpathList, expectedXpathList);
    }

    @Test
    public void evaluateXpathTestHL7() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ParsingException {
        String fileName = "org/sag/policies/HL7/HL7.xml";
        List<String> xpathList = Arrays.asList(
                "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess']",
                "//Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/Target[1]"
        );
        List<String> expectedXpathList = Arrays.asList(
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess']",
                "/PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/Target[1]"
        );
        evaluateXpathTest(fileName, xpathList, expectedXpathList);
    }

    private void evaluateXpathTest(String fileName, List<String> xpathList, List<String> expectedXpathList) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ParsingException {
        ClassLoader classLoader = XpathSolver.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        // by load the policy and then encode it back to string, we replace the namespace declaration with default namespace declaration
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        InputStream stream = IOUtils.toInputStream(policy.encode(), Charset.defaultCharset());
        Document doc = PolicyLoader.getDocument(stream, false);

        //Evaluate XPath against Document itself
        XPath xPath = XPathFactory.newInstance().newXPath();
        for (int i = 0; i < xpathList.size(); i++) {
            String xpathString = xpathList.get(i);
            String expectedXPathString = expectedXpathList.get(i);
            // get node by xpath string
            NodeList nodes = (NodeList) xPath.evaluate(xpathString,
                    doc.getDocumentElement(), XPathConstants.NODESET);
            // the xpath should identify a unique node
            Assert.assertEquals(1, nodes.getLength());
            Node node = nodes.item(0);
//            System.out.println(XpathSolver.NodeToString(node, false, true));
            // get xpath of node
//            System.out.println("\"" + XpathSolver.buildNodeXpath(node) + "\",");
            Assert.assertEquals(expectedXPathString, XpathSolver.buildNodeXpath(node));
        }
    }

    @Test
    public void buildXPathForRuleTest() throws ParserConfigurationException, IOException, SAXException, ParsingException {
        String ruleString = "<?xml version='1.0' encoding='UTF-8'?><Rule Effect='Deny' RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess'>\n" +
                "    <Description>Deny Billing Statement access</Description>\n" +
                "    <Target>\n" +
                "        <AnyOf>\n" +
                "            <AllOf>\n" +
                "                <Match MatchId='urn:oasis:names:tc:xacml:1.0:function:string-equal'>\n" +
                "                    <AttributeValue DataType='http://www.w3.org/2001/XMLSchema#string'>Billing Statement</AttributeValue>\n" +
                "                    <AttributeDesignator AttributeId='com.axiomatics.hl7.object.objectType' Category='urn:oasis:names:tc:xacml:3.0:attribute-category:resource' DataType='http://www.w3.org/2001/XMLSchema#string' MustBePresent='false'/>\n" +
                "                </Match>\n" +
                "            </AllOf>\n" +
                "        </AnyOf>\n" +
                "    </Target>\n" +
                "</Rule>";
        InputStream stream = IOUtils.toInputStream(ruleString, Charset.defaultCharset());
        Document doc = PolicyLoader.getDocument(stream, false);
        Rule rule = Rule.getInstance(doc.getDocumentElement(), new PolicyMetaData(XACMLConstants.XACML_1_0_IDENTIFIER,
                null), null);
        String xpathString = XpathSolver.buildRuleXpath(rule);
        String expected = "//Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess']";
        Assert.assertEquals(expected, xpathString);
    }
}
