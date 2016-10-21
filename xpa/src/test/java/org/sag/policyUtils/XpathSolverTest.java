package org.sag.policyUtils;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 * Created by shuaipeng on 10/20/16.
 */
public class XpathSolverTest {
    @Test
    public void getEntryListTest() throws ParserConfigurationException, IOException, SAXException {
//        String fileName = "org/sag/policies/conference3.xml";
        String fileName = "org/sag/policies/HL7.xml";
        ClassLoader classLoader = XpathSolver.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        //obtain Document somehow, doesn't matter how
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = b.parse(new FileInputStream(file));
        List<String> list = XpathSolver.getEntryList(doc);
//        for (String entry: list)
//            System.out.println("\"" + entry + "\",");

        String[] expected = new String[]{
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.updateNote']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments.createAssessment']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory.readPaymentHistory']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords.readMedicalRecord']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords.explicitDenyAccess']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess.withAdditionalAuthority']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess.accessMedicalRecord']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.conditionpolicyset']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.conditionpolicyset']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.conditionpolicyset']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.alternateRead']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.conditionpolicyset']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.alternateUpdate']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan.Id_105']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/xacml3:Target[1]",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.clinicalObjectAccess']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess']"
        };

        Assert.assertArrayEquals(list.toArray(), expected);
    }

    @Test
    public void buildXpathTest() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        String fileName = "org/sag/policies/HL7.xml";
        ClassLoader classLoader = XpathSolver.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        //obtain Document somehow, doesn't matter how
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new FileInputStream(file));
        //Evaluate XPath against Document itself
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new UniversalNamespaceCache(doc, true));
        // TODO investigate why Xpath.evaluate() doen't work with namespace
        List<String> xpathList = Arrays.asList("/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.clinicalObjectAccess']",
                "/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan']/xacml3:Target[1]"
        );
        for (String expectedXPathString : xpathList) {
            // get node by xpath string
            NodeList nodes = (NodeList) xPath.evaluate(expectedXPathString,
                    doc.getDocumentElement(), XPathConstants.NODESET);
            // the xpath should identify a unique node
            Assert.assertEquals(nodes.getLength(), 1);
            Node node = nodes.item(0);
//            System.out.println(XpathSolver.NodeToString(node, false, true));
            // get xpath of node
            Assert.assertEquals(expectedXPathString, XpathSolver.buildXpath(node));
        }
    }
}
