package org.sag.mutation;

import org.wso2.balana.AbstractPolicy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by shuaipeng on 9/8/16.
 */
public class Mutant extends AbstractPolicy {
    private String name;
    private AbstractPolicy policy;
    /**
     * unmodifiable list
     */
    private List<Integer> faultLocations;

    public Mutant(AbstractPolicy policy) {
        this(policy, new ArrayList<>());
    }

    public Mutant(AbstractPolicy policy, List<Integer> faultLocations) {
        this.policy = policy;
        faultLocations = Collections.unmodifiableList(faultLocations);
    }

    public AbstractPolicy getPolicy() {
        return policy;
    }

    @Override
    public String encode() {
        return policy.encode();
    }

    @Override
    public void encode(StringBuilder builder) {
        policy.encode(builder);
    }
}
