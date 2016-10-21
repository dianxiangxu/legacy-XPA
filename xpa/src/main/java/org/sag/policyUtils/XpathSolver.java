package org.sag.policyUtils;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.DOMHelper;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by shuaipeng on 10/20/16.
 */
public class XpathSolver {
    private static Pattern policyPattern = Pattern.compile("(?:\\w+:)*Policy");
    private static Pattern policysetPattern = Pattern.compile("(?:\\w+:)*PolicySet");
    private static Pattern rulePattern = Pattern.compile("(?:\\w+:)*Rule");
    private static Pattern targetPattern = Pattern.compile("(?:\\w+:)*Target");

    /**
     * get the string representation of a DOM node, used for debugging
     *
     * @param node
     * @param omitXmlDeclaration
     * @param prettyPrint
     * @return string representation of a DOM node
     */
    public static String NodeToString(Node node, boolean omitXmlDeclaration, boolean prettyPrint) {
        if (node == null) {
            throw new IllegalArgumentException("node is null.");
        }

        try {
            // Remove unwanted whitespaces
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("//text()[normalize-space()='']");
            NodeList nodeList = (NodeList) expr.evaluate(node, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node nd = nodeList.item(i);
                nd.getParentNode().removeChild(nd);
            }

            // Create and setup transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
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

    private static void buildEntryList(List<String> entries, String parentXPath, Element parent) {
        String name = DOMHelper.getLocalName(parent);
        //here we use regex match instead of simply String.equals() because some XACML may use namespace, in such case
        // the local name is "namespace:Policy" instead of "Policy".
        if (rulePattern.matcher(name).matches()
                || targetPattern.matcher(name).matches() && !rulePattern.matcher(DOMHelper.getLocalName(parent.getParentNode())).matches())
            entries.add(parentXPath);

        if (policyPattern.matcher(name).matches() || policysetPattern.matcher(name).matches()) {
            NodeList children = parent.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element) {
                    String identifier = getNodeIdentifier(child);
                    buildEntryList(entries, parentXPath + "/" + identifier, (Element) child);
                }
            }
        }
    }

    public static List<String> getEntryList(Document doc) {
        ArrayList<String> entries = new ArrayList<>();
        Element root = doc.getDocumentElement();
        buildEntryList(entries, "/" + getNodeIdentifier(root), root);
        return entries;
    }

    public static String buildXpath(Node node) {
        Node parent = node.getParentNode();
        String parentXpath = "";
        if (parent != null && parent.getParentNode() != null)
            parentXpath = buildXpath(parent);
        return parentXpath + "/" + getNodeIdentifier(node);
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
            identifier = String.format("%s[@%s='%s']", nodeName, idAttr, idValue);
        return identifier;
    }
}
