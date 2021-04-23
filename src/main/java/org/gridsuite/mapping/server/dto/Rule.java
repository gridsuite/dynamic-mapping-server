package org.gridsuite.mapping.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gridsuite.mapping.server.utils.*;
import org.gridsuite.mapping.server.dto.filters.*;

@Getter
@AllArgsConstructor
@ApiModel("Rule")
public class Rule   {
    @ApiModelProperty("Equipment type") private EquipmentType equipmentType;

    @ApiModelProperty("Mapped Model Instance ID")
    private String mappedModel;

    @ApiModelProperty("Composition")
    private String composition;

    @ApiModelProperty("Filters")
    private Filter[] filters;

    public Filter[] getFilters() {
        return filters;
    }

}

