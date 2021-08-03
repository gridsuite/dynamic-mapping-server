/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.gridsuite.mapping.server.model.ScriptEntity;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@Schema(description = "Script")
@AllArgsConstructor
public class Script {

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Name of the parent mapping")
    private String parentName;

    @Schema(description = "Generated Script")
    private String script;

    public Script(ScriptEntity scriptEntity) {
        name = scriptEntity.getName();
        parentName = scriptEntity.getParentName();
        script = scriptEntity.getScript();
    }

    public ScriptEntity convertToEntity() {
        return new ScriptEntity(name, parentName, script);
    }

}
