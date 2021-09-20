package org.gridsuite.mapping.server.dto.models;

import lombok.Getter;
import org.gridsuite.mapping.server.utils.SetGroupType;

@Getter
public class SimpleGroup {
    private final String name;
    private final SetGroupType type;
    private final int setsNumber;

    SimpleGroup(ParametersSetsGroup group) {
        name = group.getName();
        type = group.getType();
        setsNumber = group.getSets().size();
    }
}
