package org.sag.policyUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * this class loads a <code>Policy</code> or a <code>PolicySet</code> from a file
 * Created by shuaipeng on 9/7/16.
 */
public class PolicyLoader {
    private static Log logger = LogFactory.getLog(PolicyLoader.class);

    /**
     * read file try to get a <code>Document</code> object from it
     *
     * @param file XACML file to read
     * @return a <code>Document</code> object
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private static Document getDocument(File file) throws IOException, SAXException, ParserConfigurationException {
        try (InputStream stream = new FileInputStream(file)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            factory.setIgnoringComments(true);
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            // create a builder based on the factory & try to load the policy
            DocumentBuilder db = factory.newDocumentBuilder();
            // Test
            //System.out.println(policyFile);
            return db.parse(stream);
        }

    }

    /**
     * read a file and try to get a <code>Policy</code> or <code>PolicySet</code> object from it
     *
     * @param file XACML file to read
     * @return a  <code>Policy</code> or <code>PolicySet</code> object, or null if an error occurred.
     */
    public static AbstractPolicy loadPolicy(File file) {
        try {
            Document doc = getDocument(file);
            Element root = doc.getDocumentElement();
            if (DOMHelper.getLocalName(root).equals("Policy"))
                return Policy.getInstance(root);
            else if (DOMHelper.getLocalName(root).equals("PolicySet"))
                return PolicySet.getInstance(root);
            else
                throw new ParsingException("Cannot create Policy or PolicySet from root of type "
                        + DOMHelper.getLocalName(root));
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

}
