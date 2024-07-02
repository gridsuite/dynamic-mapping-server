package org.gridsuite.mapping.server.dto.automata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.gridsuite.mapping.server.utils.PropertyType;

@Data
@AllArgsConstructor
public class BasicProperty {
    @Schema(description = "Property name")
    private String name;
    @Schema(description = "Property value in string representation")
    private String value;
    @Schema(description = "Property type")
    private PropertyType type;
}
