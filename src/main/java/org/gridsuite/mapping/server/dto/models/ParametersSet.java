/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.gridsuite.mapping.server.model.ModelParameterSetEntity;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
public class ParametersSet {
    private String name;
    private List<ModelParameter> parameters;
    @JsonIgnore
    private Date lastModifiedDate;

    private String modelName;

    public ParametersSet(ModelParameterSetEntity modelParameterSetEntity) {
        name = modelParameterSetEntity.getName();
        parameters = modelParameterSetEntity.getParameters().stream().map(modelParameterEntity -> new ModelParameter(modelParameterEntity)).collect(Collectors.toList());
        lastModifiedDate = modelParameterSetEntity.getLastModifiedDate();
        modelName = modelParameterSetEntity.getModelName();
    }

    public ParametersSet(String name, List<ModelParameter> parameters, String modelName) {
        this.name = name;
        this.parameters = parameters;
        lastModifiedDate = new Date();
        this.modelName = modelName;
    }
}
