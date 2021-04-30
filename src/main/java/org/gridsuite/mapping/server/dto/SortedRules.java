package org.gridsuite.mapping.server.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.utils.EquipmentType;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
public class SortedRules {

    private EquipmentType type;

    private FlatRule[] rules;

    public SortedRules(EquipmentType type, ArrayList<Rule> sortedRules) {
        this.type = type;
        rules = sortedRules.stream().map(rule -> )
    }
}
