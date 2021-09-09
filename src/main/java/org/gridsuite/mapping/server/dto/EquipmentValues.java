package org.gridsuite.mapping.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.gridsuite.mapping.server.utils.EquipmentType;

import java.util.Map;
import java.util.Set;

@Data
@Schema(description = "Equipment Values")
@AllArgsConstructor
public class EquipmentValues {
    private EquipmentType type;

    private Map<String, Set<String>> values;
}
