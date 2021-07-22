/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.gridsuite.mapping.server.model.ScriptEntity;

import java.util.Date;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@ApiModel("Script")
@AllArgsConstructor
public class Script {

    @ApiModelProperty("Name")
    private String name;

    @ApiModelProperty("Name of the parent mapping")
    private String parentName;

    @ApiModelProperty("Generated Script")
    private String script;

    @JsonIgnore
    @ApiModelProperty("Creation date")
    private Date createdDate;

    public Script(ScriptEntity scriptEntity) {
        name = scriptEntity.getName();
        parentName = scriptEntity.getParentName();
        script = scriptEntity.getScript();
        createdDate = scriptEntity.getCreatedDate();
    }

    public ScriptEntity convertToEntity() {
        return new ScriptEntity(name, parentName, script, createdDate);
    }

}
