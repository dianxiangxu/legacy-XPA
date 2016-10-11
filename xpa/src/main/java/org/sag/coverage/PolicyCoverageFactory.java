package org.sag.coverage;

import java.util.ArrayList;
import java.util.List;

public class PolicyCoverageFactory {
    private static List<List<Coverage>> coverageMatrix;

    static void addCoverage(Coverage coverage) {
        coverageMatrix.get(coverageMatrix.size() - 1).add(coverage);
    }

    static void newRow() {
        coverageMatrix.add(new ArrayList<>());
    }

    static void init() {
        coverageMatrix = new ArrayList<>();
    }
}
