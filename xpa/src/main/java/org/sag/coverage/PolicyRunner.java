package org.sag.coverage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.Balana;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.ctx.xacml3.XACML3EvaluationCtx;

/**
 * Created by shuaipeng on 9/8/16.
 */
public class PolicyRunner {
    private static Log logger = LogFactory.getLog(PolicyRunner.class);

    static boolean runTest(AbstractPolicy policy, String request, String oracleString) {
        int oracle = balanaFinalDecision(oracleString);
        int response = evaluate(policy, request);
        return response == oracle;
    }

    private static int evaluate(AbstractPolicy policy, String request) {
        logger.debug("policy: " + policy);
        logger.debug("request: " + request);
        RequestCtxFactory rc = new RequestCtxFactory();
        try {
            AbstractRequestCtx ar = rc.getRequestCtx(request);
            XACML3EvaluationCtx ec = new XACML3EvaluationCtx(new RequestCtx(ar.getAttributesSet(),
                    ar.getDocumentRoot()), Balana.getInstance().getPdpConfig());
            return policy.evaluate(ec).getDecision();
        } catch (ParsingException e) {
            logger.error(e);
            return Integer.MAX_VALUE;
        }
    }

    private static int balanaFinalDecision(String decisionString) {
        if (decisionString.equalsIgnoreCase("Permit"))
            return AbstractResult.DECISION_PERMIT;
        else if (decisionString.equalsIgnoreCase("Deny"))
            return AbstractResult.DECISION_DENY;
        else if (decisionString.equalsIgnoreCase("NA") || decisionString.equalsIgnoreCase("NotApplicable")) // new pattern 11/13/14
            return AbstractResult.DECISION_NOT_APPLICABLE;
        else if (decisionString.equalsIgnoreCase("INDETERMINATE"))
            return AbstractResult.DECISION_INDETERMINATE;
        else if (decisionString.equalsIgnoreCase("IP"))
            return AbstractResult.DECISION_INDETERMINATE_PERMIT;
        else if (decisionString.equalsIgnoreCase("ID"))
            return AbstractResult.DECISION_INDETERMINATE_DENY;
        else if (decisionString.equalsIgnoreCase("IDP"))
            return AbstractResult.DECISION_INDETERMINATE_DENY_OR_PERMIT;
        return AbstractResult.DECISION_INDETERMINATE;
    }

}
