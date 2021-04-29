package org.gridsuite.mapping.server.dto;

import org.gridsuite.mapping.server.utils.EquipmentType;

public class SortedRules {

    private EquipmentType type;

    private Rule[] rules;

    public EquipmentType getType() {
        return type;
    }

    public Rule[] getRule() {
        return rules;
    }
}