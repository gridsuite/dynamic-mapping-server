/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.mapping.server.dto.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.ModelVariableSetEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VariablesSet {
    private UUID id;
    private String name;
    private List<ModelVariableDefinition> variableDefinitions;

    public VariablesSet(ModelVariableSetEntity variableSetEntity) {
        this.id = variableSetEntity.getId();
        this.name = variableSetEntity.getName();
        this.variableDefinitions = variableSetEntity.getVariableDefinitions().stream().map(ModelVariableDefinition::new).collect(Collectors.toList());
    }

}
