package org.sag.combiningalgorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.sag.combiningalgorithms.TruthTable.ConRecord;
import org.sag.combiningalgorithms.TruthTable.PolicyTable;
import org.sag.combiningalgorithms.TruthTable.RuleRecord;
import org.sag.combiningalgorithms.TruthTable.TarRecord;
import org.sag.coverage.PolicySpreadSheetTestRecord;
import org.sag.coverage.PolicySpreadSheetTestSuite;
import org.sag.gui.MutationPanel;
import org.sag.gui.MutationPanel2;
import org.sag.gui.TestPanel;
import org.sag.gui.XPA;
import org.sag.mcdc.MCDCConditionSet;
import org.sag.mcdc.MCDC_converter2;
import org.sag.mutation.PolicyMutator;
import org.wso2.balana.Balana;
import org.wso2.balana.MatchResult;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.Policy;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyTreeElement;
import org.wso2.balana.Rule;
import org.wso2.balana.TargetMatch;
import org.wso2.balana.XACMLConstants;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.attr.DateAttribute;
import org.wso2.balana.attr.IPAddressAttribute;
import org.wso2.balana.attr.IntegerAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.attr.TimeAttribute;
import org.wso2.balana.attr.xacml3.AttributeDesignator;
import org.wso2.balana.combine.CombinerElement;
import org.wso2.balana.combine.CombiningAlgorithm;
import org.wso2.balana.combine.xacml2.FirstApplicableRuleAlg;
import org.wso2.balana.combine.xacml3.DenyOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.DenyUnlessPermitRuleAlg;
import org.wso2.balana.combine.xacml3.PermitOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.PermitUnlessDenyRuleAlg;
import org.wso2.balana.cond.Apply;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.cond.Expression;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.ResultFactory;
import org.wso2.balana.ctx.xacml2.Result;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.ctx.xacml3.XACML3EvaluationCtx;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.xacml3.AllOfSelection;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.wso2.balana.xacml3.Target;

public class PolicyX {

	private static Balana balana;
	String policyName;
	algorithm al = new algorithm();
	Call_Z3str z3 = new Call_Z3str();
	public HashMap nameMap = new HashMap();
	public HashMap typeMap = new HashMap();

	private String getName(String name) {
		boolean has = true;
		if (nameMap.containsKey(name)) {
			return nameMap.get(name).toString();
		} else {
			StringBuffer sb = new StringBuffer();
			do {
				sb = new StringBuffer();
				String base = "abcdefghijklmnopqrstuvwxyz";
				Random random = new Random();
				for (int i = 0; i < 5; i++) {
					int number = random.nextInt(base.length());

					sb.append(base.charAt(number));
				}
				if (!nameMap.containsValue(sb.toString())) {
					has = false;
				}
			} while (has == true);
			nameMap.put(name, sb.toString());
			return sb.toString();
		}
	}

	private String getType(String name, String type) {
		if (typeMap.containsKey(name)) {
			return typeMap.get(name).toString();
		} else {
			if (type.contains("string")) {
				typeMap.put(name, "String");
			}
			if (type.contains("integer")) {
				typeMap.put(name, "Int");
			}
			return typeMap.get(name).toString();
		}
	}

	Policy policy;

	public PolicyX(Policy policy) {
		this.policy = policy;
		this.policyName = policy.getId().toString();
		// System.out.println(policyName);
	}

	public Vector<Vector<Object>> generateRequestForDifferenceRCAs()
			throws IOException {
		function f = new function();
		System.out.println(f.checkDefaultRule(policy) + "default rule");
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		if (policy.getCombiningAlg() instanceof DenyOverridesRuleAlg) {
			result = generateRequestsForDenyOverrides();
		} else if (policy.getCombiningAlg() instanceof PermitOverridesRuleAlg) {
			result = generateRequestsForPermitOverrides();
		} else if (policy.getCombiningAlg() instanceof DenyUnlessPermitRuleAlg) {
			result = generateRequestsForDenyUnlessPermit();
		} else if (policy.getCombiningAlg() instanceof PermitUnlessDenyRuleAlg) {
			result = generateRequestsForPermitUnlessDeny();
		} else if (policy.getCombiningAlg() instanceof FirstApplicableRuleAlg) {
			result = generateRequestsForFirstApplicable();
		}

		for (Vector<Object> child : result) {
			if (child.get(1).toString().equals("0")) {
				child.set(1, "Pemrit");
			} else if (child.get(1).toString().equals("1")) {
				child.set(1, "Deny");
			} else if (child.get(1).toString().equals("2")) {
				child.set(1, "Indeterminate");
			} else if (child.get(1).toString().equals("3")) {
				child.set(1, "NotApplicable");
			} else if (child.get(1).toString().equals("4")) {
				child.set(1, "ID");
			} else if (child.get(1).toString().equals("5")) {
				child.set(1, "IP");
			} else if (child.get(1).toString().equals("6")) {
				child.set(1, "IDP");
			}

			if (child.get(3).toString().equals("0")) {
				child.set(3, "Pemrit");
			} else if (child.get(3).toString().equals("1")) {
				child.set(3, "Deny");
			} else if (child.get(3).toString().equals("2")) {
				child.set(3, "Indeterminate");
			} else if (child.get(3).toString().equals("3")) {
				child.set(3, "NotApplicable");
			} else if (child.get(3).toString().equals("4")) {
				child.set(3, "ID");
			} else if (child.get(3).toString().equals("5")) {
				child.set(3, "IP");
			} else if (child.get(3).toString().equals("6")) {
				child.set(3, "IDP");
			}
		}

		return result;
	}

	private Vector<Object> createVector(String alg1, String result1,
			String alg2, String result2, String request) {
		Vector<Object> data1 = new Vector<Object>();
		data1.add(alg1);
		data1.add(result1);
		data1.add(alg2);
		data1.add(result2);
		data1.add(request);
		System.out.println(request + " request");
		return data1;
	}

	public Vector<Vector<Object>> generateRequestsForDenyOverrides()
			throws IOException {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		PolicyEvaluation pe = new PolicyEvaluation();
		// ArrayList<QueryForDifference> requests = new
		// ArrayList<QueryForDifference>();
		String request1 = Deny_Permit_Override();
		if (request1 != "") {

			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
			Vector<Object> data1 = createVector("DenyOverrides",
					PolicyEvaluate(policy, request1) + "", "PermitOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyOverrides", "null",
					"PermitOverrides", "null", "");
			data.add(data1);
		}

		request1 = DenyOverride_DenyUnlessPermit();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit");
			Vector<Object> data1 = createVector("DenyOverrides",
					PolicyEvaluate(policy, request1) + "", "DenyUnlessPermit",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyOverrides", "null",
					"DenyUnlessPermit", "null", "");
			data.add(data1);
		}

