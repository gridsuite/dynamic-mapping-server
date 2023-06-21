/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.utils.AutomatonFamily;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@NoArgsConstructor
public class Automaton {
    @Schema(description = "Automaton family")
    @JsonProperty
    private AutomatonFamily family;

    @Schema(description = "Mapped Model Instance ID")
    private String model;

    @Schema(description = "Mapped Parameters Set Group ID")
    private String setGroup;

    @Schema(description = "Properties of automaton model")
    private List<BasicProperty> properties;

    public Automaton(AutomatonEntity automatonEntity) {
        this.family = automatonEntity.getFamily();
        this.model = automatonEntity.getModel();
        this.setGroup = automatonEntity.getSetGroup();
        this.properties = automatonEntity.getProperties().stream()
                .map(propertyEntity -> new BasicProperty(propertyEntity.getName(), propertyEntity.getValue(), propertyEntity.getType()))
                .collect(Collectors.toList());
    }
}

