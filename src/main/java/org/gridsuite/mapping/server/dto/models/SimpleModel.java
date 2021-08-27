package org.gridsuite.mapping.server.dto.models;

import lombok.Getter;
import org.gridsuite.mapping.server.utils.EquipmentType;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class SimpleModel {
    private final String name;
    private final EquipmentType type;
    private final List<SimpleGroup> groups;

    public SimpleModel(Model model) {
        name = model.getModelName();
        type = model.getEquipmentType();
        groups = model.getSetsGroups().stream().map(SimpleGroup::new).collect(Collectors.toList());
    }
}
