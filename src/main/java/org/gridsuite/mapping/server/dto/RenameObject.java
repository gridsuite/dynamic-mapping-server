package org.gridsuite.mapping.server.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RenameObject {
    @ApiModelProperty("Previous Name of the Object")
    private String oldName;

    @ApiModelProperty("New Name of the Object")
    private String newName;
}