		request1 = DenyOverride_PermitUnlessDeny();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
			Vector<Object> data1 = createVector("DenyOverrides",
					PolicyEvaluate(policy, request1) + "", "PermitUnlessDeny",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyOverrides", "null",
					"PemritUnlessDeny", "null", "");
			data.add(data1);
		}

		request1 = DenyOverride_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
			Vector<Object> data1 = createVector("DenyOverrides",
					PolicyEvaluate(policy, request1) + "", "FirstApplicable",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyOverrides", "null",
					"FirstApplicable", "null", "");
			data.add(data1);
		}

		pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
		return data;
	}

	public Vector<Vector<Object>> generateRequestsForPermitOverrides()
			throws IOException {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		PolicyEvaluation pe = new PolicyEvaluation();
		// ArrayList<QueryForDifference> requests = new
		// ArrayList<QueryForDifference>();
		String request1 = Deny_Permit_Override();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
			Vector<Object> data1 = createVector("PermitOverrides",
					PolicyEvaluate(policy, request1) + "", "DenyOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitOverrides", "null",
					"DenyOverrides", "null", "");
			data.add(data1);
		}
		request1 = PermitOverride_DenyUnlessPermit();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit");
			Vector<Object> data1 = createVector("PermitOverrides",
					PolicyEvaluate(policy, request1) + "", "DenyUnlessPermit",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitOverrides", "null",
					"DenyUnlessPermit", "null", "");
			data.add(data1);
		}

		request1 = PermitOverride_PermitUnlessDeny();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
			Vector<Object> data1 = createVector("PermitOverrides",
					PolicyEvaluate(policy, request1) + "", "PermitUnlessDeny",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitOverrides", "null",
					"PermitUnlessDeny", "null", "");
			data.add(data1);
		}
		request1 = PermitOverride_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
			Vector<Object> data1 = createVector("PermitOverrides",
					PolicyEvaluate(policy, request1) + "", "FirstApplicable",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitOverrides", "null",
					"FirstApplicalbe", "null", "");
			data.add(data1);
		}
		pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
		return data;
	}

	public Vector<Vector<Object>> generateRequestsForDenyUnlessPermit()
			throws IOException {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		PolicyEvaluation pe = new PolicyEvaluation();
		// ArrayList<QueryForDifference> requests = new
		// ArrayList<QueryForDifference>();
		String request1 = DenyOverride_DenyUnlessPermit();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
			Vector<Object> data1 = createVector("DenyUnlessPermit",
					PolicyEvaluate(policy, request1) + "", "DenyOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyUnlessPermit", "null",
					"DenyOverrides", "null", "");
			data.add(data1);
		}
		request1 = PermitOverride_DenyUnlessPermit();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
			Vector<Object> data1 = createVector("DenyUnlessPermit",
					PolicyEvaluate(policy, request1) + "", "PermitOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyUnlessPermit", "null",
					"PermitOverrides", "null", "");
			data.add(data1);
		}
		request1 = DenyUnlessPermit_PermitUnlessDeny();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
			Vector<Object> data1 = createVector("DenyUnlessPermit",
					PolicyEvaluate(policy, request1) + "", "PermitUnlessDeny",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyUnlessPermit", "null",
					"PermitUnlessDeny", "null", "");
			data.add(data1);
		}
		request1 = DenyUnlessPermit_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
			Vector<Object> data1 = createVector("DenyUnlessPermit",
					PolicyEvaluate(policy, request1) + "", "FirstApplicable",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyUnlessPermit", "null",
					"FirstApplicable", "null", "");
			data.add(data1);
		}
		pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit");
		return data;
	}

	public Vector<Vector<Object>> generateRequestsForPermitUnlessDeny()
			throws IOException {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		PolicyEvaluation pe = new PolicyEvaluation();
		// ArrayList<QueryForDifference> requests = new
		// ArrayList<QueryForDifference>();
		String request1 = DenyOverride_PermitUnlessDeny();
		// System.out.println("this is test" + PolicyEvaluate(policy, ""));
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
			Vector<Object> data1 = createVector("PermitUnlessDeny",
					PolicyEvaluate(policy, request1) + "", "DenyOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitUnlessDeny", "null",
					"DenyOverrides", "null", "");
			data.add(data1);
		}
		request1 = PermitOverride_PermitUnlessDeny();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
			Vector<Object> data1 = createVector("PermitUnlessDeny",
					PolicyEvaluate(policy, request1) + "", "PermitOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitUnlessDeny", "null",
					"PermitOverrides", "null", "");
			data.add(data1);
		}
		request1 = DenyUnlessPermit_PermitUnlessDeny();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit");
			Vector<Object> data1 = createVector("PermitUnlessDeny",
					PolicyEvaluate(policy, request1) + "", "DenyUnlessPermit",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitUnlessDeny", "null",
					"DenyUnlessPermit", "null", "");
			data.add(data1);
		}
		request1 = PermitUnlessDeny_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
			Vector<Object> data1 = createVector("PermitUnlessDeny",
					PolicyEvaluate(policy, request1) + "", "FirstApplicable",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitUnlessDeny", "null",
					"FirstApplicalbe", "null", "");
			data.add(data1);
		}
		pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
		return data;
	}

	public Vector<Vector<Object>> generateRequestsForFirstApplicable()
			throws IOException {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		PolicyEvaluation pe = new PolicyEvaluation();
		// ArrayList<QueryForDifference> requests = new
		// ArrayList<QueryForDifference>();
		System.out.println("DF START");
		String request1 = DenyOverride_FirstApplicable();
		System.out.println("DF DONE");
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
			Vector<Object> data1 = createVector("FirstApplicable",
					PolicyEvaluate(policy, request1) + "", "DenyOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("FirstApplicable", "null",
					"DenyOverrides", "null", "");
			data.add(data1);
			System.out.println("*********************************");
		}
		request1 = PermitOverride_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
			Vector<Object> data1 = createVector("FirstApplicable",
					PolicyEvaluate(policy, request1) + "", "PermitOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("FirstApplicable", "null",
					"PermitOverrides", "null", "");
			data.add(data1);
		}
		request1 = DenyUnlessPermit_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit");
			Vector<Object> data1 = createVector("FirstApplicable",
					PolicyEvaluate(policy, request1) + "", "DenyUnlessPermit",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("FirstApplicable", "null",
					"DenyUnlessPermit", "null", "");
			data.add(data1);
		}
		request1 = PermitUnlessDeny_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
			System.out.println("modify to pud here");
			Vector<Object> data1 = createVector("FirstApplicable",
					PolicyEvaluate(policy, request1) + "", "PermitUnlessDeny",
					pe.evaluation(request1), request1);
			System.out.println(data1.size());
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("FirstApplicable", "null",
					"PermitUnlessPermit", "null", "");
			data.add(data1);
		}
		pe.modifyAlg("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
		System.out.println(data.size() + "data size");
		return data;
	}

	ArrayList<MyAttr> attributes = new ArrayList<MyAttr>();

	public void generateTest(TestPanel testPanel) {
		PolicySpreadSheetTestSuite byOneTestSuite = new PolicySpreadSheetTestSuite(
				generate_OneTrue(testPanel), "GenTests/");
		byOneTestSuite.writeToExcelFile(testPanel
				.getTestsuiteXLSfileName("OneTrue"));

		PolicySpreadSheetTestSuite OnetrueOtherFalse = new PolicySpreadSheetTestSuite(
				generate_OneTrueOtherFalse(testPanel), "GenTests/");
		OnetrueOtherFalse.writeToExcelFile(testPanel
				.getTestsuiteXLSfileName("OneTrueOtherFalse"));

		PolicySpreadSheetTestSuite ByTwo = new PolicySpreadSheetTestSuite(
				generate_ByTwo(testPanel), "GenTests/");
		ByTwo.writeToExcelFile(testPanel.getTestsuiteXLSfileName("ByTwo"));

		PolicySpreadSheetTestSuite ByDenyPermit = new PolicySpreadSheetTestSuite(
				generate_ByDenyPermit(testPanel), "GenTests/");
		ByDenyPermit.writeToExcelFile(testPanel
				.getTestsuiteXLSfileName("PermitDenyCombine"));

	}

	public ArrayList<PolicySpreadSheetTestRecord> generate_OneTrue(
			TestPanel testPanel) {

		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		List<Rule> rules = getRuleFromPolicy(policy);
		function f = new function();
		int count = 1;
		int ruleNo = 0;
		File file = new File(testPanel.getTestOutputDestination("_Basic"));
		if (!file.isDirectory() && !file.exists()) {
			file.mkdir();
		} else {
			f.deleteFile(file);
		}

		for (Rule rule : rules) {
			if (rule.getCondition() == null && rule.getTarget() == null) {
				boolean success = generateDefaultRule(generator,
						testPanel, ruleNo, rules, count, "_Basic");
				if(success)
					count++;
				continue;
			}
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			sb.append(True_Target((Target) rule.getTarget(), collector) + "\n");
			sb.append(True_Condition(rule.getCondition(), collector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat == true) {
				System.out.println(nameMap.size() + " map size");
				try {
					z3.getValue(collector, nameMap);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(collector.size() + "   collector size");
				String request = f.print(collector);
				try {
					String path = testPanel.getTestOutputDestination("_Basic")
							+ File.separator + "request" + count + ".txt";

					FileWriter fw = new FileWriter(path);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(request);
					bw.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// generate target object
				PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
						PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + count,
						"request" + count + ".txt", request, "");
				generator.add(psstr);
				count++;
			}
			ruleNo++;
		}

		return generator;
	}

	public ArrayList<PolicySpreadSheetTestRecord> generate_OneTrueOtherFalse(
			TestPanel testPanel) {
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		List<Rule> rules = getRuleFromPolicy(policy);
		function f = new function();
		int count = 1;
		int ruleNo = 0;
		File file = new File(testPanel.getTestOutputDestination("_Exclusive"));
		if (!file.isDirectory() && !file.exists()) {
			file.mkdir();
		} else {
			f.deleteFile(file);
		}
		long startTime = System.currentTimeMillis();
		for (Rule rule : rules) {
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			if (rule.getCondition() == null && rule.getTarget() == null) {
				boolean success = generateDefaultRule(generator,
						testPanel, ruleNo, rules, count, "_Exclusive");
				if(success)
					count++;
				continue;
			}
			sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			sb.append(True_Target((Target) rule.getTarget(), collector) + "\n");
			sb.append(True_Condition(rule.getCondition(), collector) + "\n");
			for (Rule fRule : rules) {
				if (fRule.getId().equals(rule.getId()))
					break;
				if (f.isDefaultRule(fRule))
					continue;
				sb.append(FalseTarget_FalseCondition(fRule, collector) + "\n");
				// sb.append(False_Condition(fRule, collector) + "\n");
			}
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat == true) {
				try {
					z3.getValue(collector, nameMap);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String request = f.print(collector);
				try {
					String path = testPanel
							.getTestOutputDestination("_Exclusive")
							+ File.separator + "request" + count + ".txt";

					FileWriter fw = new FileWriter(path);
					BufferedWriter bw = new BufferedWriter(fw);
					bw.write(request);
					bw.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// generate target object
				PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
						PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + count,
						"request" + count + ".txt", request, "");
				generator.add(psstr);
				count++;
			}
			ruleNo++;
		}

		long endTime = System.currentTimeMillis();
		System.out.println("System runtime ： " + (endTime - startTime) + "ms");
		return generator;
	}

	public ArrayList<PolicySpreadSheetTestRecord> generate_ByTwo(
			TestPanel testPanel) {
		// two true, how about others?
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();

		List<Rule> rules = getRuleFromPolicy(policy);
		function f = new function();
		int count = 1;

		File file = new File(testPanel.getTestOutputDestination("_Pair"));
		if (!file.isDirectory() && !file.exists()) {
			file.mkdir();
		} else {
			f.deleteFile(file);
		}
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < rules.size() ; i++) {
			Rule rule1 = rules.get(i);
			for (int j  = i; j <rules.size() ; j++) {
				Rule rule2 = rules.get(j);
				if (rule1.getId().toString().equals(rule2.getId().toString())) {
					continue;
				}
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				StringBuffer sb = new StringBuffer();
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				sb.append(True_Target((Target) rule1.getTarget(), collector)
						+ "\n");
				sb.append(True_Condition(rule1.getCondition(), collector)
						+ "\n");
				sb.append(True_Target((Target) rule2.getTarget(), collector)
						+ "\n");
				sb.append(True_Condition(rule2.getCondition(), collector)
						+ "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat == true) {
					try {
						z3.getValue(collector, nameMap);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					String request = f.print(collector);
					try {
						String path = testPanel
								.getTestOutputDestination("_Pair")
								+ File.separator + "request" + count + ".txt";

						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					count++;
				}
			}

		}
		long endTime = System.currentTimeMillis();
		System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
		return generator;
	}

	public ArrayList<PolicySpreadSheetTestRecord> generate_ByDenyPermit(
			TestPanel testPanel) {
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		List<Rule> pRules = getPermitRuleFromPolicy(policy);
		List<Rule> dRules = getDenyRuleFromPolicy(policy);
		function f = new function();
		int count = 1;

		File file = new File(testPanel.getTestOutputDestination("_PDpair"));
		if (!file.isDirectory() && !file.exists()) {
			file.mkdir();
		} else {
			f.deleteFile(file);
		}
		long startTime = System.currentTimeMillis();
		for (Rule prule : pRules) {
			for (Rule drule : dRules) {
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				StringBuffer sb = new StringBuffer();
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				sb.append(True_Target((Target) prule.getTarget(), collector)
						+ "\n");
				sb.append(True_Condition(prule.getCondition(), collector)
						+ "\n");
				sb.append(True_Target((Target) drule.getTarget(), collector)
						+ "\n");
				sb.append(True_Condition(drule.getCondition(), collector)
						+ "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat == true) {
					try {
						z3.getValue(collector, nameMap);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					String request = f.print(collector);
					try {
						String path = testPanel
								.getTestOutputDestination("_PDpair")
								+ File.separator + "request" + count + ".txt";

						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					count++;
				}
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
		return generator;
	}


	public String getTargetAttribute(Target target,
			ArrayList<MyAttr> collector, MyAttr input) {
		StringBuffer sb = new StringBuffer();
		if (target != null) {
			for (AnyOfSelection anyofselection : target.getAnyOfSelections()) {
				StringBuilder orBuilder = new StringBuilder();
				for (AllOfSelection allof : anyofselection.getAllOfSelections()) {
					StringBuilder allBuilder = new StringBuilder();
					for (TargetMatch match : allof.getMatches()) {

						if (match.getEval() instanceof AttributeDesignator) {

							AttributeDesignator attribute = (AttributeDesignator) match
									.getEval();
							if (!attribute.getId().toString()
									.equals(input.getName())) {
								continue;
							}
							allBuilder.append(" ("
									+ al.returnFunction(match
											.getMatchFunction().encode()) + " "
									+ getName(attribute.getId().toString())
									+ " ");
							if (attribute.getType().toString()
									.contains("string")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll("\n", "");
								value = value.trim();
								allBuilder.append("\"" + value + "\")");
							}
							if (attribute.getType().toString()
									.contains("integer")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll("\n", "");
								value.trim();
								value = value.trim();
								allBuilder.append(value + ")");
							}
							getType(getName(attribute.getId().toString()),
									attribute.getType().toString());
							MyAttr myattr = new MyAttr(attribute.getId()
									.toString(), attribute.getCategory()
									.toString(), attribute.getType().toString());
							if (isExist(collector, myattr) == false) {
								collector.add(myattr);
							}
						}
					}
					allBuilder.insert(0, "(and ");
					allBuilder.append(")");
					orBuilder.append(allBuilder);
				}
				orBuilder.insert(0, "(or ");
				orBuilder.append(")");
				sb.append(orBuilder);
			}
			sb.insert(0, "(and ");
			sb.append(")");
			return sb.toString();
		}
		return "";
	}

	public String getTargetAttribute(Target target, ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		if (target != null) {
			for (AnyOfSelection anyofselection : target.getAnyOfSelections()) {
				StringBuilder orBuilder = new StringBuilder();
				for (AllOfSelection allof : anyofselection.getAllOfSelections()) {
					StringBuilder allBuilder = new StringBuilder();
					for (TargetMatch match : allof.getMatches()) {

						if (match.getEval() instanceof AttributeDesignator) {

							AttributeDesignator attribute = (AttributeDesignator) match
									.getEval();
							// System.out.println("********" +
							// attribute.getId().toString());
							allBuilder.append(" ("
									+ al.returnFunction(match
											.getMatchFunction().encode()) + " "
									+ getName(attribute.getId().toString())
									+ " ");
							if (attribute.getType().toString()
									.contains("string")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll("\n", "");
								value = value.trim();
								allBuilder.append("\"" + value + "\")");
							}
							if (attribute.getType().toString()
									.contains("integer")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll("\n", "");
								value.trim();
								value = value.trim();
								allBuilder.append(value + ")");
							}
							getType(getName(attribute.getId().toString()),
									attribute.getType().toString());
							MyAttr myattr = new MyAttr(attribute.getId()
									.toString(), attribute.getCategory()
									.toString(), attribute.getType().toString());
							if (isExist(collector, myattr) == false) {
								collector.add(myattr);
							}
						}
					}
					allBuilder.insert(0, " (and");
					allBuilder.append(")");
					orBuilder.append(allBuilder);
				}
				orBuilder.insert(0, " (or ");
				orBuilder.append(")");
				sb.append(orBuilder);
			}
			sb.insert(0, "(and ");
			sb.append(")");
			return sb.toString();
		}
		return "";
	}

	public StringBuffer ApplyStatements(Apply apply, String function,
			StringBuffer sb, ArrayList<MyAttr> collector) {
		if (apply.getFunction().encode()
				.contains("urn:oasis:names:tc:xacml:1.0:function:and")) {
			StringBuffer newsb = new StringBuffer();
			;
			for (Object element : apply.getList()) {
				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().toString(),
							newsb, collector);
				}
			}
			newsb.insert(0, "(and ");
			newsb.append(")");
			sb.append(newsb);
			return sb;
		} else if (apply.getFunction().encode()
				.contains("urn:oasis:names:tc:xacml:1.0:function:or")) {
			StringBuffer newsb = new StringBuffer();
			for (Object element : apply.getList()) {

				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().toString(),
							newsb, collector);
				}
			}
			newsb.insert(0, "(or ");
			newsb.append(")");
			sb.append(newsb);
			return sb;
		} else if (apply.getFunction().encode()
				.contains("urn:oasis:names:tc:xacml:1.0:function:not")) {
			StringBuffer newsb = new StringBuffer();
			for (Object element : apply.getList()) {
				if (element instanceof Apply) {

					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().toString(),
							newsb, collector);
				}
			}
			newsb.insert(0, "(not ");
			newsb.append(")");
			sb.append(newsb);
			return sb;
		} else if (apply.getFunction().encode().contains("string-is-in")) {
			String value = "";
			value = getAttrValue(apply);
			String functionName = al.returnFunction(apply.getFunction()
					.encode());
			sb = buildAttrDesignator(sb, apply, value, functionName, collector);
			return sb;
		} else if (apply.getFunction().encode()
				.contains("string-at-least-one-member-of")) {
			String value = "";
			String functionName = al.returnFunction(apply.getFunction()
					.encode());
			for (Object element : apply.getList()) {
				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					value = getAttrValue(childApply);
				}
			}
			sb = buildAttrDesignator(sb, apply, value, functionName, collector);
			return sb;
		} else {
			for (Object element : apply.getList()) {
				String value = null;
				if (element instanceof IntegerAttribute) {
					IntegerAttribute intValue = (IntegerAttribute) element;
					value = intValue.getValue() + "";
					sb.append(value + ")");

				}
				if (element instanceof StringAttribute) {
					StringAttribute stringValue = (StringAttribute) element;
					value = stringValue.getValue() + "";
					sb.append("\"" + value + "\")");
				}
				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().encode(),
							sb, collector);
				}
				if (element instanceof AttributeDesignator) {
					AttributeDesignator attributes = (AttributeDesignator) element;
					sb.append(" (" + al.returnFunction(function) + " "
							+ getName(attributes.getId().toString()) + " ");
					getType(getName(attributes.getId().toString()), attributes
							.getType().toString());
					MyAttr myattr = new MyAttr(attributes.getId().toString(),
							attributes.getCategory().toString(), attributes
									.getType().toString());
					if (isExist(collector, myattr) == false) {
						collector.add(myattr);
					}

				}
			}
		}
		return sb;
	}

	public String getAttrValue(Apply apply) {
		String value = "";
		for (Object element : apply.getList()) {
			if (element instanceof IntegerAttribute) {
				IntegerAttribute intValue = (IntegerAttribute) element;
				value = intValue.getValue() + ")";
				// sb.append(value + ")");

			}
			if (element instanceof StringAttribute) {
				StringAttribute stringValue = (StringAttribute) element;
				value = "\"" + stringValue.getValue() + "\")";
				// sb.append("\"" + value + "\")");
			}
		}
		return value;
	}

	public StringBuffer buildAttrDesignator(StringBuffer sb, Apply apply,
			String value, String function, ArrayList<MyAttr> collector) {
		for (Object element : apply.getList()) {
			if (element instanceof AttributeDesignator) {
				AttributeDesignator attributes = (AttributeDesignator) element;
				sb.append(" (" + function + " "
						+ getName(attributes.getId().toString()) + " " + value);
				getType(getName(attributes.getId().toString()), attributes
						.getType().toString());
				MyAttr myattr = new MyAttr(attributes.getId().toString(),
						attributes.getCategory().toString(), attributes
								.getType().toString());
				if (isExist(collector, myattr) == false) {
					collector.add(myattr);
				}
			}
		}
		return sb;
	}

	public String getConditionAttribute(Condition condition,
			ArrayList<MyAttr> collector) {
		if (condition != null) {
			Expression expression = condition.getExpression();
			StringBuffer sb = new StringBuffer();
			if (expression instanceof Apply) {
				Apply apply = (Apply) expression;
				sb = ApplyStatements(apply, "", sb, collector);
			}
			return sb.toString();
		}
		return "";
	}

	public boolean isExist(ArrayList<MyAttr> generation, MyAttr childAttr) {
		if (generation == null)
			return false;
		for (MyAttr it : generation) {
			if (it.getName().equals(childAttr.getName())) {
				return true;
			}
		}
		return false;
	}

	public void mergeAttribute(ArrayList<MyAttr> Globalattributes,
			ArrayList<MyAttr> Localattributes) {
		for (MyAttr localmyattr : Localattributes) {
			boolean found = false;
			for (MyAttr globalmyattr : Globalattributes) {
				if (localmyattr.getName().equals(globalmyattr.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				Globalattributes.add(localmyattr);
			}
		}
	}

	public void printAttribute(ArrayList<MyAttr> globalattributes) {
		for (MyAttr myattr : globalattributes) {
			System.out.println(myattr.toString());
		}
	}

	public String Deny_Permit_Override() throws IOException {
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		TruePolicyTarget(policy, collector);
		function f = new function();
		if (f.allDenyRule(policy) || f.allPermitRule(policy)) {
			return "";
		} else {
			List<Rule> permitRule = getPermitRuleFromPolicy(policy);
			List<Rule> denyRule = getDenyRuleFromPolicy(policy);
			for (Rule pRule : permitRule) {
				for (Rule dRule : denyRule) {
					StringBuffer sb = new StringBuffer();
					ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
					// condition 1: P & D
					sb.append(TruePolicyTarget(policy, localcollector) + "\n");
					sb.append(TrueTarget_TrueCondition(pRule, localcollector)
							+ "\n");
					sb.append(TrueTarget_TrueCondition(dRule, localcollector)
							+ "\n");
					boolean sat = z3str(sb.toString(), nameMap, typeMap);
					if (sat == true) {
						z3.getValue(localcollector, nameMap);
						String request = f.print(localcollector);
						System.out.println(request);
						System.out.println(PolicyEvaluate(policy, request));
						return request;
					}
				}
			}

			// Condition 2, P & ID
			for (Rule pRule : permitRule) {
				for (Rule dRule : denyRule) {
					boolean ind = checkIndeterminate(dRule, pRule);
					if (dRule.getCondition() == null) {
						continue;
					}
					if (ind == false) {
						continue;
					} else {
						StringBuffer sb = new StringBuffer();
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(pRule,
								localcollector) + "\n");
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// for indeterminate
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
							System.out.println(PolicyEvaluate(policy, request));
							return request;
						}
					}
				}
			}

			// Condition 3, IP & D
			for (Rule pRule : permitRule) {
				for (Rule dRule : denyRule) {
					boolean ind = checkIndeterminate(pRule, dRule);
					if (pRule.getCondition() == null) {
						continue;
					}
					if (ind == false) {
						continue;
					} else {
						StringBuffer sb = new StringBuffer();
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(dRule,
								localcollector) + "\n");
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						System.out.println(sat);
						if (sat == true) {
							// localcollector.add(ind);
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
							System.out.println(PolicyEvaluate(policy, request));
							return request;
						}
					}
				}
			}
		}
		return "";
	}

	public String DenyOverride_DenyUnlessPermit() throws IOException {
		function f = new function();
		if (f.checkDefaultRule(policy) && f.allDenyRule(policy)) {
			return "";
		}
		if (f.checkDefaultRule(policy) && f.allPermitRule(policy)) {
			return "";
		}

		// Condition1,first permit rule
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("deny")) {
			List<Rule> permitRule = getPermitRuleFromPolicy(policy);
			for (Rule pRule : permitRule) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
				sb.append(TruePolicyTarget(policy, localcollector) + "\n");
				sb.append(TrueTarget_TrueCondition(pRule, localcollector)
						+ "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat == true) {
					z3.getValue(localcollector, nameMap);
					String request = f.print(localcollector);
					System.out.println(PolicyEvaluate(policy, request));
					return request;
				}
			}
		}
		// Condition2, first deny rule
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("permit")) {
			List<Rule> denyRule = getDenyRuleFromPolicy(policy);
			for (Rule dRule : denyRule) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
				sb.append(TruePolicyTarget(policy, localcollector) + "\n");
				sb.append(TrueTarget_TrueCondition(dRule, localcollector)
						+ "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat == true) {
					z3.getValue(localcollector, nameMap);
					String request = f.print(localcollector);
					System.out.println(PolicyEvaluate(policy, request));
					return request;
				}
			}
		}
		// otherwise, all indeterminate
		// TODO how to make sure can make indeterminate
		boolean ind = allIndeterminate(policy);
		if (ind) {
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat == true) {
				z3.getValue(localcollector, nameMap);
				localcollector.add(invalidAttr()); // at least one attribute
				String request = f.print(localcollector);
				System.out.println(PolicyEvaluate(policy, request));
				return request;
			}
		}
		return "";
	}

	public String DenyOverride_PermitUnlessDeny() throws IOException {
		function f = new function();
		// theorem 7
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("deny")) {
			return "";
		}
		// theorem 8
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("permit")) {
			if (f.allPermitRule(policy)) {
				return "";
			} else {
				List<Rule> denyRule = getDenyRuleFromPolicy(policy);
				for (Rule dRule : denyRule) {
					ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
					StringBuffer sb = new StringBuffer();
					sb.append(TruePolicyTarget(policy, localcollector) + "\n");
					sb.append(True_Target((Target) dRule.getTarget(),
							localcollector) + "\n");
					boolean ind = oneRuleIndeterminate(dRule, policy);
					if (ind) {
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// localcollector.add(ind);
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
							System.out.println(PolicyEvaluate(policy, request));
							return request;
						}
					}
				}
			}
		}
		// otherwise, all indeterminate
		// TODO how to make sure can make indeterminate
		boolean ind = allIndeterminate(policy);
		if (ind) {
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat == true) {
				z3.getValue(localcollector, nameMap);
				localcollector.add(invalidAttr());
				String request = f.print(localcollector);
				System.out.println(PolicyEvaluate(policy, request));
				return request;
			}
		}
		return "";
	}

	public String DenyOverride_FirstApplicable() throws IOException {
		// TODO should have "or" relation here; tagged as "***"
		function f = new function();
		// theorem 9, all permit, i = ip, j = p
		if (f.allPermitRule(policy)) {
			System.out.println("All permit");
			List<Rule> permitRule = getPermitRuleFromPolicy(policy);
			for (int i = 0; i < permitRule.size(); i++) {
				for (int j = permitRule.size() - 1; j > i; j--) {
					boolean ind = checkIndeterminate(permitRule.get(i),
							permitRule.get(j));
					if (ind == false) {
						continue;
					} else {
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						StringBuffer sb = new StringBuffer();
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(permitRule.get(j),
								localcollector) + "\n");
						// TODO can not evaluate?
						// sb.append(True_Target(permitRule.get(i),
						// localcollector) + "\n");
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// localcollector.add(ind);
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
							System.out.println(request);
							System.out.println("Printl here");
							return request;
						}
					}
				}
			}
		} else {
			// theorem 10; at least have one deny rule;
			// condition a;
			List<Rule> Rule = getRuleFromPolicy(policy);
			for (int i = 0; i < Rule.size(); i++) {
				// if first rule is deny rule;
				if (Rule.get(i).getEffect() != 0) {
					for (int j = Rule.size() - 1; j > i; j--) {
						// no matter j is P or D rule, try id,p/d first
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						StringBuffer sb = new StringBuffer();
						// if second rule is deny, ID&D

						boolean ind = checkIndeterminate(Rule.get(i),
								Rule.get(j));
						if (ind == false) {
							continue;
						}
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(Rule.get(j),
								localcollector) + "\n");
						for (int k = 0; k < i; k++) {
							// TODO should not be "and" here
							sb.append(False_Target((Target) Rule.get(k)
									.getTarget(), localcollector)
									+ "\n");
							sb.append(False_Condition(Rule.get(k)
									.getCondition(), localcollector)
									+ "\n");
						}
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							if (f.isDefaultRule(Rule.get(j)))
								localcollector.add(invalidAttr());
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
							System.out.println(PolicyEvaluate(policy, request));
							return request;
						}
					}
					for (int j = Rule.size() - 1; j > i; j--) {
						// if failed, and j is P rule, try, id, ip

						if (Rule.get(j).getCondition() == null) {
							continue;
						}

						if (Rule.get(j).getEffect() == 0) {
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							boolean ind2 = oneRuleIndeterminate(Rule.get(j),
									policy);
							boolean ind = oneRuleIndeterminate(Rule.get(i),
									policy);
							if (ind2 == false || ind == false) {
								continue;
							}
							sb = new StringBuffer();
							localcollector = new ArrayList<MyAttr>();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							for (int k = 0; k < i; k++) {
								// TODO should not be "and" here
								sb.append(False_Target((Target) Rule.get(k)
										.getTarget(), localcollector)
										+ "\n");
								sb.append(False_Condition(Rule.get(k)
										.getCondition(), localcollector)
										+ "\n");
							}
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								localcollector.add(invalidAttr()); // in case it
																	// is empty
								z3.getValue(localcollector, nameMap);
								String request = f.print(localcollector);
								System.out.println(PolicyEvaluate(policy,
										request));
								return request;
							}

						}

					}

				} else {
					// first permit
					for (int j = Rule.size() - 1; j > i; j--) { // j for deny //
																// rule
						if (Rule.get(j).getEffect() != 1) {
							continue;
						} else {
							// condition 1, P&D
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							sb.append(TrueTarget_TrueCondition(Rule.get(j),
									localcollector) + "\n");
							sb.append(TrueTarget_TrueCondition(Rule.get(i),
									localcollector) + "\n");
							for (int k = 0; k < i; k++) {
								sb.append(False_Target((Target) Rule.get(k)
										.getTarget(), localcollector)
										+ "\n");
								sb.append(False_Condition(Rule.get(k)
										.getCondition(), localcollector)
										+ "\n");
							}
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								z3.getValue(localcollector, nameMap);
								String request = f.print(localcollector);
								System.out.println(PolicyEvaluate(policy,
										request));
								return request;
								// TODO here, need a test?
							} else {
								// condition 2 P&ID

								localcollector = new ArrayList<MyAttr>();
								sb = new StringBuffer();

								boolean ind = checkIndeterminate(Rule.get(j),
										Rule.get(i));
								if (ind == false) {
									continue;
								}
								sb.append(TruePolicyTarget(policy,
										localcollector) + "\n");
								sb.append(TrueTarget_TrueCondition(Rule.get(i),
										localcollector) + "\n");
								sat = z3str(sb.toString(), nameMap, typeMap);
								if (sat == true) {
									String request = f.print(localcollector);
									System.out.println(PolicyEvaluate(policy,
											request));
									return request;
								}
							}
						}
					}
					// first IP;
					if (Rule.get(i).getEffect() == 0) {
						for (int j = Rule.size() - 1; j > i; j--) {
							if (Rule.get(j).getEffect() == 0) {
								continue;
							}

							boolean ind = oneRuleIndeterminate(Rule.get(i),
									policy);
							boolean ind2 = oneRuleIndeterminate(Rule.get(j),
									policy);
							if (ind == false || ind2 == false) {
								continue;
							}
							// IP & ID
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								localcollector.add(invalidAttr()); // in case is
																	// empty
								z3.getValue(localcollector, nameMap);
								String request = f.print(localcollector);
								System.out.println(PolicyEvaluate(policy,
										request));
								return request;
							} else {
								// IP & D;
								boolean ind3 = checkIndeterminate(Rule.get(i),
										Rule.get(j));
								if (ind3 == false) {
									continue;
								}
								localcollector = new ArrayList<MyAttr>();
								sb = new StringBuffer();
								sb.append(TruePolicyTarget(policy,
										localcollector) + "\n");
								sb.append(TrueTarget_TrueCondition(Rule.get(j),
										localcollector) + "\n");
								sat = z3str(sb.toString(), nameMap, typeMap);
								if (sat == true) {
									if (f.isDefaultRule(Rule.get(j)))
										localcollector.add(invalidAttr());
									z3.getValue(localcollector, nameMap);
									String request = f.print(localcollector);
									System.out.println(PolicyEvaluate(policy,
											request));
									return request;
								}
							}
						}
					}
				}
			}

		}
		return "";
	}

	public String PermitOverride_DenyUnlessPermit() throws IOException {
		function f = new function();
		// theorem 7
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("permit")) {
			return "";
		}
		// theorem 8
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("deny")) {
			if (f.allDenyRule(policy)) {
				return "";
			} else {
				List<Rule> permitRule = getPermitRuleFromPolicy(policy);
				for (Rule pRule : permitRule) {
					ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
					StringBuffer sb = new StringBuffer();
					sb.append(TruePolicyTarget(policy, localcollector) + "\n");
					// sb.append(True_Target(dRule, localcollector) + "\n");
					boolean ind = oneRuleIndeterminate(pRule, policy);
					if (ind) {
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// localcollector.add(ind);
							z3.getValue(localcollector, nameMap);
							if (localcollector.size() == 0) {
								localcollector.add(invalidAttr());
							}
							String request = f.print(localcollector);
							System.out.println(PolicyEvaluate(policy, request));
							return request;
						}
					}
				}
			}
		}
		// otherwise, all indeterminate
		// TODO how to make sure can make indeterminate
		boolean ind = allIndeterminate(policy);
		if (ind) {
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat == true) {
				z3.getValue(localcollector, nameMap);
				localcollector.add(invalidAttr());
				String request = f.print(localcollector);
				System.out.println();
				System.out.println(PolicyEvaluate(policy, request));
				return request;
			}
		}
		return "";
	}

	public String PermitOverride_PermitUnlessDeny() throws IOException {
		function f = new function();
		if (f.checkDefaultRule(policy) && f.allDenyRule(policy)) {
			return "";
		}
		if (f.checkDefaultRule(policy) && f.allPermitRule(policy)) {
			return "";
		}

		// Condition1,first permit rule
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("deny")) {
			List<Rule> permitRule = getPermitRuleFromPolicy(policy);
			for (Rule pRule : permitRule) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
				sb.append(TruePolicyTarget(policy, localcollector) + "\n");
				sb.append(TrueTarget_TrueCondition(pRule, localcollector)
						+ "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat == true) {
					z3.getValue(localcollector, nameMap);
					String request = f.print(localcollector);
					System.out.println(PolicyEvaluate(policy, request));
					return request;
				}
			}
		}
		// Condition2, first deny rule
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("permit")) {
			List<Rule> denyRule = getDenyRuleFromPolicy(policy);
			for (Rule dRule : denyRule) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
				sb.append(TruePolicyTarget(policy, localcollector) + "\n");
				sb.append(TrueTarget_TrueCondition(dRule, localcollector)
						+ "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat == true) {
					z3.getValue(localcollector, nameMap);
					String request = f.print(localcollector);
					System.out.println(PolicyEvaluate(policy, request));
					return request;
				}
			}
		}
		// otherwise, all indeterminate
		// TODO how to make sure can make indeterminate
		boolean ind = allIndeterminate(policy);
		if (ind) {
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat == true) {
				z3.getValue(localcollector, nameMap);
				localcollector.add(invalidAttr());
				String request = f.print(localcollector);
				System.out.println(PolicyEvaluate(policy, request));
				return request;
			}
		}
		return "";
	}

	public String PermitOverride_FirstApplicable() throws IOException {
		function f = new function();
		// theorem 9, all deny, i = id, j = d
		if (f.allDenyRule(policy)) {
			List<Rule> denyRule = getDenyRuleFromPolicy(policy);
			for (int i = 0; i < denyRule.size(); i++) {

				for (int j = denyRule.size() - 1; j > 0; j--) {
					boolean ind = checkIndeterminate(denyRule.get(i),
							denyRule.get(j));
					if (ind == false) {
						continue;
					} else {
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						StringBuffer sb = new StringBuffer();
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(denyRule.get(j),
								localcollector) + "\n");
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// localcollector.add(ind);
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);

							System.out.println(PolicyEvaluate(policy, request));
							return request;
						}
					}
				}
			}
		} else {
			// theorem 10; at least have one permit rule;
			// condition a;
			List<Rule> Rule = getRuleFromPolicy(policy);
			for (int i = 0; i < Rule.size(); i++) {
				// if first rule is permit rule;
				if (Rule.get(i).getEffect() == 0) {
					if (f.isDefaultRule(Rule.get(i))) {
						continue;
					}
					for (int j = Rule.size() - 1; j > i; j--) {

						// no matter j is P or D rule, try ip,p/d first
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						StringBuffer sb = new StringBuffer();
						// if second rule is deny, ID&D
						boolean ind = checkIndeterminate(Rule.get(i),
								Rule.get(j));
						if (ind == false) {
							continue;
						}
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(Rule.get(j),
								localcollector) + "\n");
						for (int k = 0; k < i; k++) {
							// TODO should not be "and" here
							sb.append(False_Target((Target) Rule.get(k)
									.getTarget(), localcollector)
									+ "\n");
							sb.append(False_Condition(Rule.get(k)
									.getCondition(), localcollector)
									+ "\n");
						}
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// localcollector.add(ind);
							if (f.isDefaultRule(Rule.get(j))) {
								localcollector.add(invalidAttr());
							}
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
							System.out.println(RuleEvaluate(Rule.get(i),
									request) + "Result");
							return request;
						}
					}
					for (int j = Rule.size() - 1; j > i; j--) {
						// if failed, and j is Deny rule, try, ip, id
						if (Rule.get(j).getEffect() == 1) {
							if (f.isDefaultRule(Rule.get(j))) {
								continue;
							}
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							boolean ind2 = oneRuleIndeterminate(Rule.get(j),
									policy);
							boolean ind = oneRuleIndeterminate(Rule.get(i),
									policy);
							// boolean same = sameAttributes(Rule.get(i),
							// Rule.get(j));
							if (ind2 == false || ind == false) {
								continue;
							}

							sb = new StringBuffer();
							localcollector = new ArrayList<MyAttr>();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							for (int k = 0; k < i; k++) {
								// TODO should not be "and" here
								sb.append(False_Target((Target) Rule.get(k)
										.getTarget(), localcollector)
										+ "\n");
								sb.append(False_Condition(Rule.get(k)
										.getCondition(), localcollector)
										+ "\n");
							}
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								localcollector.add(invalidAttr());
								z3.getValue(localcollector, nameMap);
								localcollector.add(invalidAttr());
								String request = f.print(localcollector);
								return request;
							}

						}

					}

				} else {
					// first deny
					for (int j = Rule.size() - 1; j > i; j--) { // j for deny //
																// rule
						if (Rule.get(j).getEffect() == 1) {
							continue;
						} else {
							// condition 1, D&P
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							sb.append(TrueTarget_TrueCondition(Rule.get(j),
									localcollector) + "\n");
							sb.append(TrueTarget_TrueCondition(Rule.get(i),
									localcollector) + "\n");
							for (int k = 0; k < i; k++) {
								sb.append(False_Target((Target) Rule.get(k)
										.getTarget(), localcollector)
										+ "\n");
								sb.append(False_Condition(Rule.get(k)
										.getCondition(), localcollector)
										+ "\n");
							}
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								z3.getValue(localcollector, nameMap);
								String request = f.print(localcollector);
								return request;
								// TODO here, need a test?
							} else {
								// condition 2 D&IP
								if (f.isDefaultRule(Rule.get(j))) {
									continue;
								}
								localcollector = new ArrayList<MyAttr>();
								sb = new StringBuffer();

								boolean ind = checkIndeterminate(Rule.get(j),
										Rule.get(i));
								if (ind == false) {
									continue;
								}
								sb.append(TruePolicyTarget(policy,
										localcollector) + "\n");
								sb.append(TrueTarget_TrueCondition(Rule.get(i),
										localcollector) + "\n");
								sat = z3str(sb.toString(), nameMap, typeMap);
								if (sat == true) {
									if (f.isDefaultRule(Rule.get(i)))
										localcollector.add(invalidAttr());
									z3.getValue(localcollector, nameMap);
									String request = f.print(localcollector);
									return request;
								}
							}
						}
					}
				}
				// first ID;
				if (Rule.get(i).getEffect() == 1) {
					for (int j = Rule.size() - 1; j > i; j--) {
						if (Rule.get(j).getEffect() == 1) {
							continue;
						}

						boolean ind = oneRuleIndeterminate(Rule.get(i), policy);
						boolean ind2 = oneRuleIndeterminate(Rule.get(j), policy);
						if (ind == false || ind2 == false) {
							continue;
						}
						// ID & IP
						if (f.isDefaultRule(Rule.get(j))) {
							continue;
						} else {
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								System.out
										.println("here ---------------------");
								localcollector.add(invalidAttr());
								z3.getValue(localcollector, nameMap);
								String request = f.print(localcollector);
								return request;
							} else {
								// ID & P;

								boolean ind3 = checkIndeterminate(Rule.get(i),
										Rule.get(j));
								if (ind3 == false) {
									continue;
								}
								localcollector = new ArrayList<MyAttr>();
								sb = new StringBuffer();
								sb.append(TruePolicyTarget(policy,
										localcollector) + "\n");
								sb.append(TrueTarget_TrueCondition(Rule.get(j),
										localcollector) + "\n");
								sat = z3str(sb.toString(), nameMap, typeMap);
								if (sat == true) {
									if (f.isDefaultRule(Rule.get(j)))
										localcollector.add(invalidAttr());
									z3.getValue(localcollector, nameMap);
									String request = f.print(localcollector);
									return request;
								}
							}
						}

					}
				}
			}
		}

		return "";
	}

	public String DenyUnlessPermit_PermitUnlessDeny() throws IOException {

		function f = new function();
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("permit")) {
			if (f.allPermitRule(policy)) {
				return "";
			} else {
				List<Rule> denyRule = getDenyRuleFromPolicy(policy);
				for (Rule rule : denyRule) {
					if (f.isDefaultRule(rule))
						continue;
					ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
					StringBuffer sb = new StringBuffer();
					sb.append(TruePolicyTarget(policy, localcollector) + "\n");
					sb.append(TrueTarget_TrueCondition(rule, localcollector)
							+ "\n");
					boolean sat = z3str(sb.toString(), nameMap, typeMap);
					if (sat) {
						z3.getValue(localcollector, nameMap);
						String request = f.print(localcollector);
						return request;
					}
				}
			}
		} else if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("deny")) {
			if (f.allDenyRule(policy)) {
				return "";
			} else {
				List<Rule> permitRule = getPermitRuleFromPolicy(policy);
				for (Rule rule : permitRule) {
					if (f.isDefaultRule(rule))
						continue;
					ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
					StringBuffer sb = new StringBuffer();
					sb.append(TruePolicyTarget(policy, localcollector) + "\n");
					sb.append(TrueTarget_TrueCondition(rule, localcollector)
							+ "\n");
					boolean sat = z3str(sb.toString(), nameMap, typeMap);
					if (sat) {
						z3.getValue(localcollector, nameMap);
						String request = f.print(localcollector);
						return request;
					}
				}
			}
		} else {
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat) {
				if (localcollector.isEmpty())
					localcollector.add(invalidAttr());
				z3.getValue(localcollector, nameMap);
				String request = f.print(localcollector);
				return request;
			}
		}
		return "";
	}

	public String DenyUnlessPermit_FirstApplicable() throws IOException {
		function f = new function();
		List<Rule> rules = getRuleFromPolicy(policy);
		for (Rule rule : rules) {
			boolean ind = oneRuleIndeterminate(rule, policy);
			if (ind == false) {
				// System.out.println("condition one rule ind " + ind);
				continue;
			}
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			// System.out.println(sat + "condition sat");
			if (sat) {
				if (localcollector.size() == 0) {
					localcollector.add(invalidAttr());
				}
				z3.getValue(localcollector, nameMap);
				String request = f.print(localcollector);
				return request;
			}
		}
		return "";
	}

	public String PermitUnlessDeny_FirstApplicable() throws IOException {
		return DenyUnlessPermit_FirstApplicable();
	}

	public void initBalana(XPA xpa) {

		try {
			// using file based policy repository. so set the policy location as
			// system property
			String policyLocation = (new File(".")).getCanonicalPath()
					+ File.separator + "resources";
			System.setProperty(xpa.getWorkingPolicyFilePath(), policyLocation);
		} catch (IOException e) {
			System.err.println("Can not locate policy repository");
		}
		// create default instance of Balana
		balana = Balana.getInstance();
	}

	private XACML3EvaluationCtx getEvaluationCtx(String request) {
		RequestCtxFactory rc = new RequestCtxFactory();
		AbstractRequestCtx ar = null;
		try {
			ar = rc.getRequestCtx(request);
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XACML3EvaluationCtx ec;
		ec = new XACML3EvaluationCtx(new RequestCtx(ar.getAttributesSet(),
				ar.getDocumentRoot()), balana.getPdpConfig());
		return ec;
	}

	public int TargetEvaluate(Target target, String request) {
		// 0 = match, 1 = no match, 2 = ind
		MatchResult match = null;

		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);

		match = target.match(ec);
		return match.getResult();

	}

	public int ConditionEvaluate(Condition condition, String request) {
		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		EvaluationResult result = condition.evaluate(ec);
		if (result.indeterminate()) {
			return 2;
		} else {
			BooleanAttribute bool = (BooleanAttribute) (result
					.getAttributeValue());
			if (bool.getValue()) {
				// if any obligations or advices are defined, evaluates them and
				// return
				return 0;
			} else {
				return 1;
			}
		}

	}

	public ArrayList<Integer> MatchOfTarget(Target target, String request) {
		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		ArrayList<Integer> result = new ArrayList<Integer>();

		List<AnyOfSelection> anyOfSelections = target.getAnyOfSelections();
		if (anyOfSelections != null) {
			for (AnyOfSelection anyof : anyOfSelections) {
				List<AllOfSelection> allOfSelections = anyof
						.getAllOfSelections();
				if (allOfSelections != null) {
					for (AllOfSelection allof : allOfSelections) {
						for (TargetMatch targetmatch : allof.getMatches()) {
							MatchResult match = null;
							match = targetmatch.match(ec);
							result.add(match.getResult());
						}
					}
				}
			}
		}
		return result;
	}

	public int RuleEvaluate(Rule rule, String request) {
		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		rule.evaluate(ec);
		return rule.evaluate(ec).getDecision();
	}

	public int PolicyEvaluate(Policy policy, String request) {
		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		// System.out.print(request);ReadPolicy.getPDPconfig()
		return policy.evaluate(ec).getDecision();
	}

	public List<Rule> getRuleFromPolicy(Policy policy) {
		List<CombinerElement> childElements = policy.getChildElements();
		List<Rule> Elements = new ArrayList<Rule>();
		for (CombinerElement element : childElements) {
			PolicyTreeElement tree1 = element.getElement();
			Rule rule = null;
			if (tree1 instanceof Rule) {
				rule = (Rule) tree1;
				Elements.add(rule);
			}
		}
		return Elements;
	}

	public List<Rule> getPermitRuleFromPolicy(Policy policy) {
		List<CombinerElement> childElements = policy.getChildElements();
		List<Rule> permitElements = new ArrayList<Rule>();
		for (CombinerElement element : childElements) {
			PolicyTreeElement tree1 = element.getElement();
			Rule rule = null;
			if (tree1 instanceof Rule) {
				rule = (Rule) tree1;
				if (rule.getEffect() == 0)
					permitElements.add(rule);
			}
		}
		return permitElements;
	}

	public List<Rule> getDenyRuleFromPolicy(Policy policy) {
		List<CombinerElement> childElements = policy.getChildElements();
		List<Rule> permitElements = new ArrayList<Rule>();
		for (CombinerElement element : childElements) {
			PolicyTreeElement tree1 = element.getElement();
			Rule rule = null;
			if (tree1 instanceof Rule) {
				rule = (Rule) tree1;
				if (rule.getEffect() == 1)
					permitElements.add(rule);
			}
		}
		return permitElements;
	}

	public StringBuffer TruePolicyTarget(Policy policy,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		Target target = (Target) policy.getTarget();
		if (target != null) {
			sb.append(getTargetAttribute(target, collector));
		}
		if (sb.toString().equals("(and )")) {
			return new StringBuffer();
		}
		return sb;
	}

	public StringBuffer TrueTarget_TrueCondition(Rule rule,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(True_Target((Target) rule.getTarget(), collector));
		sb.append(True_Condition(rule.getCondition(), collector));
		return sb;
	}

	public StringBuffer TrueTarget_FalseCondition(Rule rule,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(True_Target((Target) rule.getTarget(), collector));
		sb.append(False_Condition(rule.getCondition(), collector));
		return sb;
	}

	public StringBuffer False_Target(Target target, ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTargetAttribute(target, collector));
		String[] lines = sb.toString().split("\n");
		StringBuffer output = new StringBuffer();
		for (String s : lines) {
			if (s.isEmpty()) {
				continue;
			} else {
				StringBuffer subsb = new StringBuffer();
				subsb.append("(not ");
				subsb.append(s);
				subsb.append(")");
				output.append(subsb);
			}
		}
		return output;
	}

	public StringBuffer FalseTarget_FalseCondition(Rule rule,
			ArrayList<MyAttr> collector) {
		StringBuffer targetsb = new StringBuffer();
		StringBuffer conditionsb = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		Target target = (Target) rule.getTarget();
		targetsb.append(getTargetAttribute(target, collector));
		conditionsb
				.append(getConditionAttribute(rule.getCondition(), collector));
		sb.append("(not (and ");
		sb.append(targetsb);
		sb.append(conditionsb);
		sb.append("))");
		return sb;
	}

	public StringBuffer True_Target(Target target, ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTargetAttribute(target, collector));
		sb.append("\n");
		return sb;
	}

	public StringBuffer Ind_Target(Rule rule, ArrayList<MyAttr> collector,
			MyAttr attr) {
		StringBuffer sb = new StringBuffer();
		Target target = (Target) rule.getTarget();
		sb.append(getTargetAttribute(target, collector, attr));
		sb.append("\n");
		return sb;
	}

	public StringBuffer False_Condition(Condition condition,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getConditionAttribute(condition, collector));
		String[] lines = sb.toString().split("\n");
		StringBuffer output = new StringBuffer();
		for (String s : lines) {
			if (s.isEmpty()) {
				continue;
			} else {
				StringBuffer subsb = new StringBuffer();
				subsb.append("(not ");
				subsb.append(s);
				subsb.append(")");
				output.append(subsb);
			}
		}
		return output;
	}

	public StringBuffer True_Condition(Condition condition,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getConditionAttribute(condition, collector));
		sb.append("\n");
		return sb;
	}

	private boolean checkIndeterminate(Rule rule1, Rule rule2) {

		ArrayList<MyAttr> base = new ArrayList<MyAttr>();
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		ArrayList<MyAttr> target = new ArrayList<MyAttr>();
		TrueTarget_TrueCondition(rule1, collector);
		TrueTarget_TrueCondition(rule2, base);
		TruePolicyTarget(policy, target);
		for (MyAttr c : collector) {
			for (MyAttr b : base) {
				if (c.getName().toString().equals(b.getName().toString())) {
					return false;
				} else {
					for (MyAttr t : target) {
						if (c.getName().toString()
								.equals(t.getName().toString())) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	public boolean oneRuleIndeterminate(Rule rule, Policy policy) {
		// TODO
		// problem here, what if there is only one condition attr in this rule?
		// Indeterminate -> NA
		// if (rule.getCondition() == null) {
		// return false;
		// }

		ArrayList<MyAttr> temp = new ArrayList<MyAttr>();
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		TrueTarget_TrueCondition(rule, temp);
		TruePolicyTarget(policy, collector);
		if (collector.size() == 0) {
			return true;
		}
		for (MyAttr c : collector) {
			for (MyAttr t : temp) {
				if (c.getName().toString().equals(t.getName().toString())) {
					return false;
				}
			}

		}
		return true;
	}

	private MyAttr invalidAttr() {
		MyAttr myattr = new MyAttr(randomAttribute(), randomAttribute(),
				"http://www.w3.org/2001/XMLSchema#string");
		myattr.addValue("Indeterminate");
		return myattr;
	}

	private String randomAttribute() {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	public boolean z3str(String input, HashMap nameMap, HashMap typeMap) {
		z3.buildZ3Input(input, nameMap, typeMap);
		z3.buildZ3Output();
		if (z3.checkConflict() == true) {
			return true;
		} else {
			return false;
		}
	}

	public boolean allIndeterminate(Policy policy) {
		// return one unique attribute that not existing in any rules
		List<CombinerElement> childElements = policy.getChildElements();
		List<Rule> ruleElements = new ArrayList<Rule>();
		ArrayList<MyAttr> attributes = new ArrayList<MyAttr>();
		ArrayList<MyAttr> temps = new ArrayList<MyAttr>(); // store existing
															// attributes

		for (CombinerElement element : childElements) {
			PolicyTreeElement tree = element.getElement();
			Rule rule = null;
			if (tree instanceof Rule) {
				rule = (Rule) tree;
				ruleElements.add(rule);
			}
		}
		ArrayList<MyAttr> target = new ArrayList<MyAttr>();
		TruePolicyTarget(policy, target);
		for (Rule rule : ruleElements) {
			ArrayList<MyAttr> child = new ArrayList<MyAttr>();
			TrueTarget_TrueCondition(rule, child);

			outer: for (MyAttr c : temps) {
				for (MyAttr t : target) {
					if (!c.getName().toString().equals(t.getName().toString())) {
						break outer;
					}
					return false;
				}
			}
		}
		return true;
	}

	private void removeFromArray(ArrayList<MyAttr> collector, MyAttr myattr) {
		for (MyAttr coll : collector) {
			if (myattr.getName().equals(coll.getName().toString())) {
				collector.remove(coll);
			}
		}
	}

	public class ConRecord {
		public int effect;
		public int covered;

		public ConRecord(int effect, int covered) {
			this.effect = effect;
			this.covered = covered;
		}

		public int getEffect() {
			return this.effect;
		}

		public int getCovered() {
			return this.covered;
		}

		public void setCovered(int covered) {
			this.covered = covered;
		}
	}

	public class TarRecord {
		private ArrayList<Integer> basics;
		private ArrayList<String> tokens;
		public int effect; // 0=T, 1=F, 2=E;
		public int covered; // 0 = not covered, 1 = covered

		public TarRecord(int effect, int covered) {
			this.effect = effect;
			this.covered = covered;
		}

		public TarRecord(ArrayList<Integer> basics, ArrayList<String> tokens,
				int effect, int covered) {
			this(effect, covered);
			this.basics = basics;
			this.tokens = tokens;
			// this.effect = -1;
		}

		public int getCovered() {
			return this.covered;
		}

		public void setCovered(int covered) {
			this.covered = covered;
		}

		public int getEffect() {
			return this.effect;
		}

		public ArrayList<Integer> getArray() {
			return this.basics;
		}

		public ArrayList<String> getTokens() {
			return this.tokens;
		}

		public void setEffect(int effect) {
			this.effect = effect;
		}

		// build z3_input from the arrailists
		public String buildZ3(MCDC_converter2 converter) {
			StringBuffer sb = new StringBuffer();
			if (tokens != null && tokens.size() > 0) {
				sb.append("(and ");
				for (int i = 0; i < tokens.size(); i++) {
					System.out.println(getTokens().get(i).toString());
					if (getArray().get(i).equals(1)) {
						sb.append("(not ");
						sb.append(converter.getMap().get(getTokens().get(i))
								.toString()
								+ " )");
					} else {
						sb.append(converter.getMap().get(getTokens().get(i))
								.toString()
								+ " ");
					}
				}
				sb.append(")");
			}
			return sb.toString();
		}
	}

	public class RuleRecord {
		// if target or condition is null, the size is 0;
		private ArrayList<TarRecord> target;
		private ArrayList<ConRecord> condition;

		public RuleRecord() {
			target = new ArrayList<TarRecord>();
			condition = new ArrayList<ConRecord>();
		}

		public void addTarget(TarRecord tar) {
			this.target.add(tar);
		}

		public ArrayList<TarRecord> getTarget() {
			return this.target;
		}

		public void addCondition(ConRecord con) {
			this.condition.add(con);
		}

		public ArrayList<ConRecord> getCondition() {
			return this.condition;
		}
	}

	public class PolicyTable {
		private ArrayList<TarRecord> target;
		private ArrayList<RuleRecord> ruleRecord;

		public PolicyTable() {
			ruleRecord = new ArrayList<RuleRecord>();
			target = new ArrayList<TarRecord>();
		}

		public void addTarget(TarRecord record) {
			target.add(record);
		}

		public void addRule(RuleRecord record) {
			ruleRecord.add(record);
		}

		public ArrayList<TarRecord> getTarget() {
			return this.target;
		}

		public ArrayList<RuleRecord> getRules() {
			return this.ruleRecord;
		}
	}

	public PolicyTable buildDecisionCoverage(Policy policy) {
		PolicyTable policytable = new PolicyTable();
		List<Rule> rules = getRuleFromPolicy(policy);
		Target target = (Target) policy.getTarget(); // get policy target
		if (target != null) {
			TarRecord record = new TarRecord(2, 0);
			policytable.addTarget(record);
			record = new TarRecord(1, 0);
			policytable.addTarget(record);
			record = new TarRecord(0, 0);
			policytable.addTarget(record);
		}
		for (Rule rule : rules) {
			if (isDefaultRule(rule)) {
				continue;
			}
			RuleRecord rulerecord = new RuleRecord();
			target = (Target) rule.getTarget();
			Condition condition = (Condition) rule.getCondition();
			if (target != null) {
				TarRecord tar = new TarRecord(2, 0);
				rulerecord.addTarget(tar);
				tar = new TarRecord(1, 0);
				rulerecord.addTarget(tar);
				tar = new TarRecord(0, 0);
				rulerecord.addTarget(tar);
			} else {
				// even is empty, need to go next;
				TarRecord tar = new TarRecord(0, 0);
				rulerecord.addTarget(tar);
			}
			if (condition != null) {
				ConRecord con = new ConRecord(2, 0);
				rulerecord.addCondition(con);

				con = new ConRecord(1, 0);
				rulerecord.addCondition(con);

				con = new ConRecord(0, 0);
				rulerecord.addCondition(con);
			}
			policytable.addRule(rulerecord);
		}
		return policytable;
	}
	
	public PolicyTable buildDecisionCoverage_NoId(Policy policy){
		PolicyTable policytable = new PolicyTable();
		List<Rule> rules = getRuleFromPolicy(policy);
		Target target = (Target) policy.getTarget(); // get policy target
		if (target != null) {
			TarRecord record ;
			record = new TarRecord(1, 0);
			policytable.addTarget(record);
			record = new TarRecord(0, 0);
			policytable.addTarget(record);
		}
		for (Rule rule : rules) {
			if (isDefaultRule(rule)) {
				continue;
			}
			RuleRecord rulerecord = new RuleRecord();
			target = (Target) rule.getTarget();
			Condition condition = (Condition) rule.getCondition();
			if (target != null) {
				TarRecord tar ;
				tar = new TarRecord(1, 0);
				rulerecord.addTarget(tar);
				tar = new TarRecord(0, 0);
				rulerecord.addTarget(tar);
			} else {
				// even is empty, need to go next;
				TarRecord tar = new TarRecord(0, 0);
				rulerecord.addTarget(tar);
			}
			if (condition != null) {
				ConRecord con ;

				con = new ConRecord(1, 0);
				rulerecord.addCondition(con);

				con = new ConRecord(0, 0);
				rulerecord.addCondition(con);
			}
			policytable.addRule(rulerecord);
		}
		return policytable;
	}

	public ArrayList<PolicySpreadSheetTestRecord> generate_DecisionCoverage(
			TestPanel testPanel, PolicyTable policytable, String fileName) {
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		function f = new function();
		List<Rule> rules = getRuleFromPolicy(policy);
		ArrayList<TarRecord> trecord = policytable.getTarget();

		File file = new File(
				testPanel.getTestOutputDestination(fileName));
		if (!file.isDirectory() && !file.exists()) {
			file.mkdir();
		} else {
			f.deleteFile(file);
		}

		long startTime = System.currentTimeMillis();

		int count = 1;
		for (TarRecord ptarget : trecord) {
			if (ptarget.getEffect() == 2 && ptarget.getCovered() == 0) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				if (collector.size() > 0){
					collector.remove(0);
				collector.add(invalidAttr());
				}
				// in case is empty
				else{
					continue;
				}
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat) {
					try {
						z3.getValue(collector, nameMap);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ptarget.setCovered(1);
					String request = f.print(collector);
					System.out.println(request);
					try {
						String path = testPanel
								.getTestOutputDestination(fileName)
								+ File.separator + "request" + count + ".txt";
						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// generate target object
					PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					ptarget.setCovered(1);
					count++;

				}
			} else if (ptarget.getEffect() == 1 && ptarget.getCovered() == 0) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				sb.append(False_Target((Target) policy.getTarget(), collector));
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat) {
					try {
						z3.getValue(collector, nameMap);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ptarget.setCovered(1);
					System.out.println("I am here");
					String request = f.print(collector);
					System.out.println(request);
					try {
						String path = testPanel
								.getTestOutputDestination(fileName)
								+ File.separator + "request" + count + ".txt";
						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// generate target object
					PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					ptarget.setCovered(1);
					count++;

				}

			} else if (ptarget.getEffect() == 0) {
				// if policy target is true
				for (int i = rules.size() - 1; i >= 0; i--) {
					if (isDefaultRule(rules.get(i))) {
						boolean success = generateDefaultRule(generator,
								testPanel, i, rules, count, fileName);
						if(success)
							count++;
						continue;
					}

					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					StringBuffer prefix = new StringBuffer();
					for (int k = 0; k < i; k++) {
						prefix.append(FalseTarget_FalseCondition(rules.get(k),
								collector) + "\n");
					}
					RuleRecord ruleRecord = policytable.getRules().get(i);
					for (TarRecord rtarget : ruleRecord.getTarget()) {
						if (rtarget.getEffect() == 2
								&& rtarget.getCovered() == 0) { // rule target
																// is ind
							boolean getOne = generateIndTarget(generator,
									testPanel, policytable, rules.get(i),
									collector, count, i, prefix,
									fileName, rtarget, true);
							if (getOne)
								count++;
						} else if (rtarget.getEffect() == 1
								&& rtarget.getCovered() == 0) {
							StringBuffer sb = new StringBuffer();
							// ArrayList<MyAttr> collector = new
							// ArrayList<MyAttr>();
							System.out.println("false target for rule: " + i);
							sb.append(TruePolicyTarget(policy, collector)
									+ "\n");
							sb.append(False_Target((Target) rules.get(i)
									.getTarget(), collector)
									+ "\n");
							sb.append(prefix);
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							System.out.println(sat);
							if (sat) {
								try {
									z3.getValue(collector, nameMap);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								rtarget.setCovered(1);
								String request = f.print(collector);
								updateDecisionTable(policy, policytable,
										request, i);
								System.out.println(request);
								try {
									String path = testPanel
											.getTestOutputDestination(fileName)
											+ File.separator
											+ "request"
											+ count + ".txt";
									FileWriter fw = new FileWriter(path);
									BufferedWriter bw = new BufferedWriter(fw);
									bw.write(request);
									bw.close();

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								// generate target object
								PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
										PolicySpreadSheetTestSuite.TEST_KEYWORD
												+ " " + count, "request"
												+ count + ".txt", request, "");
								generator.add(psstr);
								count++;
							}

						} else if (rtarget.getEffect() == 0) {
							// if rule target is true
							if (ruleRecord.getCondition().size() == 0) {
								StringBuffer sb = new StringBuffer();
								sb.append(True_Target((Target) rules.get(i)
										.getTarget(), collector)
										+ "\n");
								sb.append(TruePolicyTarget(policy, collector)
										+ "\n");
								sb.append(prefix);
								boolean sat = z3str(sb.toString(), nameMap,
										typeMap);
								if (sat) {
									try {
										z3.getValue(collector, nameMap);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									rtarget.setCovered(1);
									String request = f.print(collector);
									System.out.println(request);
									System.out.println(i);
									updateDecisionTable(policy, policytable,
											request, i);
									try {
										String path = testPanel
												.getTestOutputDestination(fileName)
												+ File.separator
												+ "request"
												+ count + ".txt";
										FileWriter fw = new FileWriter(path);
										BufferedWriter bw = new BufferedWriter(
												fw);
										bw.write(request);
										bw.close();

									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									// generate target object
									PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
											PolicySpreadSheetTestSuite.TEST_KEYWORD
													+ " " + count, "request"
													+ count + ".txt", request,
											"");
									generator.add(psstr);
									count++;
								}

							} else {
								for (ConRecord rcondition : ruleRecord
										.getCondition()) {
									if (rcondition.getEffect() == 2
											&& rcondition.getCovered() == 0) {
										// TODO
										boolean getOne = generateIndCondition(
												generator, testPanel,
												policytable, rules.get(i),
												collector, count, i, prefix,
												fileName,
												rcondition, true);
										if (getOne)
											count++;
									} else if (rcondition.getEffect() == 1
											&& rcondition.getCovered() == 0) {
										StringBuffer sb = new StringBuffer();
										// ArrayList<MyAttr> collector = new
										// ArrayList<MyAttr>();
										sb.append(TruePolicyTarget(policy,
												collector) + "\n");
										sb.append(True_Target((Target) rules
												.get(i).getTarget(), collector)
												+ "\n");
										sb.append(False_Condition(rules.get(i)
												.getCondition(), collector)
												+ "\n");
										sb.append(prefix + "\n");

										boolean sat = z3str(sb.toString(),
												nameMap, typeMap);
										if (sat) {
											try {
												z3.getValue(collector, nameMap);
											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											rcondition.setCovered(1);
											String request = f.print(collector);
											updateDecisionTable(policy,
													policytable, request, i);
											try {
												String path = testPanel
														.getTestOutputDestination(fileName)
														+ File.separator
														+ "request"
														+ count
														+ ".txt";
												FileWriter fw = new FileWriter(
														path);
												BufferedWriter bw = new BufferedWriter(
														fw);
												bw.write(request);
												bw.close();

											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											// generate target object
											PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
													PolicySpreadSheetTestSuite.TEST_KEYWORD
															+ " " + count,
													"request" + count + ".txt",
													request, "");
											generator.add(psstr);
											count++;
										}

									} else if (rcondition.getEffect() == 0
											&& rcondition.getCovered() == 0) {
										StringBuffer sb = new StringBuffer();
										// ArrayList<MyAttr> collector = new
										// ArrayList<MyAttr>();
										sb.append(TruePolicyTarget(policy,
												collector) + "\n");
										sb.append(True_Target((Target) rules
												.get(i).getTarget(), collector)
												+ "\n");
										sb.append(True_Condition(rules.get(i)
												.getCondition(), collector)
												+ "\n");
										sb.append(prefix + "\n");
										boolean sat = z3str(sb.toString(),
												nameMap, typeMap);
										if (sat) {
											try {
												z3.getValue(collector, nameMap);
											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											rcondition.setCovered(1);
											String request = f.print(collector);
											// updateDecisionTable(policy,
											// policytable, request, i);
											try {
												String path = testPanel
														.getTestOutputDestination(fileName)
														+ File.separator
														+ "request"
														+ count
														+ ".txt";
												FileWriter fw = new FileWriter(
														path);
												BufferedWriter bw = new BufferedWriter(
														fw);
												bw.write(request);
												bw.close();

											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											// generate target object
											PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
													PolicySpreadSheetTestSuite.TEST_KEYWORD
															+ " " + count,
													"request" + count + ".txt",
													request, "");
											generator.add(psstr);
											count++;
										}
									}
								}
								rtarget.setCovered(1);
							}
						}
					}
				}
				ptarget.setCovered(1);
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("程序运行时间： " + (endTime - startTime) + "ms");

		for (TarRecord ttrecord : policytable.getTarget()) {
			if (ttrecord.covered == 0) {
				System.out.println("PolicyTarget not covered: "
						+ ttrecord.getEffect());
			}
		}
		int ii = 1;
		for (RuleRecord rrrecord : policytable.getRules()) {
			for (TarRecord ttrecord : rrrecord.getTarget()) {
				if (ttrecord.covered == 0) {
					System.out.println("RuleTarget not covered: " + ii
							+ " effect: " + ttrecord.getEffect());
				}
			}
			for (ConRecord ccrecord : rrrecord.getCondition()) {
				if (ccrecord.covered == 0) {
					System.out.println("RuleCondition not covered: " + ii
							+ " effect: " + ccrecord.getEffect());
				}
			}
			ii++;
		}
		return generator;
	}

	private StringBuffer makeAvailability(List<Rule> rules, int position,
			StringBuffer sb, ArrayList<MyAttr> collector) {
		for (int i = 0; i < position; i++) {
			Rule rule = rules.get(i);
			if (rule.getEffect() == rules.get(position).getEffect()) {
				if (rule.getTarget() != null) {
					sb.append(False_Target((Target) rule.getTarget(), collector));
					sb.append("\n");
					continue;
				} else if (rule.getCondition() != null) {
					sb.append(False_Condition(rule.getCondition(), collector));
					sb.append("\n");
					continue;
				}
			}
		}
		return sb;
	}

	public void updateDecisionTable(Policy policy, PolicyTable policytable,
			String request, int start) {
		List<Rule> rules = getRuleFromPolicy(policy);
		for (int i = 0; i < start; i++) {
			if (isDefaultRule(rules.get(i))) {
				continue;
			}
			Rule rule = rules.get(i);
			int tResult = -1;
			int cResult = -1;
			if (rule.getTarget() != null) {
				tResult = TargetEvaluate((Target) rule.getTarget(), request);
			}
			if (rule.getCondition() != null) {
				cResult = ConditionEvaluate(rule.getCondition(), request);
			}
			// get rule record
			RuleRecord record = policytable.getRules().get(i);
			// first doesn't have target
			if (rule.getTarget() == null) {
				for (ConRecord conRecord : record.getCondition()) {
					if (conRecord.getEffect() == cResult
							&& conRecord.getCovered() == 0) {
						conRecord.setCovered(1);
					}
				}
			} else { // has target
				for (TarRecord tarRecord : record.getTarget()) {
					if (tarRecord.getEffect() == tResult
							&& tarRecord.getCovered() == 0) {
						if (tResult != 0) {
							tarRecord.setCovered(1);

						} else {
							for (ConRecord conRecord : record.getCondition()) {
								if (conRecord.getEffect() == cResult
										&& conRecord.getCovered() == 0) {
									conRecord.setCovered(1);
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean arrayMatch(ArrayList<Integer> arry1,
			ArrayList<Integer> arry2) {
		if (arry1.size() != arry2.size() || arry1 == null || arry2 == null) {
			return false;
		} else {
			for (int i = 0; i < arry1.size(); i++) {
				if (!arry1.get(i).equals(arry2.get(i))) {
					return false;
				}
			}
		}
		return true;
	}

	private void printarray(ArrayList<Integer> arr) {
		for (int i = 0; i < arr.size(); i++) {
			System.out.print(arr.get(i));
		}
		System.out.println();
	}

	private void updateMCDCTable(Policy policy, PolicyTable policytable,
			String request, int start) {
		List<Rule> rules = getRuleFromPolicy(policy);
		for (int i = 0; i < start; i++) {
			if (isDefaultRule(rules.get(i))) {
				continue;
			}
			Rule rule = rules.get(i);
			ArrayList<Integer> tResult = new ArrayList<Integer>();
			int tResult2 = -1; // for ind
			int cResult = -1;
			if (rule.getTarget() != null) {
				tResult = MatchOfTarget((Target) rule.getTarget(), request);

				// printarray(tResult);
				tResult2 = TargetEvaluate((Target) rule.getTarget(), request);
			}
			if (rule.getCondition() != null) {
				cResult = ConditionEvaluate(rule.getCondition(), request);
			}
			// get rule record
			RuleRecord record = policytable.getRules().get(i);
			// first doesn't have target
			if (rule.getTarget() == null) {
				for (ConRecord conRecord : record.getCondition()) {
					if (conRecord.getEffect() == cResult
							&& conRecord.getCovered() == 0) {
						conRecord.setCovered(1);
					}
				}
			} else { // has target
				for (TarRecord tarRecord : record.getTarget()) {
					if (tarRecord.getCovered() == 1)
						continue;
					if ((tarRecord.effect == tResult2) && tarRecord.effect == 2) {
						tarRecord.setCovered(1);
					}
					if (tarRecord.getArray() != null
							&& arrayMatch(tResult, tarRecord.getArray())) {
						if (tarRecord.getEffect() == 1) {
							tarRecord.setCovered(1);
						} else {
							for (ConRecord conRecord : record.getCondition()) {
								if (conRecord.getEffect() == cResult
										&& conRecord.getCovered() == 0) {
									//conRecord.setCovered(1);
								}
							}
						}
					}
				}
			}
		}
	}

	public boolean isDefaultRule(Rule rule) {
		if (rule.getCondition() == null && rule.getTarget() == null) {
			return true;
		} else {
			return false;
		}
	}

	public PolicyTable buildMCDC_Table(Policy policy, MCDC_converter2 converter) {
		PolicyTable policytable = new PolicyTable();
		List<Rule> rules = getRuleFromPolicy(policy);
		// Target target = (Target) policy.getTarget();

		if ((Target) policy.getTarget() != null) {
			policytable.addTarget(new TarRecord(2, 0)); // add ind manually

			ArrayList<String> order = new ArrayList<String>();
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector));
			String mcdc_input = converter.convert(sb.toString());

			String[] s = mcdc_input.split(" ");
			for (int p = 0; p < s.length; p++) {
				if (!s[p].endsWith("(") && !s[p].endsWith(")")
						&& !s[p].endsWith("&&"))
					order.add(s[p]);
			}

			if (!mcdc_input.trim().equals("")) {
				MCDCConditionSet mcdcset = new MCDCConditionSet(mcdc_input);
				ArrayList<String> positives = mcdcset.getPositiveConditions();
				ArrayList<String> negatives = mcdcset.getNegativeConditions();
				for (String result : negatives) {
					System.out.println("result :" + result);
					policytable.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 1, 0));
				}
				for (String result : positives) {
					System.out.println("result :" + result);
					policytable.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 0, 0));
				}
			}
			policytable.addTarget(new TarRecord(0, 0));
			// add an dumb target to policy target
		}

		for (Rule rule : rules) {
			if (isDefaultRule(rule)) {
				continue;
			}
			RuleRecord ruleRecord = new RuleRecord();
			// first target
			if (rule.getTarget() != null) {
				ruleRecord.addTarget(new TarRecord(2, 0)); // add ind

				ArrayList<String> order = new ArrayList<String>();

				Target rTarget = (Target) rule.getTarget();
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				sb.append(True_Target(rTarget, collector));
				String mcdc_input = converter.convert(sb.toString());
				System.out.println("mcdc input here: " + mcdc_input);
				String[] s = mcdc_input.split(" ");
				for (int p = 0; p < s.length; p++) {
					if (!s[p].endsWith("(") && !s[p].endsWith(")")
							&& !s[p].endsWith("&&"))
						order.add(s[p]);
				}
				System.out.println(printArray(order, 1));
				MCDCConditionSet mcdcset = new MCDCConditionSet(mcdc_input);
				ArrayList<String> positives = mcdcset.getPositiveConditions();
				ArrayList<String> negatives = mcdcset.getNegativeConditions();
				for (String result : negatives) {
					System.out.println("result :" + result);

					ruleRecord.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 1, 0));
				}
				for (String result : positives) {
					System.out.println("result :" + result);
					ruleRecord.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 0, 0));
				}

			} else {
				ruleRecord.addTarget(new TarRecord(0, 0));
				// need to go on
			}

			if (rule.getCondition() != null) {
				ConRecord con = new ConRecord(2, 0);
				ruleRecord.addCondition(con);

				con = new ConRecord(1, 0);
				ruleRecord.addCondition(con);

				con = new ConRecord(0, 0);
				ruleRecord.addCondition(con);
			} else {
				ConRecord con = new ConRecord(0, 0);
				ruleRecord.addCondition(con);
			}

			policytable.addRule(ruleRecord);
		}

		return policytable;
	}
	
	
	public PolicyTable buildMCDC_Table_NoId(Policy policy, MCDC_converter2 converter) {
		PolicyTable policytable = new PolicyTable();
		List<Rule> rules = getRuleFromPolicy(policy);
		// Target target = (Target) policy.getTarget();

		if ((Target) policy.getTarget() != null) {
			//policytable.addTarget(new TarRecord(2, 0)); // add ind manually

			ArrayList<String> order = new ArrayList<String>();
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector));
			String mcdc_input = converter.convert(sb.toString());

			String[] s = mcdc_input.split(" ");
			for (int p = 0; p < s.length; p++) {
				if (!s[p].endsWith("(") && !s[p].endsWith(")")
						&& !s[p].endsWith("&&"))
					order.add(s[p]);
			}

			if (!mcdc_input.trim().equals("")) {
				MCDCConditionSet mcdcset = new MCDCConditionSet(mcdc_input);
				ArrayList<String> positives = mcdcset.getPositiveConditions();
				ArrayList<String> negatives = mcdcset.getNegativeConditions();
				for (String result : negatives) {
					System.out.println("result :" + result);
					policytable.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 1, 0));
				}
				for (String result : positives) {
					System.out.println("result :" + result);
					policytable.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 0, 0));
				}
			}
			policytable.addTarget(new TarRecord(0, 0));
			// add an dumb target to policy target
		}

		for (Rule rule : rules) {
			if (isDefaultRule(rule)) {
				continue;
			}
			RuleRecord ruleRecord = new RuleRecord();
			// first target
			if (rule.getTarget() != null) {
				//ruleRecord.addTarget(new TarRecord(2, 0)); // add ind

				ArrayList<String> order = new ArrayList<String>();

				Target rTarget = (Target) rule.getTarget();
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				sb.append(True_Target(rTarget, collector));
				String mcdc_input = converter.convert(sb.toString());
				System.out.println("mcdc input here: " + mcdc_input);
				String[] s = mcdc_input.split(" ");
				for (int p = 0; p < s.length; p++) {
					if (!s[p].endsWith("(") && !s[p].endsWith(")")
							&& !s[p].endsWith("&&"))
						order.add(s[p]);
				}
				System.out.println(printArray(order, 1));
				MCDCConditionSet mcdcset = new MCDCConditionSet(mcdc_input);
				ArrayList<String> positives = mcdcset.getPositiveConditions();
				ArrayList<String> negatives = mcdcset.getNegativeConditions();
				for (String result : negatives) {
					System.out.println("result :" + result);

					ruleRecord.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 1, 0));
				}
				for (String result : positives) {
					System.out.println("result :" + result);
					ruleRecord.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 0, 0));
				}

			} else {
				ruleRecord.addTarget(new TarRecord(0, 0));
				// need to go on
			}

			if (rule.getCondition() != null) {
				ConRecord con ;
				//ruleRecord.addCondition(con);

				con = new ConRecord(1, 0);
				ruleRecord.addCondition(con);

				con = new ConRecord(0, 0);
				ruleRecord.addCondition(con);
			} else {
				ConRecord con = new ConRecord(0, 0);
				ruleRecord.addCondition(con);
			}

			policytable.addRule(ruleRecord);
		}

		return policytable;
	}
	

	public ArrayList<PolicySpreadSheetTestRecord> generate_MCDCCoverage(
			TestPanel testPanel, PolicyTable policytable, String foldName, MCDC_converter2 converter) {
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		int count = 1;
		//MCDC_converter2 converter = new MCDC_converter2();
		//PolicyTable policytable = buildMCDC_Table(policy, converter);
		// System.out.println(policytable.getRules().size() + "size here");
		function f = new function();

		List<Rule> rules = getRuleFromPolicy(policy);
		ArrayList<TarRecord> trecord = policytable.getTarget();

		File file = new File(
				testPanel.getTestOutputDestination(foldName));
		if (!file.isDirectory() && !file.exists()) {
			file.mkdir();
		} else {
			f.deleteFile(file);
		}
		int ruleNo = 1;
		long startTime = System.currentTimeMillis();
		for (TarRecord ptarget : trecord) {
			if (ptarget.getEffect() == 2 && ptarget.covered == 0) {
				// policy target ind
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				StringBuffer sb = new StringBuffer();
				sb.append(TruePolicyTarget(policy, collector));
				if (collector.size() > 0) {
					collector.remove(0);
					collector.add(invalidAttr());
				} else {
					continue;
				}
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat) {
					try {
						z3.getValue(collector, nameMap);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					String request = f.print(collector);
					try {
						String path = testPanel
								.getTestOutputDestination(foldName)
								+ File.separator + "request" + count + ".txt";
						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					count++;
					ptarget.setCovered(1);
				}
			} else if (ptarget.getEffect() == 1 && ptarget.covered == 0) {
				// false policy target
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				StringBuffer z3_input = new StringBuffer();
				TruePolicyTarget(policy, collector);
				System.out.println(ptarget.getArray().size() + "size");
				z3_input.append(ptarget.buildZ3(converter) + "\n");
				System.out.println("request : " + z3_input.toString());

				boolean sat = z3str(z3_input.toString(), nameMap, typeMap);
				if (sat) {
					try {
						z3.getValue(collector, nameMap);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					String request = f.print(collector);
					try {
						String path = testPanel
								.getTestOutputDestination(foldName)
								+ File.separator + "request" + count + ".txt";
						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					count++;
					ptarget.setCovered(1);
				}
			} else if (ptarget.getEffect() == 0 && ptarget.covered == 0) {
				// is permit policy target
				for (int i = rules.size() - 1; i >= 0; i--) {
					if (isDefaultRule(rules.get(i))) {
						boolean success = generateDefaultRule(generator,
								testPanel, i, rules, count, foldName);
						if(success)
							count++;
						continue;
					}
					
					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					StringBuffer prefix = new StringBuffer();
					for (int k = 0; k < i; k++) {
						prefix.append(FalseTarget_FalseCondition(rules.get(k),
								collector) + "\n");
					}
					RuleRecord ruleRecord = policytable.getRules().get(i);
					for (TarRecord rtarget : ruleRecord.getTarget()) {
						if (rtarget.getEffect() == 2 && rtarget.covered == 0) { // ind
							boolean getOne = generateIndTarget(generator,
									testPanel, policytable, rules.get(i),
									collector, count, i, prefix,
									foldName, rtarget, false);
							if (getOne)
								count++;
						}

						if (rtarget.getEffect() == 1 && rtarget.covered == 0) {
							TruePolicyTarget(policy, collector);
							True_Target((Target) rules.get(i).getTarget(),
									collector);
							// printTrack2("should wrong once");
							StringBuffer z3_input = new StringBuffer();
							z3_input.append(ptarget.buildZ3(converter) + "\n");
							z3_input.append(rtarget.buildZ3(converter) + "\n");
							// z3_input.append(prefix + "\n");
							z3_input.append(TruePolicyTarget(policy, collector) + "\n");
							z3_input.append(prefix);
							boolean sat = z3str(z3_input.toString(), nameMap,
									typeMap);
							if (sat) {
								try {
									z3.getValue(collector, nameMap);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								String request = f.print(collector);
								updateMCDCTable(policy, policytable, request, i);
								try {
									String path = testPanel
											.getTestOutputDestination(foldName)
											+ File.separator
											+ "request"
											+ count + ".txt";
									FileWriter fw = new FileWriter(path);
									BufferedWriter bw = new BufferedWriter(fw);
									bw.write(request);
									bw.close();

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
										PolicySpreadSheetTestSuite.TEST_KEYWORD
												+ " " + count, "request"
												+ count + ".txt", request, "");
								generator.add(psstr);
								count++;
								rtarget.setCovered(1);
							}

						} else if (rtarget.getEffect() == 0
								&& rtarget.covered == 0) {
							// rule target permit

							for (ConRecord rcondition : ruleRecord
									.getCondition()) {
								if (rcondition.getEffect() == 2
										&& rcondition.getCovered() == 0) {
									boolean getOne = generateIndCondition(
											generator, testPanel, policytable,
											rules.get(i), collector, count, i,
											prefix, foldName,
											rcondition, false);
									if (getOne)
										count++;
								} else if (rcondition.getEffect() == 1
										&& rcondition.getCovered() == 0) {
									// ArrayList<MyAttr> collector = new
									// ArrayList<MyAttr>();

									True_Target((Target) rules.get(i)
											.getTarget(), collector);

									StringBuffer z3_input = new StringBuffer();
									z3_input.append(ptarget.buildZ3(converter)
											+ "\n");
									z3_input.append(rtarget.buildZ3(converter)
											+ "\n");

									z3_input.append(False_Condition(rules
											.get(i).getCondition(), collector)
											+ "\n");
									// z3_input.append(prefix + "\n");
									z3_input.append(TruePolicyTarget(policy,
											collector) + "\n");
									z3_input.append(prefix);
									boolean sat = z3str(z3_input.toString(),
											nameMap, typeMap);
									if (sat) {
										try {
											z3.getValue(collector, nameMap);
										} catch (IOException e) {
											// TODO Auto-generated catch
											// block
											e.printStackTrace();
										}

										String request = f.print(collector);
										if (!policy
												.getCombiningAlg()
												.getIdentifier()
												.equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"))
											updateMCDCTable(policy,
													policytable, request, i);
										try {
											String path = testPanel
													.getTestOutputDestination(foldName)
													+ File.separator
													+ "request"
													+ count
													+ ".txt";
											FileWriter fw = new FileWriter(path);
											BufferedWriter bw = new BufferedWriter(
													fw);
											bw.write(request);
											bw.close();

										} catch (IOException e) {
											// TODO Auto-generated catch
											// block
											e.printStackTrace();
										}
										PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
												PolicySpreadSheetTestSuite.TEST_KEYWORD
														+ " " + count,
												"request" + count + ".txt",
												request, "");
										generator.add(psstr);
										count++;
										// rcondition.setCovered(1);
									}
								} else if (rcondition.getEffect() == 0
										&& rcondition.getCovered() == 0) {
									// ArrayList<MyAttr> collector = new
									// ArrayList<MyAttr>();
									TruePolicyTarget(policy, collector);
									True_Target((Target) rules.get(i)
											.getTarget(), collector);

									StringBuffer z3_input = new StringBuffer();
									z3_input.append(ptarget.buildZ3(converter)
											+ "\n");
									z3_input.append(rtarget.buildZ3(converter)
											+ "\n");
									// z3_input.append(prefix + "\n");
									z3_input.append(True_Condition(rules.get(i)
											.getCondition(), collector));
									z3_input.append(TruePolicyTarget(policy,
											collector) + "\n");
									z3_input.append(prefix);
									System.out.println("request : "
											+ z3_input.toString());
									boolean sat = z3str(z3_input.toString(),
											nameMap, typeMap);
									System.out.println(sat + ":   "
											+ z3_input.toString());
									if (sat) {
										try {
											z3.getValue(collector, nameMap);
										} catch (IOException e) {
											// TODO Auto-generated catch
											// block
											e.printStackTrace();
										}

										String request = f.print(collector);
										updateMCDCTable(policy, policytable,
												request, i);
										try {
											String path = testPanel
													.getTestOutputDestination(foldName)
													+ File.separator
													+ "request"
													+ count
													+ ".txt";
											FileWriter fw = new FileWriter(path);
											BufferedWriter bw = new BufferedWriter(
													fw);
											bw.write(request);
											bw.close();

										} catch (IOException e) {
											// TODO Auto-generated catch
											// block
											e.printStackTrace();
										}
										PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
												PolicySpreadSheetTestSuite.TEST_KEYWORD
														+ " " + count,
												"request" + count + ".txt",
												request, "");
										generator.add(psstr);
										count++;
										// rcondition.setCovered(1);
									}
								}
							}
							rtarget.setCovered(1);
						}
					}
					ruleNo++;
				}
				ptarget.setCovered(1);
			}

		}

		long endTime = System.currentTimeMillis();
		System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
		//checkMcdcTable(policytable);
		return generator;
	}

	private void checkMcdcTable(PolicyTable policytable) {
		System.out.println("Number of policy target : "
				+ policytable.getTarget().size());
		for (TarRecord trecord : policytable.getTarget()) {
			if (trecord.covered == 0 && trecord.getArray() != null) {
				System.out.println("Policy target not covered : ");
			}
		}
		int i = 1;
		for (RuleRecord rrecord : policytable.getRules()) {
			System.out.println("  " + i + " rule : ");
			for (TarRecord trecord : rrecord.getTarget()) {
				if (trecord.getArray() != null && trecord.covered == 0) {
					System.out.println("Rule Target not covered : "
							+ printArray(trecord.getArray()));
				}
				if (trecord.getArray() != null && trecord.covered == 1) {
					System.out.println("Rule Target  covered : "
							+ printArray(trecord.getArray()));
				}
			}
			for (ConRecord crecord : rrecord.getCondition()) {
				if (crecord.covered == 0) {
					System.out.println("Rule Condition not covered : "
							+ crecord.getEffect());
				}
			}
			i++;
		}
	}

	private String printArray(ArrayList<Integer> list) {
		StringBuffer sb = new StringBuffer();
		for (Integer i : list) {
			sb.append(i + ",");
		}
		sb.append("\n");
		return sb.toString();
	}

	private String printArray(ArrayList<String> list, int a) {
		StringBuffer sb = new StringBuffer();
		for (String i : list) {
			sb.append(i + ",");
		}
		sb.append("\n");
		return sb.toString();
	}

	// put the mcdc output into 0, 1 array in target

	private ArrayList<Integer> ConvertToArray(String input,
			ArrayList<String> order) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		String[] token = input.split("&&");
		// System.out.println("token input: " + input);
		// System.out.println(order.get(0));
		for (String s : order) {
			System.out.println("Start " + s);
			for (int i = 0; i < token.length; i++) {
				System.out.println(s + "-->" + token[i].trim());
				if (token[i].trim().startsWith("!"))
					if (token[i].trim().substring(1).equals(s)) {
						result.add(1);
						System.out.println("add 1");
					}
				if (token[i].trim().equals(s)) {
					result.add(0);
					System.out.println("add 0 ");
				}
			}
			System.out.println("End");
		}
		System.out.println(printArray(result));
		return result;

	}

	private ArrayList<String> getTokens(String inputs) {
		ArrayList<String> result = new ArrayList<String>();
		String[] token = inputs.split("&&");
		for (String s : token) {
			// System.out.println("token : " + s);
			s = s.trim();
			if (s.startsWith("!")) {
				s = s.substring(1);
			}
			result.add(s);
		}
		return result;
	}

	// print to track.txt to track z3_str
	static void printTrack2(String request) {
		try {
			FileWriter fw = new FileWriter("./track.txt", true);
			fw.write(request);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<String> getOrder(ArrayList<String> order) {
		ArrayList<String> result = new ArrayList<String>();
		for (String s : order) {
			result.add(s);
		}
		return result;
	}

	private MyAttr getDifferentAttribute(ArrayList<MyAttr> globle,
			ArrayList<MyAttr> local) {
		out: for (MyAttr l : local) {
			for (MyAttr g : globle) {
				if (g.getName().equals(l.getName())) {
					continue out;
				}
			}
			return l;
		}
		return null;
	}

	private boolean generateIndTarget(
			ArrayList<PolicySpreadSheetTestRecord> generator,
			TestPanel testPanel, PolicyTable policytable, Rule targetRule,
			ArrayList<MyAttr> collector, int testNo, int ruleNo,
			StringBuffer prefix, String coverageName, TarRecord rtarget,
			boolean isDC) {
		function f = new function();
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> temp = new ArrayList<MyAttr>();
		if (policy
				.getCombiningAlg()
				.getIdentifier()
				.toString()
				.equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")) {

			sb.append(True_Target((Target) targetRule.getTarget(), temp) + "\n");
			MyAttr unique = getDifferentAttribute(collector, temp);
			if (unique == null) {
				return false;
			}
			temp.add(invalidAttr());
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			mergeAttribute(temp, collector);
			temp.remove(unique);
			sb.append(prefix);
		} else {
			sb.append(True_Target((Target) targetRule.getTarget(), temp) + "\n");
			temp.add(invalidAttr());
			sb.append(TruePolicyTarget(policy, collector) + " \n");
			mergeAttribute(temp, collector);
			temp.remove(0);
			sb.append(prefix);
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if (sat) {
			try {
				z3.getValue(temp, nameMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
			rtarget.setCovered(1);
			String request = f.print(temp);
			if (isDC)
				updateDecisionTable(policy, policytable, request, ruleNo);
			else
				updateMCDCTable(policy, policytable, request, ruleNo);
			try {
				String path = testPanel.getTestOutputDestination(coverageName)
						+ File.separator + "request" + testNo + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + testNo,
					"request" + testNo + ".txt", request, "");
			generator.add(psstr);
			return true;
		}
		return false;
	}

	private boolean generateIndCondition(
			ArrayList<PolicySpreadSheetTestRecord> generator,
			TestPanel testPanel, PolicyTable policytable, Rule targetRule,
			ArrayList<MyAttr> collector, int testNo, int ruleNo,
			StringBuffer prefix, String coverageName, ConRecord rcondition,
			boolean isDC) {
		function f = new function();
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> temp = new ArrayList<MyAttr>();
		if (policy
				.getCombiningAlg()
				.getIdentifier()
				.toString()
				.equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")) {
			sb.append(True_Target((Target) targetRule.getTarget(), collector)
					+ "\n");
			sb.append(True_Condition(targetRule.getCondition(), temp) + "\n");
			MyAttr unique = getDifferentAttribute(collector, temp);

			if (unique == null) {
				return false;
			}
			temp.add(invalidAttr());
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			mergeAttribute(temp, collector);
			temp.remove(unique);
			sb.append(prefix);
		} else {

			sb.append(True_Target((Target) targetRule.getTarget(), collector)
					+ "\n");
			sb.append(True_Condition(targetRule.getCondition(), temp) + "\n");
			temp.add(invalidAttr());
			sb.append(TruePolicyTarget(policy, collector) + " \n");
			mergeAttribute(temp, collector);
			temp.remove(0);
			sb.append(prefix);
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);

		if (sat) {
			try {
				z3.getValue(temp, nameMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
			rcondition.setCovered(1);
			String request = f.print(temp);
			if (isDC)
				updateDecisionTable(policy, policytable, request, ruleNo);
			else
				updateMCDCTable(policy, policytable, request, ruleNo);
			try {
				String path = testPanel.getTestOutputDestination(coverageName)
						+ File.separator + "request" + testNo + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + testNo,
					"request" + testNo + ".txt", request, "");
			generator.add(psstr);
			return true;
		}
		return false;
	}

	private boolean generateDefaultRule(
			ArrayList<PolicySpreadSheetTestRecord> generator,
			TestPanel testPanel, int order, List<Rule> rules, int testNo,
			String coverageName) {
		function f = new function();
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		sb.append(TruePolicyTarget(policy, collector));
		for (int i = 0; i < order; i++) {
			sb.append(FalseTarget_FalseCondition(rules.get(i), collector) + "\n");
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if (sat) {
			try {
				z3.getValue(collector, nameMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String request = f.print(collector);
			try {
				String path = testPanel.getTestOutputDestination(coverageName)
						+ File.separator + "request" + testNo + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + testNo,
					"request" + testNo + ".txt", request, "");
			generator.add(psstr);
			return true;
		}
		return false;
	}
	
	//Turner Lehmbecker WIP
	//Generate PTT request for PTT mutations
	public PolicySpreadSheetTestRecord generate_PolicyTargetTrue(TestPanel t)
	{
		PolicySpreadSheetTestRecord ptr = null;
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		function f = new function();
		if(!policy.isTargetEmpty())
		{
			Target policyTarget = (Target)policy.getTarget();
			List<AnyOfSelection> anyOf = policyTarget.getAnyOfSelections();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			if(anyOf.size() != 0)
			{
				sb.append(False_Target(policyTarget, collector) + "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if(sat)
				{
					System.out.println(nameMap.size() + " map size");
					try
					{	
						z3.getValue(collector, nameMap);
						
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					System.out.println(collector.size() + " collector size");
					String request = f.print(collector);
					try
					{
						String path = t.getTestOutputDestination("_MutationTests") 
								+ File.separator + "requestPTT.txt";
						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					ptr = new PolicySpreadSheetTestRecord(PolicySpreadSheetTestSuite.TEST_KEYWORD
							+ " PTT", "requestPTT.txt", request, "");
					generator.add(ptr);
				}
			}
		}
		return ptr;
	}
	
		//Turner Lehmbecker WIP
		//Generate PTF request for PTF mutations
		public PolicySpreadSheetTestRecord generate_PolicyTargetFalse(TestPanel t)
		{
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
			function f = new function();
			if(!policy.isTargetEmpty())
			{
				Target policyTarget = (Target)policy.getTarget();
				List<AnyOfSelection> anyOf = policyTarget.getAnyOfSelections();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				StringBuffer sb = new StringBuffer();
				if(anyOf.size() != 0)
				{
					sb.append(True_Target(policyTarget, collector) + "\n");
					boolean sat = z3str(sb.toString(), nameMap, typeMap);
					if(sat)
					{
						System.out.println(nameMap.size() + " map size");
						try
						{	
							z3.getValue(collector, nameMap);
							
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						System.out.println(collector.size() + " collector size");
						String request = f.print(collector);
						try
						{
							String path = t.getTestOutputDestination("_MutationTests") 
									+ File.separator + "requestPTF.txt";
							FileWriter fw = new FileWriter(path);
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(request);
							bw.close();
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						ptr = new PolicySpreadSheetTestRecord(PolicySpreadSheetTestSuite.TEST_KEYWORD
								+ " PTF", "requestPTF.txt", request, "");
						generator.add(ptr);
					}
				}
			}
			return ptr;
		}
	
	//Turner Lehmbecker
	//WIP: Generate RTT requests and z3-str input
	public ArrayList<PolicySpreadSheetTestRecord> generate_RuleTargetTrue(TestPanel t)
	{
		//PolicySpreadSheetTestRecord ptr = null;
		//function f = new function();
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		function f = new function();
		int count = 1;
		//Step 0: Find default rule, if it exists
		Rule def = isDefaultRule(rules.get(rules.size() - 1)) ? rules.get(rules.size() - 1) : null;
		
		//CA Condition 1: rule combining algorithm is permit-overrides
		if(cmbAlg instanceof PermitOverridesRuleAlg)
		{
			//Condition 1: Check if default rule exists
			//Result 1: Default rule exists in policy
			if(def != null)
			{
				//Condition 1: Default rule exists and effect is "Permit"
				//Result: Test cannot be generated as policy will always return "Permit"
				
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated for given policy");
					System.err.println("The policy contains a default \"Permit\" rule with permit-overrides"
							+ " combining algorithm");
					return generator;
				}
				//Condition 2: Default rule exists but effect is "Deny"
				//Result: Continue test generation up to rule n - 1
				else
				{
					//Step 2 of Condition 2: Generate tests for each rule and ensure reachability to each rule
					System.out.println("Default rule exists, but tests can be generated");
					if(allPermitRules(rules, rules.size() - 1))
					{
						//Condition 1, Step 2: all rules from 0 -> n-1 (exclusive) are permit
						//Result: Only 1 request needs to be generated
						System.out.println("!!!All permit rules!!");
						build_OnlyOne_Request_false(rules, generator, t, 0, count, "RTT");
					}
					else if(allDenyRules(rules, rules.size() - 1))
						build_OnlyOne_Request_false(rules, generator, t, 1, count, "RTT");
					else
					{
						//Condition 2, Step 2: Mixture of rules for policy
						//Result: Normal test generation
						
						//Step 1 of Condition 2: Generate requests for rules 0 -> n-1
						buildRTTRequests_override(rules, generator, t, 0);
						
					}
				}
			}
			//Condition 1: Check if default rule exists
			//Result 2: Default rule does not exist
			else
			{
				System.out.println("No default rule exists, generating tests as normal...");
				if(allPermitRules(rules, rules.size()))
					//Condition 1 of Result 2: Policy contains only permit rules
					//Result: Only 1 request needs to be generate
					build_OnlyOne_Request_false(rules, generator, t, 0, count, "RTT");
				else if(allDenyRules(rules, rules.size()))
					build_OnlyOne_Request_false(rules, generator, t, 1, count, "RTT");
				else
					//Condition 2 of Result 2: Policy contains a mixture of rules
					//Result: Generate requests as normal
					buildRTTRequests_override(rules, generator, t, 0);
			}
			return generator;
		}
		//CA condition 2: CA is deny-overrides
		else if(cmbAlg instanceof DenyOverridesRuleAlg)
		{
			//Step 1: Check if default rule exists
			if(def != null)
			{
				//Condition 1 of Step 1: default rule exists
				if(def.getEffect() == 1)
				{
					//Result 1 of Condition 1, Step 1: default rule effect is "Deny"
					//Effect: test cannot be generated
					System.err.println("Test cannot be generated for given policy");
					System.err.print("Policy contains a default \"Deny\" rule with deny-overrides"
							+ " combining algorithm");
					return generator;
				}
				else
				{
					//Result 2 of Condition 1, Step 1: default rule effect is "Permit"
					//Effect: tests can be generated as normal up to rule n-1
					System.out.println("Default rule exists, generate tests up to rule n-1");
					//Step 2 of Condition 1: Generate requests up to n-1
					if(allDenyRules(rules, rules.size() - 1))
					{
						//Condition 1, Step 2: All rules from 0 to n-1 are deny
						//Effect/result: Only one request needs to be generated then
						System.out.println("!!!All Deny!!!");
						build_OnlyOne_Request_false(rules, generator, t, 1, count, "RTT");
					}
					else if(allPermitRules(rules, rules.size() -1))
						build_OnlyOne_Request_false(rules, generator, t, 0, count, "RTT");
					else
					{
						//Condition 2, Step 2: Mixture of rules
						//Result: Generate requests as normal
						
						//Step 1, Condition 2: Generate requests for rules
						buildRTTRequests_override(rules, generator, t, 1);
					}
				}
			}
			else
			{
				//Condition 2 of Step 1: no default rule exists
				System.out.println("No default rule exists, test generation will continue as normal...");
				if(f.allDenyRule(policy))
					//Condition 1 of Step 1, Condition 2: Policy contains only deny rules
					//Result: Only 1 request needs to be generated
					build_OnlyOne_Request_false(rules, generator, t, 1, count, "RTT");
				else if(f.allPermitRule(policy))
					build_OnlyOne_Request_false(rules, generator, t, 0, count, "RTT");
				else
					//Condition 2 of Step 1, Condition 2: Policy contains a mixture of rules
					//Result: Generate requests as normal
					buildRTTRequests_override(rules, generator, t, 1);
			}
		}
		else if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated for given policy");
					System.err.println("Policy contains a default \"Permit\" rule with" +
					" deny-unless-permit combining algorithm");
					return generator;
				}
				else
				{
					if(f.allDenyRule(policy))
					{
						System.err.println("Test cannot be generated for given policy");
						System.err.println("Policy contains nothing but \"Deny\" rules " +
						"with deny-unless-permit combining algorithm");
						System.err.println("Please consider writing a better policy and using a different combining algorithm");
						return generator;
					}
					else
						buildRTTRequests_unless(rules, generator, t, 0);
				}
			}
			else if(f.allDenyRule(policy))
			{
				System.err.println("Test cannot be generated for given policy");
				System.err.println("Policy contains nothing but \"Deny\" rules " +
				"with deny-unless-permit combining algorithm");
				System.err.println("Please consider writing a better policy and using a different combining algorithm");
				return generator;
			}
			else
				buildRTTRequests_unless(rules, generator, t, 0);
			return generator;
		}
		else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					System.err.println("Test cannot be generated for given policy");
					System.err.println("Policy contains a default \"Deny\" rule with" +
					" permit-unless-deny combining algorithm");
					return generator;
				}
				else
				{
					if(f.allPermitRule(policy))
					{
						System.err.println("Test cannot be generated for given policy");
						System.err.println("Policy contains nothing but \"Permit\" rules " +
						"with permit-unless-deny combining algorithm");
						System.err.println("Please consider writing a better policy and using a different combining algorithm");
						return generator;
					}
					else
					{
						buildRTTRequests_unless(rules, generator, t, 1);
					}
				}
			}
			else if(f.allPermitRule(policy))
			{
				System.err.println("Test cannot be generated for given policy");
				System.err.println("Policy contains nothing but \"Permit\" rules " +
				"with permit-unless-deny combining algorithm");
				System.err.println("Please consider writing a better policy and using a different combining algorithm");
				return generator;
			}
			else
				buildRTTRequests_unless(rules, generator, t, 1);
			return generator;
		}
		else if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(def != null)
			{
				//If default exists, find first rule with differing effect
				//Set that rule to target false, condition true
				//so that mutant returns that rule's effect, policy returns N/A
				//Set all other rules with same effect as default to false
				//Those rules with different effect, set to target true, condition false
				build_DefaultRTTRequests_FA(rules, def, generator, t);
			}
			else if(f.allDenyRule(policy) || f.allPermitRule(policy))
			{
				build_allOne_RequestsFalse(rules, generator, t, "RTT");
			}
			else
				buildRTTRequests_FA(rules, generator, t);
			return generator;
		}
		else
			System.err.println("Given policy's combining algorithm not currently supported.");
		return generator;
	}
	
	//Turner Lehmbecker
	//WIP: Generate RTF requests and RTF z3-str input
	public ArrayList<PolicySpreadSheetTestRecord> generate_RuleTargetFalse(TestPanel t, PolicyMutator m)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		Rule def = isDefaultRule(rules.get(rules.size() -1)) ? rules.get(rules.size() -1) : null;
		function f = new function();
		if(cmbAlg instanceof PermitOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated for given policy");
					System.err.println("The policy contains a default \"Permit\" rule with permit-overrides"
							+ " combining algorithm");
					return generator;
				}
				else
				{
					System.out.println("Default rule exists, but tests can be generated");
					if(allDenyRules(rules, rules.size() - 1))
					{
						buildRTFRequests_override(rules, generator, t, 1);
					}
					//else if(allPermitRules(rules, rules.size() -1))
						//build_OnlyOne_Request_true(rules, generator, t, 0, count, "RTF");
					else
					{
						buildRTFRequests_override(rules, generator, t, 0);
					}
				}
			}
			else
			{
				if(allDenyRules(rules,rules.size()))
				{
					buildRTFRequests_override(rules, generator, t, 1);
				}
				//else if(f.allPermitRule(policy))
					//build_OnlyOne_Request_false(rules, generator, t, 0, count, "RTF");
				else
					buildRTFRequests_override(rules, generator, t, 0);
			}
			return generator;
		}
		else if(cmbAlg instanceof DenyOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					System.err.println("Tests cannot be generated for given policy");
					System.err.println("Policy contains a default \"Deny\" rule with deny-overrides "
							+ "combining algorithm.");
					return generator;
				}
				else
				{
					System.out.println("Default rule exists, but tests can be generated");
					if(allPermitRules(rules, rules.size() - 1))
					{
						buildRTFRequests_override(rules, generator, t, 0);
					}
					//else if(allDenyRules(rules, rules.size() - 1))
						//build_OnlyOne_Request_false(rules, generator, t, 1, count, "RTF");
					else
					{
						buildRTFRequests_override(rules, generator, t, 1);
					}
				}
			}
			else
			{
				if(allPermitRules(rules, rules.size()))
					buildRTFRequests_override(rules, generator, t, 0);
				//else if(f.allDenyRule(policy))
					//build_OnlyOne_Request_false(rules, generator, t, 1, count, "RTF");
				else
					buildRTFRequests_override(rules, generator, t, 1);
			}
			return generator;
		}
		else if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated for given policy");
					System.err.println("Policy contains a default \"Permit\" rule with" +
					" deny-unless-permit combining algorithm");
					return generator;
				}
				else
				{
					if(f.allDenyRule(policy))
					{
						System.err.println("Test cannot be generated for given policy");
						System.err.println("Policy contains nothing but \"Deny\" rules " +
						"with deny-unless-permit combining algorithm");
						System.err.println("Please consider writing a better policy and using a different combining algorithm");
						return generator;
					}
					else
						buildRTFRequests_unless(rules, generator, t, 0);
				}
			}
			else
			{
				if(f.allDenyRule(policy))
				{
					System.err.println("Test cannot be generated for given policy");
					System.err.println("Policy contains nothing but \"Deny\" rules " +
					"with deny-unless-permit combining algorithm");
					System.err.println("Please consider writing a better policy and using a different combining algorithm");
					return generator;
				}
				else
					buildRTFRequests_unless(rules, generator, t, 0);
			}
			return generator;
		}
		else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					System.err.println("Test cannot be generated for given policy");
					System.err.println("Policy contains a default \"Deny\" rule with" +
					" permit-unless-deny combining algorithm");
					return generator;
				}
				else
				{
					if(f.allPermitRule(policy))
					{
						System.err.println("Test cannot be generated for given policy");
						System.err.println("Policy contains nothing but \"Permit\" rules " +
						"with permit-unless-deny combining algorithm");
						System.err.println("Please consider writing a better policy and using a different combining algorithm");
						return generator;
					}
					else
						buildRTFRequests_unless(rules, generator, t, 1);
				}
				if(f.allPermitRule(policy))
				{
					System.err.println("Test cannot be generated for given policy");
					System.err.println("Policy contains nothing but \"Permit\" rules " +
					"with permit-unless-deny combining algorithm");
					System.err.println("Please consider writing a better policy and using a different combining algorithm");
					return generator;
				}
				else
					buildRTFRequests_unless(rules, generator,t, 1);
			}
			return generator;
		}
		else if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(def != null)
			{
				build_DefaultRTFRequests_FA(rules, def, generator, t);
			}
			else if(f.allDenyRule(policy) || f.allPermitRule(policy))
				build_allOne_RequestsTrue(rules, generator, t, "RTF");
			else
				buildRTFRequests_FA(rules, generator, t);
			return generator;
		}
		else
			System.err.print("Policy's rule combining algorithm not currently supported");
		return generator;
	}
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_RuleConditionTrue(TestPanel t)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		int count = 1;
		Rule def = isDefaultRule(rules.get(rules.size() - 1)) ? rules.get(rules.size() - 1) : null;
		function f = new function();
		
		if(cmbAlg instanceof PermitOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated for given policy");
					System.err.println("The policy contains a default \"Permit\" rule with permit-overrides"
							+ " combining algorithm");
					return generator;
				}
				else
				{
					System.out.println("Default rule exists, but tests can be generated");
					if(allPermitRules(rules, rules.size() - 1) && atLeastOneCondition(rules))
					{
						System.out.println("All permit rules");
						build_OnlyOne_ConditionRequest_false(rules, generator, t, 0, count, "RCT");
					}
					else if(allDenyRules(rules, rules.size()-1))
						buildRCTRequests_override(rules, generator, t, 1);
					else
					{
						buildRCTRequests_override(rules, generator, t, 0);
					}
				}
			}
			else if(f.allPermitRule(policy) && atLeastOneCondition(rules))
				build_OnlyOne_ConditionRequest_false(rules, generator, t, 0, count, "RCT");
			else if(f.allDenyRule(policy))
				buildRCTRequests_override(rules, generator, t, 1);
			else
				buildRCTRequests_override(rules, generator, t, 0);
			return generator;
		}
		else if(cmbAlg instanceof DenyOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else if(allDenyRules(rules, rules.size() - 1) && atLeastOneCondition(rules))
				{
					System.out.println("All deny rules");
					build_OnlyOne_ConditionRequest_false(rules, generator, t, 1, count, "RCT");
				}
				else if(allPermitRules(rules, rules.size() - 1))
					buildRCTRequests_override(rules, generator, t, 0);
				else
				{
					buildRCTRequests_override(rules, generator, t, 1);
				}
					
			}
			else if(f.allDenyRule(policy) && atLeastOneCondition(rules))
			{
				System.out.println("All deny rules");
				build_OnlyOne_ConditionRequest_false(rules, generator, t, 1, count, "RCT");
			}
			else if(f.allPermitRule(policy))
				buildRCTRequests_override(rules, generator, t, 0);
			else
			{
				buildRCTRequests_override(rules, generator, t, 1);
			}
			return generator;
		}
		else if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildRCTRequests_unless(rules, generator, t, 0);
			}
			else if(f.allPermitRule(policy))
			{
				System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRCTRequests_unless(rules, generator, t, 0);
			return generator;
		}
		else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildRCTRequests_unless(rules, generator, t, 1);
			}
			else if(f.allPermitRule(policy))
			{
				System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRCTRequests_unless(rules, generator, t, 1);
			return generator;
		}
		else if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(def != null)
				build_DefaultRCTRequests_FA(rules, def, generator, t);
			else if(f.allPermitRule(policy) || f.allDenyRule(policy))
				build_AllOne_ConditionRequestsFalse(rules, generator, t, "RCT");
			else
				buildRCTRequests_FA(rules, generator, t);
			return generator;	
		}
		else
			System.err.println("Combining algorithm not currently supported");
		return generator;
	}
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_RuleConditionFalse(TestPanel t)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		Rule def = isDefaultRule(rules.get(rules.size() - 1)) ? rules.get(rules.size() - 1) : null;
		function f = new function();
		if(cmbAlg instanceof PermitOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else
				{
					if(f.allDenyRule(policy))
					{
						buildRCFRequests_override(rules, generator, t, 1);
					}
					else
						buildRCFRequests_override(rules, generator, t, 0);
				}
			}
			else if(f.allDenyRule(policy))
			{
				buildRCFRequests_override(rules, generator, t, 1);
			}
			else
				buildRCFRequests_override(rules, generator, t, 0);
			return generator;
		}
		else if(cmbAlg instanceof DenyOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else if(f.allPermitRule(policy))
				{
					buildRCFRequests_override(rules, generator, t, 0);
				}
				else
					buildRCFRequests_override(rules, generator, t, 1);
			}
			else if(f.allPermitRule(policy))
			{
				buildRCFRequests_override(rules, generator, t, 0);
			}
			else
				buildRCFRequests_override(rules, generator, t, 1);
			return generator;
		}
		else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else if(f.allPermitRule(policy))
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildRCFRequests_unless(rules, generator, t, 1);
			}
			else if(f.allPermitRule(policy))
			{
				System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRCFRequests_unless(rules, generator, t, 1);
			return generator;
		}
		else if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else if(f.allDenyRule(policy))
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildRCFRequests_unless(rules, generator, t, 0);
			}
			else if(f.allDenyRule(policy))
			{
				System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRCFRequests_unless(rules, generator, t, 0);
			return generator;
		}
		else if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(def != null)
				build_DefaultRCFRequests_FA(rules, def, generator, t);
			else if(f.allDenyRule(policy) || f.allPermitRule(policy))
				build_AllOne_ConditionRequestsTrue(rules, generator, t, "RCF");
			else
				buildRCFRequests_FA(rules, generator, t);
			return generator;
		}
		return generator;
	}
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_RemoveOneRule(TestPanel t)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		List<Rule> rules = getRuleFromPolicy(policy);
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		function f = new function();
		boolean allDeny = false;
		boolean allPermit = false;
		boolean defaultRule = isDefaultRule(rules.get(rules.size() - 1));
		if(defaultRule)
		{
			allDeny = allDenyRules(rules, rules.size() - 1);
			if(!allDeny)
				allPermit = allPermitRules(rules, rules.size() - 1);
		}
		else
		{
			allDeny = f.allDenyRule(policy);
			if(!allDeny)
				allPermit = f.allPermitRule(policy);
		}
		int count = 1;
		for(Rule r : rules)
		{
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
			{
				if(allDeny)
				{
					System.err.println("Test cannot be generated");
					System.err.println("Policy contains all deny rules");
					System.err.println("COMBINING ALGORITHM: " + cmbAlg.toString());
					return generator;
				}
				else if(r.getEffect() == 0)
					ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RER");
				else
					continue;
			}
			else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
			{
				if(allPermit)
				{
					System.err.println("Test cannot be generated");
					System.err.println("Policy contains all permit rules");
					System.err.println("COMBINING ALGORITHM: " + cmbAlg.toString());
					return generator;
				}
				else if(r.getEffect() == 1)
					ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RER");
				else
					continue;
			}
			else
			{
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RER");
			}
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
		return generator;
	}
	
	
	
	private void buildRCTRequests_override(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect)
	{
		int count = 1;
		for(Rule r : rules)
		{
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!r.isConditionEmpty())
			{
				ptr = buildConditionRequest_false(rules, r, sb, collector, count, t, rules.size(), "RCT");
			}
			else
				System.err.println("Rule does not contain a condition");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRTTRequests_override(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect)
	{
		int count = 1;
		for(Rule temp : rules)
		{
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			ptr = buildRequest_false(rules, temp, sb, collector, count, t, rules.size(), "RTT");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRTFRequests_override(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect)
	{
		int count = 1;
		for(Rule temp : rules)
		{
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(temp.getEffect() == effect)
				ptr = buildRequest_true(rules, temp, sb, collector, count, t, rules.size(), "RTF");
			else
				ptr = buildRequest_false2(rules, temp, sb, collector, count, t, rules.size(), "RTF");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRCFRequests_override(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect)
	{
		int count = 1;
		for(Rule r : rules)
		{
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(r.getEffect() == effect && !r.isConditionEmpty())
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RCF");
			else if(r.getEffect() != effect && !r.isConditionEmpty())
				ptr = buildConditionRequest_false(rules, r, sb, collector, count, t, rules.size(), "RCF");
			else
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RCF");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private PolicySpreadSheetTestRecord buildRequest_false(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		//Make current rule evaluate to false
		sb.append(False_Target((Target)rule.getTarget(), collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		//Ensure rules before and following current evaluate to false (NotApplicable)
		for(int i = 0; i < stop; i++)
		{
			Rule temp = rules.get(i);
			if(temp.getId().equals(rule.getId()) || isDefaultRule(temp))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			System.out.println(request);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRequest_false2(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		//Make current rule evaluate to false
		if(!rule.isTargetEmpty())
		{
			if(rule.isConditionEmpty())
				sb.append(True_Target((Target)rule.getTarget(), collector) + "\n");
			else
			{
				sb.append(False_Target((Target)rule.getTarget(), collector) + "\n");
				sb.append(True_Condition(rule.getCondition(), collector) + "\n");
			}
		}
		else
			sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		//Ensure rules before and following current evaluate to false (NotApplicable)
		for(int i = 0; i < stop; i++)
		{
			Rule temp = rules.get(i);
			if(temp.getId().equals(rule.getId()) || isDefaultRule(temp))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			System.out.println(request);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_false(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(TrueTarget_FalseCondition(rule, collector) + "\n");
		
		for(int i = 0; i < stop; i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(rule.getId()) || isDefaultRule(r))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_true(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(TrueTarget_TrueCondition(rule, collector) + "\n");
		
		for(int i = 0; i < stop; i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(rule.getId()) || isDefaultRule(r))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRequest_Allfalse(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type, int effect)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		Rule firstDiff = null;
		//For a policy containing a default rule
		//we must ensure that the policy never returns the default rule's effect 
		//and the mutant always returns the default rule's effect
		//To do this, we find the first rule regardless of the position in the policy
		//that has a different effect than the default rule and make sure that it evals to true
		//therefore returning either the opposite of the default rule's effect or ID
		//this method is only called when there is at least 1 rule with a different effect than the default rule
		//otherwise, a different method is called
		int diffIndex = -1;
		int currentIndex = rules.indexOf(current);
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getEffect() != effect)
			{
				firstDiff = r;
				diffIndex = i;
				break;
			}
		}

		//if the index of the differing rule is 0 or -1 (i.e. the first rule or a differing rule could not be found)
		//then there is no possible way to generate a valid test
		if(diffIndex <= 0)
			return ptr;
		
		//if the index of the differing rule is less than that of the current rule
		//we need to backtrack and find the the first rule before the differing that has the same
		//effect as the default rule and set this to false target, true condition
		//while this will inevitably cause the test to fail to detect most mutants
		//it guarantees that at least one mutant will be discovered with this test
		//based on our validation algorithm
		else if(diffIndex < currentIndex)
		{
			for(int i = diffIndex - 1; i >= 0; i--)
			{
				Rule r = rules.get(i);
				if(r.getEffect() != firstDiff.getEffect())
					current = r;
			}
			sb.append(False_Target((Target)current.getTarget(), collector) + "\n");
			sb.append(True_Condition(current.getCondition(), collector) + "\n");
			sb.append(TrueTarget_TrueCondition(firstDiff, collector) + "\n");
		}
		else
		{
			sb.append(False_Target((Target)current.getTarget(), collector) + "\n");
			sb.append(True_Condition(current.getCondition(), collector) + "\n");
			sb.append(TrueTarget_TrueCondition(firstDiff, collector) + "\n");
		}
		//Ensure rules before and following current evaluate to false (NotApplicable)
		for(int i = 0; i < stop; i++)
		{	
			Rule temp = rules.get(i);
			if(isDefaultRule(temp) || temp.getId().equals(current.getId()) || temp.getId().equals(firstDiff.getId()))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			System.out.println(request);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_AllFalse(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type, int effect)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		Rule firstDiff = null;
		int diffIndex = -1;
		int currentIndex = rules.indexOf(current);
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getEffect() != effect)
			{
				if(!r.isConditionEmpty())
				{
					diffIndex = i;
					firstDiff = r;
					break;
				}
			}
		}
		
		if(diffIndex <= 0)
			return ptr;
		else if(diffIndex < currentIndex)
		{
			for(int i = diffIndex - 1; i >= 0; i--)
			{
				Rule r = rules.get(i);
				if(r.getEffect() != firstDiff.getEffect())
					current = r;
			}
			sb.append(False_Target((Target)current.getTarget(), collector) + "\n");
			sb.append(True_Condition(current.getCondition(), collector) + "\n");
			sb.append(TrueTarget_TrueCondition(firstDiff, collector) + "\n");
		}
		else
		{
			sb.append(False_Target((Target)current.getTarget(), collector) + "\n");
			sb.append(True_Condition(current.getCondition(), collector) + "\n");
			sb.append(TrueTarget_TrueCondition(firstDiff, collector) + "\n");
		}
		
		for(int i = 0; i < stop; i++)
		{
			Rule temp = rules.get(i);
			if(isDefaultRule(temp) || temp.getId().equals(current.getId()) || temp.getId().equals(firstDiff.getId()))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRequest_true(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		
		//Ensure that current rule evaluates to true
		sb.append(TrueTarget_TrueCondition(rule, collector) + "\n");
		
		//ensure rules before and following current evaluate to false
		for(int i = 0; i < stop; i++)
		{
			Rule temp = rules.get(i);
			if(temp.getId().equals(rule.getId()) || isDefaultRule(temp))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		
		System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			//System.out.println(request);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	//Turner Lehmbecker
	//Helper method to check if rules from 0 to a specified index are all permit rules 
	//returns true if all rules from 0 to index are permit
	//false if at least one rule from 0 to index is deny
	//NEEDED IN SOME CASES
	private boolean allPermitRules(List<Rule> rules, int index)
	{
		int stop = (index < 0 || index > rules.size()) ? (index < 0 ? 0 : (index > rules.size() ? rules.size() : index)) : index;
		if(stop == 0)
		{
			return rules.get(stop).getEffect() == 0;
		}
		boolean allPermit = true;
		int i = 0;
		while(i < stop && allPermit)
		{
			allPermit = rules.get(i).getEffect() == 0;
			i++;
		}
		return allPermit;
	}
	
	//Turner Lehmbecker
	//Helper method to check if rules from 0 to a specified index are all deny rules 
	//returns true if all rules from 0 to index are deny
	//false if at least one rule from 0 to index is permit
	//NEEDED IN SOME CASES
	private boolean allDenyRules(List<Rule> rules, int index)
	{
		int stop = (index < 0 || index > rules.size()) ? (index < 0 ? 0 : (index > rules.size() ? rules.size() : index)) : index;
		if(stop == 0)
		{
			return rules.get(stop).getEffect() == 1;
		}
		boolean allDeny = true;
		int i = 0;
		while(i < stop && allDeny)
		{
			allDeny = rules.get(i).getEffect() == 1;
			i++;
		}
		return allDeny;
	}
	
	private void build_OnlyOne_Request_false(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, int count, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		function f = new function();
		
		if(!policy.isTargetEmpty())
			sb.append(TruePolicyTarget(policy, collector) + "\n");
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(!isDefaultRule(r))
			{
				if(r.getEffect() == effect)
				{
					if(!r.isTargetEmpty() && !r.isConditionEmpty())
					{
						sb.append(False_Target((Target)r.getTarget(), collector) + "\n");
						sb.append(True_Condition(r.getCondition(), collector) + "\n");
					}
					else if(r.isTargetEmpty() && !r.isConditionEmpty())
						sb.append(False_Condition(r.getCondition(), collector) + "\n");
					else if(!r.isTargetEmpty() && r.isConditionEmpty())
						sb.append(False_Target((Target)r.getTarget(), collector) + "\n");
					else
						continue;
				}
				else
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
			}
		}
		System.out.println("Z3: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
			if(ptr != null)
				generator.add(ptr);
		}
		else
		{
			if(type.compareTo("RTT") == 0)
				buildRTTRequests_override(rules, generator, t, effect);
			else if(type.compareTo("RTF") == 0)
				buildRTFRequests_override(rules, generator, t, effect);
		}
	}
	
	private void build_OnlyOne_ConditionRequest_false(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, int count, String type)
	{
		System.err.println("Generating only one test");
		PolicySpreadSheetTestRecord ptr = null;
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		function f = new function();
		sb.append(TruePolicyTarget(policy, collector) + "\n");
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(!isDefaultRule(r))
			{
				if(r.getEffect() == effect)
				{
					if(!r.isTargetEmpty() && !r.isConditionEmpty())
						sb.append(TrueTarget_FalseCondition(r, collector) + "\n");
					else if(r.isTargetEmpty() && !r.isConditionEmpty())
						sb.append(False_Condition(r.getCondition(), collector) + "\n");
					else
						sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
				}
				else
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
			}
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
		else
		{
			if(type.compareTo("RCT") == 0)
				buildRCTRequests_override(rules, generator, t, effect);
			else if(type.compareTo("RCF") == 0)
				buildRCFRequests_override(rules, generator, t, effect);
		}
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_unlessFalse(List<Rule> rules, Rule firstMatch, StringBuffer sb, TestPanel t, int count, int effect, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		function f = new function();
		sb.append(TruePolicyTarget(policy, collector) + "\n");
		sb.append(TrueTarget_FalseCondition(firstMatch, collector) + "\n");
		int firstIndex = rules.indexOf(firstMatch);
		for(int i = 0; i < firstIndex; i++)
		{
			sb.append(FalseTarget_FalseCondition(rules.get(i), collector) + "\n");
		}
		
		for(int i = firstIndex + 1; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getEffect() == effect)
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRequest_unlessFalse(List<Rule> rules, Rule firstMatch, StringBuffer sb, TestPanel t,  int index, int count, int effect, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		System.out.println("Found a match!");
		function f = new function();
		sb.append(TruePolicyTarget(policy, collector) + "\n");
		
		if(!firstMatch.isTargetEmpty())
			sb.append(False_Target((Target)firstMatch.getTarget(), collector) + "\n");
		if(!firstMatch.isConditionEmpty())
			sb.append(True_Condition(firstMatch.getCondition(), collector) + "\n");
		
		for(int i = 0; i < index; i++)
			sb.append(FalseTarget_FalseCondition(rules.get(i), collector) + "\n");
			
		for(int j = index + 1; j < rules.size(); j++)
		{
			Rule temp = rules.get(j);
			if(temp.getEffect() == effect)
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		System.out.println("Z3-input: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" +type+ count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bmw = new BufferedWriter(fw);
				bmw.write(request);
				bmw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		else
		{
			System.err.println("Z3 input does not meet boolean satisfiability requirements");
			System.err.println("Test could not be generated");
		}
		return ptr;
	}
	
	private void buildRTTRequests_unless(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect)
	{
		int count = 1, index = 0;
		for(Rule temp : rules)
		{
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			if(temp.getEffect() == effect)
				ptr = buildRequest_unlessFalse(rules, temp, sb, t, index, count, effect, "RTT");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
			index++;
		}
	}
	
	private void buildRCTRequests_unless(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect)
	{
		int count = 1;
		for(Rule r : rules)
		{
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			if(r.getEffect() == effect && !r.isConditionEmpty())
				ptr = buildConditionRequest_unlessFalse(rules, r, sb, t, count, effect, "RCT");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRTFRequests_unless(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect)
	{
		int count = 1;
		for(Rule temp : rules)
		{
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(temp.getEffect() == effect)
				ptr = buildRequest_true(rules, temp, sb, collector, count, t, rules.size(), "RTF");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRCFRequests_unless(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect)
	{
		int count = 1;
		for(Rule r : rules)
		{
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if((r.getEffect() == effect && !r.isConditionEmpty()) || !isDefaultRule(r))
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RCF");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRTTRequests_FA(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			for(Rule temp : rules)
			{
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				ptr = buildRequest_falseFA(rules, temp, sb, collector, count, t, "RTT");
				if(ptr != null)
				{
					generator.add(ptr);
					count++;
				}
				
			}
		}
		else
			System.err.println("Must have at least rule to generate a test");
	}
	
	private void buildRCTRequests_FA(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t)
	{
		int count = 1;
		for(Rule r : rules)
		{
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			PolicySpreadSheetTestRecord ptr = null;
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!r.isConditionEmpty())
				ptr = buildConditionRequest_falseFA(rules, r, sb, collector, count, t, "RCT");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRCFRequests_FA(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t)
	{
		int count = 1;
		for(Rule r : rules)
		{
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			PolicySpreadSheetTestRecord ptr = null;
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!r.isConditionEmpty())
				ptr = buildConditionRequest_trueFA(rules, r, sb, collector, count, t, "RCF");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRTFRequests_FA(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			for(Rule temp : rules)
			{
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				ptr = buildRequest_trueFA(rules, temp, sb, collector, count, t, "RTF");
				if(ptr != null)
				{
					generator.add(ptr);
					count++;
				}
				
			}
		}
		else
			System.err.println("Must have at least rule to generate a test");
	}
	
	private void build_DefaultRTTRequests_FA(List<Rule> rules, Rule defaultRule, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			int effect = defaultRule.getEffect();
			function checker = new function();
			if(checker.allDenyRule(policy) || checker.allPermitRule(policy))
			{
				build_allOne_RequestsFalse(rules, generator, t, "RTT");
				checker = null;
			}
			else
			{
				for(Rule r : rules)
				{
					StringBuffer sb = new StringBuffer();
					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					PolicySpreadSheetTestRecord ptr = null;
					sb.append(TruePolicyTarget(policy, collector) + "\n");
					if(r.getEffect() == effect)
						ptr = buildRequest_Allfalse(rules, r, sb, collector, count, t, rules.size(), "RTT", effect);
					else
						ptr = buildRequest_false(rules, r, sb, collector, count, t, rules.size(), "RTT");
					if(ptr != null)
					{
						generator.add(ptr);
						count++;
					}
				}
			}
		}
	}
	
	private void build_DefaultRCTRequests_FA(List<Rule> rules, Rule defaultRule, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t)
	{
		int count = 1;
		int effect = defaultRule.getEffect();
		function checker = new function();
		if(checker.allDenyRule(policy) || checker.allPermitRule(policy))
		{
			build_AllOne_ConditionRequestsFalse(rules, generator, t, "RCT");
			checker = null;
		}
		else
		{
			for(Rule r : rules)
			{
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				if(r.getEffect() == effect && !r.isConditionEmpty())
					ptr = buildConditionRequest_AllFalse(rules, r, sb, collector, count, t, rules.size(), "RCT", effect);
				else if(r.getEffect() != effect && !r.isConditionEmpty())
					ptr = buildConditionRequest_false(rules, r, sb, collector, count, t, rules.size(), "RCT");
				if(ptr != null)
				{
					generator.add(ptr);
					count++;
				}
			}
		}
	}
	
	private void build_DefaultRCFRequests_FA(List<Rule> rules, Rule defaultRule, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t)
	{
		int count = 1;
		int effect = defaultRule.getEffect();
		function checker = new function();
		if(checker.allDenyRule(policy) || checker.allPermitRule(policy))
			build_AllOne_ConditionRequestsTrue(rules, generator, t, "RCF");
		else
		{
			for(Rule r : rules)
			{
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				if(r.getEffect() == effect && !r.isConditionEmpty())
					ptr = buildConditionRequest_true(rules, r, sb, collector, count, t, rules.size(), "RCF");
				else if(r.getEffect() != effect && !r.isConditionEmpty())
					ptr = buildConditionRequest_AllFalse(rules, r, sb, collector, count, t, rules.size(), "RCF", effect);
				if(ptr != null)
				{
					generator.add(ptr);
					count++;
				}
			}
		}
	}
	
	private void build_DefaultRTFRequests_FA(List<Rule> rules, Rule defaultRule, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			int effect = defaultRule.getEffect();
			function checker = new function();
			if(checker.allDenyRule(policy) || checker.allPermitRule(policy))
			{
				build_allOne_RequestsTrue(rules, generator, t, "RTF");
				checker = null;
			}
			else
			{
				for(Rule r : rules)
				{
					StringBuffer sb = new StringBuffer();
					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					PolicySpreadSheetTestRecord ptr = null;
					sb.append(TruePolicyTarget(policy, collector) + "\n");
					if(r.getEffect() == effect)
						ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RTF");
					else
						ptr = buildRequest_Allfalse(rules, r, sb, collector, count, t, rules.size(), "RTF", effect);
					if(ptr != null)
					{
						generator.add(ptr);
						count++;
					}
				}
			}
		}
	}
	
	private void build_allOne_RequestsFalse(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, String type)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			for(Rule r : rules)
			{
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				ptr = buildRequest_false(rules, r, sb, collector, count, t, rules.size(), type);
				if(ptr != null)
				{
					generator.add(ptr);
					count++;
				}
			}
		}
	}
	
	private void build_AllOne_ConditionRequestsFalse(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, String type)
	{
		int count = 1;
		for(Rule r : rules)
		{
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			PolicySpreadSheetTestRecord ptr = null;
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!r.isConditionEmpty())
				ptr = buildConditionRequest_false(rules, r, sb, collector, count, t, rules.size(), type);
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void build_AllOne_ConditionRequestsTrue(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, String type)
	{
		int count = 1;
		for(Rule r : rules)
		{
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			PolicySpreadSheetTestRecord ptr = null;
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!r.isConditionEmpty() || !isDefaultRule(r))
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), type);
			else
				continue;
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void build_allOne_RequestsTrue(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, String type)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			for(Rule r : rules)
			{
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), type);
				if(ptr != null)
				{
					generator.add(ptr);
					count++;
				}
			}
		}
	}
	
	private PolicySpreadSheetTestRecord buildRequest_falseFA(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(False_Target((Target)current.getTarget(), collector) + "\n");
		sb.append(True_Condition(current.getCondition(), collector) + "\n");
		int currentIndex = rules.indexOf(current);
		int firstDifferent = -1;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(current.getId()) || isDefaultRule(r))
				continue;
			else if(i > currentIndex)
			{
				if(r.getEffect() == current.getEffect())
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
				else if(firstDifferent < 0 && r.getEffect() != current.getEffect())
					firstDifferent = i;
				else
					continue;
			}
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		if(firstDifferent > 0)
		{
			sb.append(TrueTarget_TrueCondition(rules.get(firstDifferent), collector) + "\n");
		}
		System.out.println("Z3-input: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_falseFA(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(TrueTarget_FalseCondition(current, collector) + "\n");
		int currentIndex = rules.indexOf(current);
		int diffIndex = -1;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(current.getId()) || isDefaultRule(r))
				continue;
			else if(i > currentIndex)
			{
				if(r.getEffect() == current.getEffect())
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
				else if(diffIndex < 0 && r.getEffect() != current.getEffect() && !r.isConditionEmpty())
					diffIndex = i;
				else
					continue;
			}
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		if(diffIndex > 0)
			sb.append(TrueTarget_TrueCondition(rules.get(diffIndex), collector) + "\n");
		System.out.println("Z3: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_trueFA(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(TrueTarget_TrueCondition(current, collector) + "\n");
		int currentIndex = rules.indexOf(current);
		int diffIndex = -1;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(current.getId()) || isDefaultRule(r))
				continue;
			else if(i > currentIndex)
			{
				if(r.getEffect() == current.getEffect())
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
				else if(diffIndex < 0 && r.getEffect() != current.getEffect() && !r.isConditionEmpty())
					diffIndex = i;
				else
					continue;
			}
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		if(diffIndex > 0)
			sb.append(TrueTarget_TrueCondition(rules.get(diffIndex), collector) + "\n");
		System.out.println("Z3: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRequest_trueFA(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(TrueTarget_TrueCondition(current, collector) + "\n");
		int currentIndex = rules.indexOf(current);
		int firstDifferent = -1;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(current.getId()) || isDefaultRule(r))
				continue;
			else if(i > currentIndex)
			{
				if(r.getEffect() == current.getEffect())
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
				else if(firstDifferent < 0 && r.getEffect() != current.getEffect())
					firstDifferent = i;
				else
					continue;
			}
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		if(firstDifferent > 0)
		{
			sb.append(TrueTarget_TrueCondition(rules.get(firstDifferent), collector) + "\n");
		}
		System.out.println("Z3-input: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private boolean atLeastOneCondition(List<Rule> rules)
	{
		for(Rule r : rules)
		{
			if(r.isConditionEmpty())
				return false;
		}
		return true;
	}
}
