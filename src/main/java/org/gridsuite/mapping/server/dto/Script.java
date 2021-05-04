package org.gridsuite.mapping.server.dto;

import lombok.Data;
import org.gridsuite.mapping.server.model.ScriptEntity;

@Data
public class Script {

    private String name;
    private String parentName;
    private String script;

    public Script(ScriptEntity scriptEntity) {
        name = scriptEntity.getName();
        parentName = scriptEntity.getParentName();
        script = scriptEntity.getScript();
    }

}
