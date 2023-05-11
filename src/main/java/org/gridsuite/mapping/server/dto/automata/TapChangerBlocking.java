/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.model.AutomatonPropertyEntity;
import org.gridsuite.mapping.server.model.MappingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TapChangerBlocking extends AbstractAutomaton {

    @Schema(description = "Name")
    private String name;

    @Schema(description = "U Measurement")
    @JsonProperty("uMeasurements")
    private List<String> uMeasurements;

    @Schema(description = "Transformers ")
    private List<String> transformers;

    public TapChangerBlocking(AutomatonEntity automatonEntity) {
        super(automatonEntity);
        this.setName(automatonEntity.getName());
        this.setUMeasurements(new ArrayList<>(automatonEntity.getUMeasurements()));
        this.setTransformers(new ArrayList<>(automatonEntity.getTransformers()));
    }

    @Override
    public ArrayList<BasicProperty> convertToBasicProperties() {
        ArrayList<BasicProperty> propertiesList = new ArrayList<>();
        return propertiesList;
    }

    @Override
    public AutomatonEntity convertAutomatonToEntity(MappingEntity parentMapping) {
        AutomatonEntity convertedAutomaton = super.convertAutomatonToEntity(parentMapping);

        // model properties
        convertedAutomaton.setName(this.getName());
        convertedAutomaton.setUMeasurements(this.getUMeasurements());
        convertedAutomaton.setTransformers(this.getTransformers());

        // additional properties
        ArrayList<AutomatonPropertyEntity> convertedProperties = new ArrayList<>();
        convertedAutomaton.setProperties(convertedProperties);

        return convertedAutomaton;
    }
}
