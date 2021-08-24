package org.gridsuite.mapping.server.dto.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gridsuite.mapping.server.utils.EquipmentType;

@AllArgsConstructor
@Getter
public class SimpleModel {
    private String name;
    private EquipmentType type;
}
