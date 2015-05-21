
package org.sag.coverage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;

import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

// a number of tests (queries) for the same policy
// the tests are assumed under the same folder of the policy under test
public class PolicyRunner {
 
	private static String BalanaPolicyLocation = "resources";
//	private static String BalanaPolicyLocation = "//Users//dianxiangxu//Documents//JavaProjects//WSO2-XACML//XPA//resources";
	
	private PDP pdp;			// created from the policy under test
	
	private String orginalPolicyFolder;
	
	public PolicyRunner(String policyFilePath) throws Exception{
    	setupPolicyResource(policyFilePath);
        System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, BalanaPolicyLocation);
        System.out.println(System.getProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY));
        
        Balana balana = Balana.getInstance();
        pdp = new PDP(balana.getPdpConfig());       
	}
	
	private File getPolicyFile(String policyFilePath) throws Exception{		
		File policyFile = new File(policyFilePath);
		if (policyFile.exists()) {
			return policyFile;
		}	
		String fileInCurrentFolder = System.getProperty("user.dir") +File.separator + policyFilePath.replace("//", "/");
System.out.println(fileInCurrentFolder);		
		policyFile = new File(fileInCurrentFolder);
		if (policyFile.exists())
			return policyFile;
		else 
			throw new Exception("File "+policyFilePath+" not found!");
	}
	
    private void setupPolicyResource(String policyFilePath) throws Exception{
          //deleteExistingPolicies(new File(BalanaPolicyLocation));
          File policyFile = getPolicyFile(policyFilePath);
          orginalPolicyFolder = policyFile.getParentFile().getAbsolutePath();
          String newPolicyFile = BalanaPolicyLocation + File.separator + policyFile.getName();
          //copyFile(policyFile, new File(newPolicyFile));
    }
    
    private static void deleteExistingPolicies(File policyFolder){
    	if (policyFolder.isDirectory()) {
    	    for (File policyFile : policyFolder.listFiles())
    	        policyFile.delete();
    	}
    }

    public int runTestWithoutOracle(String testID, String request){
		PolicyCoverageFactory.currentTestID = testID;
    	PolicyCoverageFactory.currentTestOracle = -1;
    	return getResponse(request);
    }

    public boolean runTest(String testID, String request, String oracleString){
		PolicyCoverageFactory.currentTestID = testID;
    	int oracle = balanaFinalDecision(oracleString);
    	PolicyCoverageFactory.currentTestOracle = oracle;
    	return getResponse(request) == oracle;
    }

    public boolean runTestFromFile(String testID, String requestFile, String oracleString){
		PolicyCoverageFactory.currentTestID = testID;
    	int oracle = balanaFinalDecision(oracleString);
    	PolicyCoverageFactory.currentTestOracle = oracle;
    	if (new File(requestFile).exists())
    		return getResponse(readTextFile(requestFile)) == oracle;
    	else
    		return getResponse(readTextFile(orginalPolicyFolder+File.separator+requestFile)) == oracle;
    }

    public int getResponse(String request){
      String response = pdp.evaluate(request);
//      System.out.println("\n======================== XACML Response ===================");
//      System.out.println(response);
//      System.out.println("===========================================================");
      int output = -8641;
      try {
          ResponseCtx responseCtx = ResponseCtx.getInstance(getXacmlResponse(response));
          AbstractResult result  = responseCtx.getResults().iterator().next();
          output = result.getDecision();
      } catch (ParsingException e) {
          e.printStackTrace();
      }
      return output;
    }
    
    private static Element getXacmlResponse(String response) {

        ByteArrayInputStream inputStream;
        DocumentBuilderFactory dbf;
        Document doc;

        inputStream = new ByteArrayInputStream(response.getBytes());
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            doc = dbf.newDocumentBuilder().parse(inputStream);
        } catch (Exception e) {
            System.err.println("DOM of request element can not be created from String");
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
               System.err.println("Error in closing input stream of XACML response");
            }
        }
        return doc.getDocumentElement();
    }    

    /*	
	public int balanaIntermediateDecision(String decisionString){
		if (decisionString.equalsIgnoreCase("Permit"))
			return AbstractResult.DECISION_PERMIT;
		else
		if (decisionString.equalsIgnoreCase("Deny"))
			return AbstractResult.DECISION_DENY;
		else
		if (decisionString.equalsIgnoreCase("NA"))
			return AbstractResult.DECISION_NOT_APPLICABLE;	
		else
		if (decisionString.equalsIgnoreCase("INDETERMINATE"))
			return AbstractResult.DECISION_INDETERMINATE;	
		else
		if (decisionString.equalsIgnoreCase("IP"))
			return AbstractResult.DECISION_INDETERMINATE_PERMIT;	
		else
		if (decisionString.equalsIgnoreCase("ID"))
			return AbstractResult.DECISION_INDETERMINATE_DENY;	
		else
		if (decisionString.equalsIgnoreCase("IDP"))
			return AbstractResult.DECISION_INDETERMINATE_DENY_OR_PERMIT;	
		return AbstractResult.DECISION_INDETERMINATE;	
	}
*/
	
	public int balanaFinalDecision(String decisionString){
		if (decisionString.equalsIgnoreCase("Permit"))
			return AbstractResult.DECISION_PERMIT;
		else
		if (decisionString.equalsIgnoreCase("Deny"))
			return AbstractResult.DECISION_DENY;
		else
		if (decisionString.equalsIgnoreCase("NA"))
			return AbstractResult.DECISION_NOT_APPLICABLE;	
		else
		if (decisionString.equalsIgnoreCase("INDETERMINATE"))
			return AbstractResult.DECISION_INDETERMINATE;	
		else
		if (decisionString.equalsIgnoreCase("IP"))
			return AbstractResult.DECISION_INDETERMINATE;	
		else
		if (decisionString.equalsIgnoreCase("ID"))
			return AbstractResult.DECISION_INDETERMINATE;	
		else
		if (decisionString.equalsIgnoreCase("IDP"))
			return AbstractResult.DECISION_INDETERMINATE;	
		return AbstractResult.DECISION_INDETERMINATE;	
	}
	
	
 	public static String readTextFile(String fileName){
		return readTextFile(new File(fileName));
	}

	public static String readTextFile(File file){
		String text = "";
		if (file==null || !file.exists())
			return text;
		Scanner in = null; 
		try {
			in = new Scanner(new FileReader(file));
			while (in.hasNextLine())
				text += in.nextLine()+"\n";
		} catch (IOException ioe){
		}
		finally {
			if (in!=null)
				in.close();
		}
		return text;
	}


	public static void copyFile (File fromFile, File toFile) throws IOException {
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = from.read(buffer)) != -1)
		    	to.write(buffer, 0, bytesRead);
		} 
		catch (IOException ioe){
			throw ioe;
		}
		finally {
			try {
				if (from != null)
					from.close();
				if (to != null)
					to.close();
			}
			catch (IOException ioe){
				throw ioe;
			}
		}
	}
	     
	public static void saveStringToTextFile(String fileString, String fileName) {
		File file = new File(fileName);
		saveStringToTextFile(fileString, file);
	}

	public static void saveStringToTextFile(String fileString, File file) {
		PrintWriter out = null;
		if (file.exists()) {
			file.delete();
		}
		try {
			out = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		out.print(fileString);
		out.close();
	}


 
}