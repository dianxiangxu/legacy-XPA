package org.sag.coverage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PolicyCoverageFactory {
    private static List<List<Coverage>> coverageMatrix;
    private static List<Boolean> results;

    static void addCoverage(Coverage coverage) {
        coverageMatrix.get(coverageMatrix.size() - 1).add(coverage);
    }

    static void newRow() {
        coverageMatrix.add(new ArrayList<Coverage>());
    }

    static void init() {
        coverageMatrix = new ArrayList<>();
    }

    public static List<List<Coverage>> getCoverageMatrix() {
        return Collections.unmodifiableList(coverageMatrix);
    }

    public static List<Boolean> getResults() {
        return Collections.unmodifiableList(results);
    }

    static void setResults(List<Boolean> testResults) {
        results = testResults;
    }
}
