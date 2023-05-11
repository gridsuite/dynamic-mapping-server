/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.model.AutomatonPropertyEntity;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.utils.PropertyType;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CurrentLimitAutomaton extends AbstractAutomaton {
    private String side;

    public CurrentLimitAutomaton(AutomatonEntity automatonEntity) {
        super(automatonEntity);

        // TODO Create generic function for all properties
        Optional<AutomatonPropertyEntity> foundSideProperty = automatonEntity.getProperties().stream().filter(property -> property.getName().equals("side")).findAny();
        if (foundSideProperty.isPresent()) {
            side = foundSideProperty.get().getValue();
        }
    }

    @Override
    public ArrayList<BasicProperty> convertToBasicProperties() {
        ArrayList<BasicProperty> propertiesList = new ArrayList<>();
        propertiesList.add(new BasicProperty("side", side));
        return propertiesList;
    }

    @Override
    public AutomatonEntity convertAutomatonToEntity(MappingEntity parentMapping) {
        AutomatonEntity convertedAutomaton = super.convertAutomatonToEntity(parentMapping);

        // additional properties
        ArrayList<AutomatonPropertyEntity> convertedProperties = new ArrayList<>();
        convertedAutomaton.setProperties(convertedProperties);

        // side property
        AutomatonPropertyEntity convertedProperty = new AutomatonPropertyEntity();
        convertedProperty.setAutomatonId(convertedAutomaton.getAutomatonId());
        convertedProperty.setName("side");
        convertedProperty.setValue(this.getSide());
        convertedProperty.setType(PropertyType.STRING);
        convertedProperties.add(convertedProperty);

        return convertedAutomaton;
    }
}


