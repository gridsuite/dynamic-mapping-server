package org.gridsuite.mapping.server.dto.models;

import lombok.Getter;
import org.gridsuite.mapping.server.utils.SetGroupType;

@Getter
public class SimpleGroup {
    private final String name;
    private final SetGroupType type;

    SimpleGroup(ParametersSetsGroup group) {
        name = group.getName();
        type = group.getType();
    }
}
