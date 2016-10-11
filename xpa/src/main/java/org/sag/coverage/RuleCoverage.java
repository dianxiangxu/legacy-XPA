package org.sag.coverage;

/**
 * Created by shuaipeng on 9/14/16.
 */
public class RuleCoverage extends Coverage {
    private CombinedCoverage combined;
    private RuleDecisionCoverage ruleDecisionCoverage;

    public RuleCoverage(IntermediateCoverage targetResult,
                        IntermediateCoverage conditionResult, int ruleResult) {
        // get combine coverage
        if ((targetResult == IntermediateCoverage.TRUE || targetResult == IntermediateCoverage.EMPTY)
                && (conditionResult == IntermediateCoverage.TRUE || conditionResult == IntermediateCoverage.EMPTY))
            combined = CombinedCoverage.BOTHTRUE;
        else if (targetResult == IntermediateCoverage.FALSE)
            combined = CombinedCoverage.FALSETARGET;
        else if (conditionResult == IntermediateCoverage.FALSE)
            combined = CombinedCoverage.FALSECONDITION;
        else if (targetResult == IntermediateCoverage.ERROR)
            combined = CombinedCoverage.ERRORTARGET;
        else
            combined = CombinedCoverage.ERRORCONDITION;
        // get rule decision coverage
        if (combined == CombinedCoverage.BOTHTRUE)
            ruleDecisionCoverage = RuleDecisionCoverage.EFFECT;
        else if (combined == CombinedCoverage.FALSETARGET
                || combined == CombinedCoverage.FALSECONDITION)
            ruleDecisionCoverage = RuleDecisionCoverage.NA;
        else
            ruleDecisionCoverage = RuleDecisionCoverage.INDETERMINATE;
    }

    CombinedCoverage getCombinedCoverage() {
        return combined;
    }

    RuleDecisionCoverage getRuleDecisionCoverage() {
        return ruleDecisionCoverage;
    }

    public enum IntermediateCoverage {
        TRUE, FALSE, ERROR, EMPTY, NOTEVALUATED
    }

    enum CombinedCoverage {
        BOTHTRUE, FALSETARGET, FALSECONDITION, ERRORTARGET, ERRORCONDITION
    }

    enum RuleDecisionCoverage {
        EFFECT, NA, INDETERMINATE
    }

}
