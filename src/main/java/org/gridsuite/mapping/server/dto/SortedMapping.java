package org.gridsuite.mapping.server.dto;

import lombok.Getter;
import lombok.Setter;
import org.gridsuite.mapping.server.utils.EquipmentType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@Getter
    @Setter
    public class SortedMapping implements Mapping {
        private String name;
        private ArrayList<SortedRules> sortedRules;

        public SortedMapping(InputMapping mapping) {
            name = mapping.getName();
            sortedRules = new ArrayList<SortedRules>();
            HashMap<EquipmentType, ArrayList<Rule>> sortingRules =  new HashMap<EquipmentType, ArrayList<Rule>>();
            mapping.getRules().stream().forEach(rule -> {
                        EquipmentType ruleType = rule.getEquipmentType();
                        if (sortingRules.keySet().contains(ruleType)) {
                            ArrayList<Rule> associatedRules = sortingRules.get(ruleType);
                            associatedRules.add(rule);
                        } else {
                            ArrayList<Rule> newRules = new ArrayList<Rule>();
                            newRules.add(rule);
                            sortingRules.put(ruleType, newRules);
                        }
            });

            for (EquipmentType type: sortingRules.keySet()) {
                ArrayList<Rule> typedRules =  sortingRules.get(type);
                typedRules.sort(Rule.ruleComparator);
                sortedRules.add(new SortedRules(type, typedRules));
            }
        }

    }
