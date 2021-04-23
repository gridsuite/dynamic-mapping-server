package org.gridsuite.mapping.server.dto.filters;

import lombok.Data;

@Data
public class EnumFilter extends Filter {

    private String[] value;

}
