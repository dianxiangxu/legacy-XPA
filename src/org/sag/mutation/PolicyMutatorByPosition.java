/**
 * 
 */
package org.sag.mutation;

import java.util.ArrayList;
import java.util.List;

import org.sag.combiningalgorithms.MyAttr;
import org.wso2.balana.AbstractTarget;
import org.wso2.balana.combine.CombinerElement;
import org.wso2.balana.combine.RuleCombiningAlgorithm;
import org.wso2.balana.combine.xacml2.FirstApplicableRuleAlg;
import org.wso2.balana.combine.xacml3.DenyOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.DenyUnlessPermitRuleAlg;
import org.wso2.balana.combine.xacml3.PermitOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.PermitUnlessDenyRuleAlg;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.wso2.balana.xacml3.Target;

/**
 * @author speng
 *
 */
public class PolicyMutatorByPosition extends PolicyMutator {
	private int position;
	private List<CombinerElement> rule_list;

	/**
	 * @param policyFilePath
	 * @throws Exception
	 */
	public PolicyMutatorByPosition(String policyFilePath) throws Exception {
		super(policyFilePath);
	}
	
	
	// Policy Related Mutation Operators----------------------------------------------------------
	// PTT
	/**
	 * Remove the Target of each Policy ensuring that the Policy is applied to all requests.
	 */
	public List<PolicyMutant> createPolicyTargetTrueMutantsByPosition() {
		List<PolicyMutant> mutantList = new ArrayList<PolicyMutant>();
		
		int mutantIndex=1;
		int bugPosition = 0;
		if(!getPolicy().isTargetEmpty()) {
			AbstractTarget target = getPolicy().getTarget();
			// Analyze AnyOf... The target might still be empty.
			List<AnyOfSelection> listAnyOf = ((Target)target).getAnyOfSelections();
			//System.out.println("Size = " + listAnyOf.size());
			if (listAnyOf.size()!=0) {
				getPolicy().setTargetEmpty();				
				
				StringBuilder builder = new StringBuilder();
				getPolicy().encode(builder);
				String mutantFileName = getMutantFileName("PTT"+mutantIndex);
				getMutantList().add(new PolicyMutant(PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" PTT"+mutantIndex, mutantFileName, bugPosition));
				saveStringToTextFile(builder.toString(), mutantFileName);
				getPolicy().setTarget(target);
			}
		}
		return mutantList;
	}
	
	// PTF
	/**
	 * Set policy target always false.
	 * @throws Exception
	 */
	public List<PolicyMutant> createPolicyTargetFalseMutantsByposition() throws Exception {
		List<PolicyMutant> mutantList = new ArrayList<PolicyMutant>();
		
		int mutantIndex=1;
		int bugPosition = 0;
		// Collect attributes from targets and conditions.
		ArrayList<MyAttr> attr = collectAttributes(getPolicy());
		
		if(attr.size()<1) {
			throw new Exception("No attribute collected");
		} else {
			// build up a false target
			String falseTarget = buildFalseTarget(attr);
			// create a single mutation for the policy.
			StringBuilder builder = new StringBuilder();
			getPolicy().encode(builder);

			// default policy target starting/ending index.
			int policyTargetStartingIndex = builder.indexOf(">\n") + 2;
			int targetEndingIndex = policyTargetStartingIndex;
			// if there exists a target, update the targetEndingIndex; otherwise, use default.
			if(!getPolicy().isTargetEmpty()) {
				targetEndingIndex = builder.indexOf("</Target>", policyTargetStartingIndex) + 9+1; // +1 to include '\n'		
			} 
			// Replace the old target with the false target.
			builder.replace(policyTargetStartingIndex, targetEndingIndex, falseTarget);
			String mutantFileName = getMutantFileName("PTF"+mutantIndex);
			
			PolicyMutant mutant = new PolicyMutant(PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" PTF"+mutantIndex, mutantFileName, bugPosition);
			
			getMutantList().add(mutant);					
			saveStringToTextFile(builder.toString(), mutantFileName);				
		}
		
		mutantList.add(mutant);
		return mutantList;
	}

	public List<PolicyMutant> createCombiningAlgorithmMutantsByPosition() {
		List<PolicyMutant> mutantList = new ArrayList<PolicyMutant>();
		
		RuleCombiningAlgorithm[] combiningAlgorithms = {new DenyOverridesRuleAlg(), 
				new PermitOverridesRuleAlg(), new DenyUnlessPermitRuleAlg(), 
				new PermitUnlessDenyRuleAlg(), new FirstApplicableRuleAlg()};
		int mutantIndex=1;
		for (RuleCombiningAlgorithm algorithm: combiningAlgorithms){
			if (!getPolicy().getCombiningAlg().getIdentifier().equals(algorithm.getIdentifier())) {
				RuleCombiningAlgorithm originalAlgorithm = (RuleCombiningAlgorithm)getPolicy().getCombiningAlg();
				getPolicy().setCombiningAlg(algorithm);
				StringBuilder builder = new StringBuilder();
				getPolicy().encode(builder);
				String mutantFileName = getMutantFileName("CRC"+mutantIndex);
				
				PolicyMutant mutant = new PolicyMutant(PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" CRC"+mutantIndex, mutantFileName, -1);
				getMutantList().add(mutant);
				
				saveStringToTextFile(builder.toString(), mutantFileName);
				getPolicy().setCombiningAlg(originalAlgorithm);				
				mutantIndex++;
				
				mutantList.add(mutant);
			}
		}
		return mutantList;
	}
	
	

}
