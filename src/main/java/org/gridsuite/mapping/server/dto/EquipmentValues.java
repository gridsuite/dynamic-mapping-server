package org.gridsuite.mapping.server.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.gridsuite.mapping.server.utils.EquipmentType;

import java.util.List;
import java.util.Map;

@Data
@ApiModel("Equipment Values")
@AllArgsConstructor
public class EquipmentValues {
    private EquipmentType type;

    private Map<String, List<String>> values;
}