package org.sag.coverage;

import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.AbstractTarget;
import org.wso2.balana.MatchResult;
import org.wso2.balana.Rule;
import org.wso2.balana.XACMLConstants;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.ctx.ResultFactory;
import org.wso2.balana.ctx.xacml2.Result;

public aspect PolicyTracer {

    pointcut ruleEvaluationPointcut(Rule rule, EvaluationCtx context): call(AbstractResult Rule.evaluate(*)) && target(rule) && args(context);

    // replace the evaluate method in the Rule class to record the result of each rule evaluation
    AbstractResult around(Rule rule, EvaluationCtx context): ruleEvaluationPointcut(rule, context) {
        // If the Target is null then it's supposed to inherit from the
        // parent policy, so we skip the matching step assuming we wouldn't
        // be here unless the parent matched
        MatchResult match = null;

// start of changes
// xacmlVersion, processObligations, processAdvices have been changed to public
AbstractTarget target = rule.getTarget();
int effectAttr=rule.getEffect();
Condition condition = rule.getCondition();
// end of changes

        if (target != null) {

            match = target.match(context);
            int result = match.getResult();

            // if the target didn't match, then this Rule doesn't apply
            if (result == MatchResult.NO_MATCH){
// start of change
            	RuleCoverage ruleCoverage = new RuleCoverage(rule.getId().toString(), RuleCoverage.TargetConditionIndividualCoverage.FALSE, RuleCoverage.TargetConditionIndividualCoverage.NOTEVALUATED, Result.DECISION_NOT_APPLICABLE);
            	PolicyCoverageFactory.currentPolicyCoverage.addRuleCoverage(ruleCoverage);

// end of change            	
                return ResultFactory.getFactory().getResult(Result.DECISION_NOT_APPLICABLE, context);
            }

            // if the target was indeterminate, we can't go on
            if (result == MatchResult.INDETERMINATE){

                // defines extended indeterminate results with XACML 3.0
            	
                if(rule.xacmlVersion == XACMLConstants.XACML_VERSION_3_0){
                    if(effectAttr == AbstractResult.DECISION_PERMIT){
// start of change
                    	RuleCoverage ruleCoverage = new RuleCoverage(rule.getId().toString(), RuleCoverage.TargetConditionIndividualCoverage.ERROR, RuleCoverage.TargetConditionIndividualCoverage.NOTEVALUATED, Result.DECISION_INDETERMINATE_PERMIT);
                    	PolicyCoverageFactory.currentPolicyCoverage.addRuleCoverage(ruleCoverage);
// end of change            	
                    	
                        return ResultFactory.getFactory().getResult(Result.DECISION_INDETERMINATE_PERMIT,
                                match.getStatus(), context);
                    } else {
// start of change
                    	RuleCoverage ruleCoverage = new RuleCoverage(rule.getId().toString(), RuleCoverage.TargetConditionIndividualCoverage.ERROR, RuleCoverage.TargetConditionIndividualCoverage.NOTEVALUATED, Result.DECISION_INDETERMINATE_DENY);
                    	PolicyCoverageFactory.currentPolicyCoverage.addRuleCoverage(ruleCoverage);
// end of change            	
                        return ResultFactory.getFactory().getResult(Result.DECISION_INDETERMINATE_DENY,
                                match.getStatus(), context);
                    }
                }
// start of change
            	RuleCoverage ruleCoverage = new RuleCoverage(rule.getId().toString(), RuleCoverage.TargetConditionIndividualCoverage.ERROR, RuleCoverage.TargetConditionIndividualCoverage.NOTEVALUATED, Result.DECISION_INDETERMINATE);
            	PolicyCoverageFactory.currentPolicyCoverage.addRuleCoverage(ruleCoverage);
// end of change            	
                return ResultFactory.getFactory().getResult(Result.DECISION_INDETERMINATE,
                        match.getStatus(), context);
            }
        }

        // if there's no condition, then we just return the effect
        if (condition == null){
// start of change
        	RuleCoverage ruleCoverage = new RuleCoverage(rule.getId().toString(), RuleCoverage.TargetConditionIndividualCoverage.TRUE, RuleCoverage.TargetConditionIndividualCoverage.EMPTY, effectAttr);
        	PolicyCoverageFactory.currentPolicyCoverage.addRuleCoverage(ruleCoverage);
// end of change            	
            // if any obligations or advices are defined, evaluates them and return
            return  ResultFactory.getFactory().getResult(effectAttr, rule.processObligations(context),
                                                        rule.processAdvices(context), context);
        }

        // otherwise we evaluate the condition
        EvaluationResult result = condition.evaluate(context);
        //System.out.println(result.toString());


        if (result.indeterminate()) {

            // defines extended indeterminate results with XACML 3.0
            if(rule.xacmlVersion == XACMLConstants.XACML_VERSION_3_0){
                if(effectAttr == AbstractResult.DECISION_PERMIT){
//start of change
                	RuleCoverage ruleCoverage = new RuleCoverage(rule.getId().toString(), RuleCoverage.TargetConditionIndividualCoverage.TRUE, RuleCoverage.TargetConditionIndividualCoverage.ERROR, Result.DECISION_INDETERMINATE_PERMIT);
                	PolicyCoverageFactory.currentPolicyCoverage.addRuleCoverage(ruleCoverage);
//end of change            	
                    return ResultFactory.getFactory().getResult(Result.DECISION_INDETERMINATE_PERMIT,
                            result.getStatus(), context);
                } else {
//start of change
                	RuleCoverage ruleCoverage = new RuleCoverage(rule.getId().toString(), RuleCoverage.TargetConditionIndividualCoverage.TRUE, RuleCoverage.TargetConditionIndividualCoverage.ERROR, Result.DECISION_INDETERMINATE_DENY);
                	PolicyCoverageFactory.currentPolicyCoverage.addRuleCoverage(ruleCoverage);
//end of change            	
                    return ResultFactory.getFactory().getResult(Result.DECISION_INDETERMINATE_DENY,
                           result.getStatus(), context);
                }
            }
//start of change
        	RuleCoverage ruleCoverage = new RuleCoverage(rule.getId().toString(), RuleCoverage.TargetConditionIndividualCoverage.TRUE, RuleCoverage.TargetConditionIndividualCoverage.ERROR, Result.DECISION_INDETERMINATE);
        	PolicyCoverageFactory.currentPolicyCoverage.addRuleCoverage(ruleCoverage);
//end of change            	

            // if it was INDETERMINATE, then that's what we return
            return ResultFactory.getFactory().getResult(Result.DECISION_INDETERMINATE,
                                                                       result.getStatus(), context);
        } else {
            // otherwise we return the effect on true, and NA on false
            BooleanAttribute bool = (BooleanAttribute) (result.getAttributeValue());
            if (bool.getValue()) {
//start of change
            	RuleCoverage ruleCoverage = new RuleCoverage(rule.getId().toString(), RuleCoverage.TargetConditionIndividualCoverage.TRUE, RuleCoverage.TargetConditionIndividualCoverage.TRUE, effectAttr);
            	PolicyCoverageFactory.currentPolicyCoverage.addRuleCoverage(ruleCoverage);
//end of change            	
                // if any obligations or advices are defined, evaluates them and return
                return  ResultFactory.getFactory().getResult(effectAttr, rule.processObligations(context),
                                                            rule.processAdvices(context), context);
            } else {
//start of change
            	RuleCoverage ruleCoverage = new RuleCoverage(rule.getId().toString(), RuleCoverage.TargetConditionIndividualCoverage.TRUE, RuleCoverage.TargetConditionIndividualCoverage.FALSE, Result.DECISION_NOT_APPLICABLE);
            	PolicyCoverageFactory.currentPolicyCoverage.addRuleCoverage(ruleCoverage);
//end of change            	
                return ResultFactory.getFactory().getResult(Result.DECISION_NOT_APPLICABLE, context);
            }
        }

    }

    // record the entry of policy evaluation
    before(AbstractPolicy policy, EvaluationCtx context): target(policy) && call(* AbstractPolicy.evaluate(*)) && args(context) {
//        System.out.print("\nPolicy ID: "+p.getId()+"\n");
    	AbstractTarget policyTarget = policy.getTarget();
    	int result = 0; // assume that there is no policy target (considered a match)
        if (policyTarget != null) {
        	MatchResult matchResult = policyTarget.match(context);
            result = matchResult.getResult();
        }
		PolicyCoverageFactory.startNewPolicyCoverage(policy.getId().toString(), policy.getChildren().size(), result);
    }

    // record the result of policy evaluation
    after() returning(AbstractResult result): call(* AbstractPolicy.evaluate(*))  {
    	PolicyCoverageFactory.currentPolicyCoverage.setDecision(result.getDecision());
    	PolicyCoverageFactory.flush();// call flush() after, so that the coverage of the last test won't be lost
	}

}
