package org.gridsuite.mapping.server.dto;

import lombok.Data;
import org.gridsuite.mapping.server.model.ModelParamsEmbeddable;
import org.gridsuite.mapping.server.utils.ParamsType;

@Data
public class SetParams implements ModelParams {
    private String name;
    private ParamsType type;

    SetParams(ModelParamsEmbeddable modelParamsEmbeddable) {
        name = modelParamsEmbeddable.getName();
        type = modelParamsEmbeddable.getType();
    }
}
