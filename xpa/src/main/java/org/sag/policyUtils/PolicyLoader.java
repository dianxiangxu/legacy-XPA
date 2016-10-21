package org.sag.policyUtils;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sag.mutation.Mutant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Mutant> readMutantsCSVFile(String mutantsCSVFileName) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(mutantsCSVFileName));
        List<Mutant> mutantList = new ArrayList<>();
        for (String[] entry : reader) {
            String mutantName = entry[0];
            String fileName = new File(mutantsCSVFileName).getParent() + File.separator + entry[1];
            List<Integer> bugPositions = StringToIntList(entry[2]);
            mutantList.add(new Mutant(PolicyLoader.loadPolicy(new File(fileName)), bugPositions, mutantName));
        }
        reader.close();
        return mutantList;
    }

    private static List<Integer> StringToIntList(String str) {
        List<Integer> results = new ArrayList<>();
        if (StringUtils.isEmpty(str))
            return results;
        String[] strs = str.replace("[", "").replace("]", "").split(",");
        for (String str1 : strs) {
            results.add(Integer.parseInt(str1.trim()));
        }
        return results;
    }

}
