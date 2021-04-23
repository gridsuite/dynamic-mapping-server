package org.gridsuite.mapping.server.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

@Getter
@Setter
public class SortedMapping implements Mapping {
    private String name;
    private SortedRules[] sortedRules;
    // Needs to put the default rule last, hence going for the most specific rule to the most generic
    public static Comparator<Rule> ruleComparator = Comparator.comparing(rule -> -rule.getFilters().length);

}
