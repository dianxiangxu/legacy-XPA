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
    private static Map<String, String> matchIDMap = new HashMap<>();
    private static Map<String, List<String>> unequalValuesMap = new HashMap<>();

    static {
        /*
          see http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047117 Section 10.2.7 Data-types
         */
        matchIDMap.put("http://www.w3.org/2001/XMLSchema#string", "urn:oasis:names:tc:xacml:1.0:function:string-equal");
        matchIDMap.put("http://www.w3.org/2001/XMLSchema#boolean", "urn:oasis:names:tc:xacml:1.0:function:boolean-equal");
        matchIDMap.put("http://www.w3.org/2001/XMLSchema#integer", "urn:oasis:names:tc:xacml:1.0:function:integer-equal");
        matchIDMap.put("http://www.w3.org/2001/XMLSchema#double", "urn:oasis:names:tc:xacml:1.0:function:double-equal");
        matchIDMap.put("http://www.w3.org/2001/XMLSchema#date", "urn:oasis:names:tc:xacml:1.0:function:date-equal");
        /*
          see https://www.w3.org/TR/xmlschema-2/#built-in-primitive-datatypes for legal literals for different types
         */
        unequalValuesMap.put("http://www.w3.org/2001/XMLSchema#string", Arrays.asList("a", "b"));
        unequalValuesMap.put("http://www.w3.org/2001/XMLSchema#boolean", Arrays.asList("true", "false"));
        unequalValuesMap.put("http://www.w3.org/2001/XMLSchema#integer", Arrays.asList("1", "2"));
        unequalValuesMap.put("http://www.w3.org/2001/XMLSchema#double", Arrays.asList("1.0", "2.0"));
        unequalValuesMap.put("http://www.w3.org/2001/XMLSchema#date", Arrays.asList("2002-10-10+13:00", "2002-10-11+13:00"));
    }

    private String int_function_one_and_only = "urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only";
    private String str_function_one_and_only = "urn:oasis:names:tc:xacml:1.0:function:string-one-and-only";
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
        System.out.println(XpathSolver.nodeToString(node, false, true));
        Node child = node.getFirstChild();
        node.removeChild(child);
        System.out.println(XpathSolver.nodeToString(node, false, true));
        node.appendChild(child);
        System.out.println(XpathSolver.nodeToString(node, false, true));

    }

    /**
     * flip rule effect
     */
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

    /**
     * Make Policy Target always true
     */
    public List<Mutant> createPolicyTargetTrueMutants(String xpathString) throws XPathExpressionException, ParsingException, IOException, ParserConfigurationException, SAXException {
        int faultLocation = xpathMapping.get(xpathString);
        return createTargetTrueMutants(xpathString, "PTT", faultLocation);

    }

    /**
     * Make Rule Target always true
     */
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
     * Make Rule Condition always true
     *
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

    /**
     * Make Rule Target always false
     */
    public List<Mutant> createRuleTargetFalseMutants(String xpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "RTT";
        String matchXpathString = xpathString + "/*[local-name()='Target' and 1]/*[local-name()='AnyOf' and 1]/*[local-name()='AllOf' and 1]/*[local-name()='Match' and 1]";
        return createTargetFalseMutants(matchXpathString, faultLocation, mutantName);
    }

    /**
     * Make the Target of a Policy or PolicySet always false
     *
     * @param xpathString xpath to the Target of a Policy or PolicySet
     */
    public List<Mutant> createPolicyTargetFalseMutants(String xpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "PTT";
        String matchXpathString = xpathString + "/*[local-name()='AnyOf' and 1]/*[local-name()='AllOf' and 1]/*[local-name()='Match' and 1]";
        return createTargetFalseMutants(matchXpathString, faultLocation, mutantName);
    }

    /**
     * If Match element exists, we can make the Target always evaluate to false by adding two conflicting Match elements
     * to the parent of Match element. For example, if the Match element says role == "physician", then we add 2 Match
     * elements: role == "a" and role == "b".
     * First make 2 clones of the Match element, for each clone: find the AttributeValue element in the Match element,
     * get the DataType attribute from the AttributeValue. Set the MatchId attribute according to DataType. And set the
     * text content of AttributeValue element according the DataType. For example, if DataType is string, we set MatchId
     * to string-equals, set the text context to "a" and "b" for the 2 clones separately.
     * element.
     *
     * @param matchXpathString the xpath to the first Match element in a Target element
     */
    private List<Mutant> createTargetFalseMutants(String matchXpathString, int faultLocation, String mutantName) throws XPathExpressionException, ParsingException {
        List<Mutant> list = new ArrayList<>();
        NodeList nodes = (NodeList) xPath.evaluate(matchXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Node matchNode = nodes.item(0);
        if (matchNode != null && !isEmptyNode(matchNode)) {
            //change doc
            List<Node> clonedNodes = new ArrayList<>();
            for (int k = 0; k < 2; k++) {
                Node cloned = matchNode.cloneNode(true);
                clonedNodes.add(cloned);
                //find the AttributeValue child node
                List<Node> attributeValueNodes = findChildrenByLocalName(cloned, "AttributeValue");
                if (attributeValueNodes.size() == 0) {
                    throw new RuntimeException("couldn't find AttributeValue in Mathch");
                }
                Node attributeValueNode = attributeValueNodes.get(0);
                //set MatchId and AttributeValue according to DataType
                String dataType = attributeValueNode.getAttributes().getNamedItem("DataType").getNodeValue();
                if (!matchIDMap.containsKey(dataType)) {
                    throw new RuntimeException("unsupported DataType: " + dataType);
                }
                cloned.getAttributes().getNamedItem("MatchId").setNodeValue(matchIDMap.get(dataType));
                attributeValueNode.setTextContent(unequalValuesMap.get(dataType).get(k));
                //add two conflicting Match nodes to parent
                matchNode.getParentNode().appendChild(cloned);
            }
//            System.out.println(XpathSolver.nodeToString(matchNode.getParentNode(), false, true));
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
            //restore doc by removing the two conflicting Match nodes from parent
            for (Node cloned : clonedNodes) {
                matchNode.getParentNode().removeChild(cloned);
            }
//            System.out.println(XpathSolver.nodeToString(matchNode.getParentNode(), false, true));
        }
        return list;
    }

    /**
     * @return all the child nodes of input node whose local name equals to input argument localName
     */
    private List<Node> findChildrenByLocalName(Node node, String localName) {
        List<Node> matchedChildNodes = new ArrayList<>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (localName.equals(child.getLocalName())) {
                matchedChildNodes.add(child);
            }
        }
        return matchedChildNodes;
    }
}
