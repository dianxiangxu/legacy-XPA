package org.sag.tryDOM;

import org.apache.commons.lang3.StringUtils;
import org.sag.policyUtils.UniversalNamespaceCache;
import org.w3c.dom.*;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.Rule;
import org.wso2.balana.combine.CombinerParameter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by shuaipeng on 9/8/16.
 */
public class FindingPath {
    private static Pattern policyPattern = Pattern.compile("(?:\\w+:)*Policy");
    private static Pattern policysetPattern = Pattern.compile("(?:\\w+:)*PolicySet");
    private static Pattern rulePattern = Pattern.compile("(?:\\w+:)*Rule");
    private static Pattern targetPattern = Pattern.compile("(?:\\w+:)*Target");

    private void findingPath() throws ParserConfigurationException, IOException, XPathExpressionException, SAXException {

//        String fileName = "org/sag/policies/conference3.xml";
//        String xPathString = "/Policy[1]/Rule[1]";

        String fileName = "org/sag/policies/HL7.xml";
        // TODO investigate why Xpath.evaluate() doen't work with namespace
//        String xPathString = "/xacml3:PolicySet[1]/xacml3:Policy[1]/xacml3:Rule[1]";
//        String xPathString = "/PolicySet[1]/Policy[1]/Rule[1]";
//        String xPathString = "/PolicySet[1]/Policy[1]/Rule[1]";
//        String xPathString = "/xacml3:PolicySet[1]/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/xacml3:Target[1]";
        String xPathString = "/xacml3:PolicySet[1]/xacml3:PolicySet[@PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/xacml3:Policy[@PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/xacml3:Rule[@RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords.readMedicalRecord']";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        //obtain Document somehow, doesn't matter how
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new FileInputStream(file));
        Node root = doc.getDocumentElement();

//Evaluate XPath against Document itself
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new UniversalNamespaceCache(doc, true));
        NodeList nodes = (NodeList)xPath.evaluate(xPathString,
                doc.getDocumentElement(), XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); ++i) {
//            Element e = (Element) nodes.item(i);
            System.out.println(toString(nodes.item(i), false, true));
        }
    }


    private void traverse() throws ParserConfigurationException, IOException, SAXException {
//        String fileName = "org/sag/policies/conference3.xml";
        String fileName = "org/sag/policies/HL7.xml";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        //obtain Document somehow, doesn't matter how
        DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = b.parse(new FileInputStream(file));
        List<String> list = getEntryList(doc);
        for (String str: list)
            System.out.println(str);
    }



    public static void main(String[] args) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
//        new FindingPath().findingPath();
        new FindingPath().traverse();
//        new FindingPath().tryXpath();
    }

    public static String toString(Node node, boolean omitXmlDeclaration, boolean prettyPrint) {
        if (node == null) {
            throw new IllegalArgumentException("node is null.");
        }

        try {
            // Remove unwanted whitespaces
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("//text()[normalize-space()='']");
            NodeList nodeList = (NodeList)expr.evaluate(node, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node nd = nodeList.item(i);
                nd.getParentNode().removeChild(nd);
            }

            // Create and setup transformer
            Transformer transformer =  TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            if (omitXmlDeclaration) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }

            if (prettyPrint) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            }

            // Turn the node into a string
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private static void buildEntryList( List<String> entries, String parentXPath, Element parent ) {
        String name = DOMHelper.getLocalName(parent);
        //here we use regex match instead of simply String.equals() because some XACML may use namespace, in such case
        // the local name is "namespace:Policy" instead of "Policy".
        if (rulePattern.matcher(name).matches()
                || targetPattern.matcher(name).matches() && !rulePattern.matcher(DOMHelper.getLocalName(parent.getParentNode())).matches())
            entries.add(parentXPath );

        if (policyPattern.matcher(name).matches() || policysetPattern.matcher(name).matches()) {
            NodeList children = parent.getChildNodes();
            for( int i = 0; i < children.getLength(); i++ ) {
                Node child = children.item( i );
                if (child instanceof Element) {
                    String identifier = getNodeIdentifier(child);
                    buildEntryList(entries, parentXPath + "/" + identifier, (Element) child);
                }
            }
        }
    }

    public static List<String> getEntryList( Document doc ) {
        ArrayList<String> entries = new ArrayList<String>();
        Element root = doc.getDocumentElement();
        buildEntryList(entries, "/"+root.getNodeName()+"[1]", root );
        return entries;
    }

    public static String buildXpath(Node node) {
        String nodeName = node.getNodeName();
        Node parent = node.getParentNode();
        if (parent == null)
            return "/" + nodeName;
        return buildXpath(parent) + "/" + buildXpath(node);
    }

    private static String getNodeIdentifier(Node node) {
        String nodeName = node.getNodeName();
        String idAttr = "";
        String idValue = "";
        if (policyPattern.matcher(nodeName).matches() || policysetPattern.matcher(nodeName).matches() || rulePattern.matcher(nodeName).matches()) {
            if (policyPattern.matcher(nodeName).matches()) {
                idAttr = "PolicyId";
            } else if (policysetPattern.matcher(nodeName).matches()) {
                idAttr = "PolicySetId";
            } else if (rulePattern.matcher(nodeName).matches()) {
                idAttr = "RuleId";
            }
            idValue = ((Element) node).getAttribute(idAttr);
        }
        String identifier;
        if (StringUtils.isEmpty(idValue))
            identifier = String.format("%s[1]", nodeName);
        else
            identifier= String.format("%s[@%s='%s']", nodeName, idAttr, idValue);
        return identifier;
    }
}
