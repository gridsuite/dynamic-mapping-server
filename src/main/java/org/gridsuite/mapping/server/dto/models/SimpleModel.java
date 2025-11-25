package org.gridsuite.mapping.server.dto.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.utils.EquipmentType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
public class SimpleModel {
    private UUID id;
    private String name;
    private EquipmentType type;
    private List<SimpleGroup> groups;

    public SimpleModel(Model model) {
        id = model.getId();
        name = model.getModelName();
        type = model.getEquipmentType();
        groups = model.getSetsGroups().stream().map(SimpleGroup::new).collect(Collectors.toList());
    }
}
