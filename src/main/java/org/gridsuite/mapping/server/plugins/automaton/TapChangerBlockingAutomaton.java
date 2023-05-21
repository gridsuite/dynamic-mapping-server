/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.plugins.automaton;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;
import org.gridsuite.mapping.server.dto.automata.BasicProperty;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.model.AutomatonPropertyEntity;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.utils.Methods;
import org.gridsuite.mapping.server.utils.PropertyType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TapChangerBlockingAutomaton extends AbstractAutomaton {

    public static final String MODEL_CLASS = "TapChangerBlockingAutomaton";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_U_MEASUREMENTS = "uMeasurements";
    public static final String PROPERTY_TRANSFORMERS = "transformers";

    @Schema(description = "Name property")
    @JsonProperty(PROPERTY_NAME)
    private String name;

    @Schema(description = "U Measurement property")
    @JsonProperty(PROPERTY_U_MEASUREMENTS)
    private List<String> uMeasurements;

    @Schema(description = "Transformers property")
    @JsonProperty(PROPERTY_TRANSFORMERS)
    private List<String> transformers;

    public TapChangerBlockingAutomaton(AutomatonEntity automatonEntity) {
        super(automatonEntity);

        name = automatonEntity.getProperty(PROPERTY_NAME);
        uMeasurements = automatonEntity.getProperty(PROPERTY_U_MEASUREMENTS, Methods::convertStringToList);
        transformers = automatonEntity.getProperty(PROPERTY_TRANSFORMERS, Methods::convertStringToList);
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getExportedClassName() {
        return MODEL_CLASS;
    }

    @Override
    public ArrayList<BasicProperty> convertToBasicProperties() {
        ArrayList<BasicProperty> properties = new ArrayList<>();
        properties.add(new BasicProperty(PROPERTY_U_MEASUREMENTS,
                uMeasurements.stream().map(elem -> "\"" + elem + "\"").collect(Collectors.joining(", "))));
        properties.add(new BasicProperty(PROPERTY_TRANSFORMERS,
                transformers.stream().map(elem -> "\"" + elem + "\"").collect(Collectors.joining(", "))));
        return properties;
    }

    @Override
    public AutomatonEntity toEntity(MappingEntity parentMappingEntity) {
        AutomatonEntity convertedAutomaton = super.toEntity(parentMappingEntity);

        convertedAutomaton.addProperty(new AutomatonPropertyEntity(convertedAutomaton.getAutomatonId(),
                PROPERTY_NAME, this.getName(), PropertyType.STRING, convertedAutomaton));

        convertedAutomaton.addProperty(new AutomatonPropertyEntity(convertedAutomaton.getAutomatonId(),
                PROPERTY_U_MEASUREMENTS, Methods.convertListToString(this.getUMeasurements()), PropertyType.STRING, convertedAutomaton));

        convertedAutomaton.addProperty(new AutomatonPropertyEntity(convertedAutomaton.getAutomatonId(),
                PROPERTY_TRANSFORMERS, Methods.convertListToString(this.getTransformers()), PropertyType.STRING, convertedAutomaton));

        return convertedAutomaton;
    }
}
