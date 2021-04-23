package org.gridsuite.mapping.server.dto;

import java.util.Comparator;

public class InputMapping implements Mapping {
    private String name;
    private Rule[] rules;

    public String getName() {
        return name;
    }

    public Rule[] getRules() {
        return rules;
    }

    // Needs to put the default rule last, hence going for the most specific rule to the most generic
    public Comparator<Rule> ruleComparator = Comparator.comparing(rule -> -rule.getFilters().length);

}
