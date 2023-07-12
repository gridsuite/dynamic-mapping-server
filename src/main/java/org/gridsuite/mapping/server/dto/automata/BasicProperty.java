package org.gridsuite.mapping.server.dto.automata;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.gridsuite.mapping.server.utils.PropertyType;

@Data
@AllArgsConstructor
public class BasicProperty {
    private String name;

    private String value;

    private PropertyType type;
}
