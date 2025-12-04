package org.gridsuite.mapping.server.dto.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.utils.SetGroupType;

import java.util.UUID;

@NoArgsConstructor
@Getter
public class SimpleGroup {
    private UUID id;
    private String name;
    private SetGroupType type;
    private int setsNumber;

    SimpleGroup(ParametersSetsGroup group) {
        id = group.getId();
        name = group.getName();
        type = group.getType();
        setsNumber = group.getSets().size();
    }
}
