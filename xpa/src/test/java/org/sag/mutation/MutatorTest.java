package org.sag.mutation;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sag.policyUtils.PolicyLoader;
import org.sag.policyUtils.ReflectionUtils;
import org.sag.policyUtils.XpathSolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.ComparisonControllers;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by shuaipeng on 12/1/16.
 */
public class MutatorTest {
    private XPath xPath = XPathFactory.newInstance().newXPath();
    private List<String> xpathList;
    private Mutator mutator;
    private Document doc;

    private boolean isRuleXpathString(String xPathString) {
        return xPathString.contains("[local-name()='Rule'");
    }

    private boolean isTargetXpathString(String xPathString) {
        return xPathString.contains("[local-name()='Target'");
    }

    @Before
    public void initialize() throws ParserConfigurationException, ParsingException, SAXException, IOException {
        File file = new File("src/test/resources/org/sag/policies/HL7/HL7.xml");
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        doc = PolicyLoader.getDocument(IOUtils.toInputStream(policy.encode(), Charset.defaultCharset()));
        xpathList = XpathSolver.getEntryListRelativeXPath(doc);
        mutator = new Mutator(new Mutant(policy, ""));
    }

    @After
    public void checkIfDocIsRestored() {
        // make sure the doc in Mutator is properly restored
        // compiler warnings like "" will appear, which is caused by a bug in JVM
        // there's little we can do about this
        // see http://stackoverflow.com/questions/25453042/how-to-disable-accessexternaldtd-and-entityexpansionlimit-warnings-with-logback
        isSameDoc(doc, (Document) ReflectionUtils.getField(mutator, "doc"));
    }

    private void isSameDoc(Document docBefore, Document docAfter) {
        Diff diff = DiffBuilder.compare(docBefore)
                .withTest(docAfter)
                .checkForSimilar()
                .ignoreComments()
                .ignoreWhitespace()
                .normalizeWhitespace()
                .withComparisonController(ComparisonControllers.StopWhenDifferent)
                .build();
        Assert.assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void createRuleEffectFlippingMutantsTest() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
                NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
                String effect = node.getAttributes().getNamedItem("Effect").getTextContent();
                Assert.assertTrue(effect.equals("Permit") || effect.equals("Deny"));
                List<Mutant> mutants = mutator.createRuleEffectFlippingMutants(xpathString);
                Assert.assertEquals(1, mutants.size());
                Mutant mutant = mutants.get(0);
                Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                nodes = (NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                node = nodes.item(0);
                String flippedEffect = node.getAttributes().getNamedItem("Effect").getTextContent();
                Assert.assertTrue(flippedEffect.equals("Permit") || flippedEffect.equals("Deny"));
                Assert.assertNotEquals(effect, flippedEffect);
            }
        }
    }

    @Test
    public void createPolicyTargetTrueMutantsTest() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isTargetXpathString(xpathString)) {
//                System.out.println(xpathString);
                NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
//                System.out.println(XpathSolver.NodeToString(node, false, true));
                List<Mutant> mutants = mutator.createPolicyTargetTrueMutants(xpathString);
                if (!Mutator.isEmptyTarget(node)) {
                    Assert.assertEquals(1, mutants.size());
                    Mutant mutant = mutants.get(0);
                    Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                    nodes = (NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                    Assert.assertEquals(1, nodes.getLength());
                    Node newNode = nodes.item(0);
//                    System.out.println(XpathSolver.NodeToString(newNode, false, true));
                    Assert.assertTrue(Mutator.isEmptyTarget(newNode));
                } else {
//                    System.out.println(XpathSolver.NodeToString(node, false, true));
                    Assert.assertEquals(0, mutants.size());
                }
//                System.out.println("===========");
            }
        }
    }

    @Test
    public void createRuleTargetTrueMutantsTest() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
                String targetXpathString = xpathString + "/*[local-name()='Target' and 1]";
//                System.out.println(xpathString);
                NodeList nodes = (NodeList) xPath.evaluate(targetXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
//                System.out.println(XpathSolver.NodeToString(node, false, true));
                List<Mutant> mutants = mutator.createRuleTargetTrueMutants(xpathString);
                if (!Mutator.isEmptyTarget(node)) {
                    Assert.assertEquals(1, mutants.size());
                    Mutant mutant = mutants.get(0);
                    Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                    nodes = (NodeList) xPath.evaluate(targetXpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                    Assert.assertEquals(1, nodes.getLength());
                    Node newNode = nodes.item(0);
//                    System.out.println(XpathSolver.NodeToString(newNode, false, true));
                    Assert.assertTrue(Mutator.isEmptyTarget(newNode));
                } else {
//                    System.out.println(XpathSolver.NodeToString(node, false, true));
                    Assert.assertEquals(0, mutants.size());
                }
//                System.out.println("===========");
            }
        }
    }
}
