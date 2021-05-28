package org.gridsuite.mapping.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.gridsuite.mapping.server.model.ScriptEntity;

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

    public Script(ScriptEntity scriptEntity) {
        name = scriptEntity.getName();
        parentName = scriptEntity.getParentName();
        script = scriptEntity.getScript();
    }

    public ScriptEntity convertToEntity() {
        return new ScriptEntity(name, parentName, script);
    }

}
