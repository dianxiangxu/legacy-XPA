package org.sag.mutation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sag.policyUtils.PolicyLoader;
import org.sag.policyUtils.XpathSolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
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
import java.util.*;

/**
 * Created by shuaipeng on 9/8/16.
 */
public class Mutator {
    private static Log logger = LogFactory.getLog(Mutator.class);
    // so far only string and integer are considered.
    private String int_function = "urn:oasis:names:tc:xacml:1.0:function:integer-equal";
    private String str_function = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
    private String int_function_one_and_only = "urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only";
    private String str_function_one_and_only = "urn:oasis:names:tc:xacml:1.0:function:string-one-and-only";
    private String str_value = "RANDOM$@^$%#&!";
    private String str_value1 = "str_A";
    private String str_value2 = "str_B";
    private String int_value = "-98274365923795632";
    private String int_value1 = "123456789";
    private String int_value2 = "-987654321";
    private Mutant baseMutant;
    private List<String> xpathList;
    private Map<String, Integer> xpathMapping;
    private Document doc;
    private XPath xPath;

    public Mutator(Mutant baseMutant) throws ParserConfigurationException, SAXException, IOException {
        this.baseMutant = baseMutant;
        InputStream stream = IOUtils.toInputStream(baseMutant.encode(), Charset.defaultCharset());
        doc = PolicyLoader.getDocument(stream);
        xpathList = XpathSolver.getEntryListRelativeXPath(doc);
        xpathMapping = new HashMap<>();
        for (int i = 0; i < xpathList.size(); i++) {
            xpathMapping.put(xpathList.get(i), i);
        }
        xPath = XPathFactory.newInstance().newXPath();
    }

    static boolean isEmptyNode(Node node) {
        // When the target is empty, it may have one child node that contains only text "\n"; when the target is not empty,
        // it may have three nodes: "\n", AnyOf element and "\n".
        NodeList children = node.getChildNodes();
        boolean isEmptyTarget = true;
        for (int i = 0; i < children.getLength(); i++) {
            // we don't use "if (child.getNodeName().equals("AnyOf"))" here for backward compatibility with XACML 2.0.
            if (!children.item(i).getNodeName().equals("#text")) {
                isEmptyTarget = false;
            }
        }
        return isEmptyTarget;
    }

    public static void main(String[] args) throws ParserConfigurationException, ParsingException, SAXException, IOException, XPathExpressionException {
        File file = new File("src/test/resources/org/sag/policies/HL7/HL7.xml");
        String xpathString = "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNoteoo']";
        XPath xPath = XPathFactory.newInstance().newXPath();
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        Document doc = PolicyLoader.getDocument(IOUtils.toInputStream(policy.encode(), Charset.defaultCharset()));
        NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        // assert only one
        Node node = nodes.item(0);
        System.out.println(XpathSolver.NodeToString(node, false, true));
        Node child = node.getFirstChild();
        node.removeChild(child);
        System.out.println(XpathSolver.NodeToString(node, false, true));
        node.appendChild(child);
        System.out.println(XpathSolver.NodeToString(node, false, true));

    }

    public List<Mutant> createRuleEffectFlippingMutants(String xpathString) throws XPathExpressionException, ParsingException {
        List<Mutant> list = new ArrayList<>();
        NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Node node = nodes.item(0);
        if (node != null) {
            //change doc
            if (node.getAttributes().getNamedItem("Effect").getTextContent().equals("Deny")) {
                node.getAttributes().getNamedItem("Effect").setTextContent("Permit");
            } else {
                node.getAttributes().getNamedItem("Effect").setTextContent("Deny");
            }
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            //restore doc
            if (node.getAttributes().getNamedItem("Effect").getTextContent().equals("Deny")) {
                node.getAttributes().getNamedItem("Effect").setTextContent("Permit");
            } else {
                node.getAttributes().getNamedItem("Effect").setTextContent("Deny");
            }
            int faultLocation = xpathMapping.get(xpathString);
            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), "CRE" + faultLocation));
        }
        return list;
    }

    public List<Mutant> createPolicyTargetTrueMutants(String xpathString) throws XPathExpressionException, ParsingException, IOException, ParserConfigurationException, SAXException {
        int faultLocation = xpathMapping.get(xpathString);
        return createTargetTrueMutants(xpathString, "PTT", faultLocation);

    }

    public List<Mutant> createRuleTargetTrueMutants(String xpathString) throws XPathExpressionException, ParsingException, IOException, ParserConfigurationException, SAXException {
        int faultLocation = xpathMapping.get(xpathString);
        return createTargetTrueMutants(xpathString + "/*[local-name()='Target' and 1]", "RTT", faultLocation);
    }

    private List<Mutant> createTargetTrueMutants(String xpathString, String mutantName, int faultLocation) throws XPathExpressionException, ParsingException, IOException, ParserConfigurationException, SAXException {
        List<Mutant> list = new ArrayList<>();
        NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Node node = nodes.item(0);
        if (node != null && !isEmptyNode(node)) {
            //change doc
            List<Node> children = new ArrayList<>();
            while (node.hasChildNodes()) {
                Node child = node.getFirstChild();
                children.add(child);
                node.removeChild(child);
            }
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
            //restore doc
            for (Node child : children) {
                node.appendChild(child);
            }
        }
        return list;
    }

    /**
     * We cannot remove all child nodes of Condition as we do to policy target and rule target, because
     * Condition.getInstance() will throw a null pointer exception. So here the whole Condition node is removed from the
     * rule node.
     */
    public List<Mutant> createRuleConditionTrueMutants(String xpathString) throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "RCT";
        String conditionXpathString = xpathString + "/*[local-name()='Condition' and 1]";
        List<Mutant> list = new ArrayList<>();
        NodeList nodes = (NodeList) xPath.evaluate(conditionXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Node node = nodes.item(0);
        if (node != null && !isEmptyNode(node)) {
            //change doc
            Node ruleNode = node.getParentNode();
            ruleNode.removeChild(node);
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
            //restore doc
            ruleNode.appendChild(node);
        }
        return list;
    }
}
