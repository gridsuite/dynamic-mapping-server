/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.model.AutomatonPropertyEntity;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.utils.PropertyType;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CurrentLimitAutomaton extends AbstractAutomaton {
    private String side;

    private String controlledQuadripole;

    public ArrayList<BasicProperty> convertToBasicProperties() {
        ArrayList<BasicProperty> propertiesList = new ArrayList<>();
        propertiesList.add(new BasicProperty("iMeasurementSide", side));
        propertiesList.add(new BasicProperty("controlledQuadripole", "\"" + controlledQuadripole + "\""));
        return propertiesList;
    }

    public CurrentLimitAutomaton(AutomatonEntity automatonEntity) {
        this.setFamily(automatonEntity.getFamily());
        this.setModel(automatonEntity.getModel());
        this.setSetGroup(automatonEntity.getSetGroup());
        this.setWatchedElement(automatonEntity.getWatchedElement());
        // TODO Create generic function for all properties
        Optional<AutomatonPropertyEntity> foundSideProperty = automatonEntity.getProperties().stream().filter(property -> property.getName().equals("side")).findAny();
        if (foundSideProperty.isPresent()) {
            side = foundSideProperty.get().getValue();
        }

        Optional<AutomatonPropertyEntity> foundControlledQuadripoleProperty = automatonEntity.getProperties().stream().filter(property -> property.getName().equals("controlledQuadripole")).findAny();
        if (foundControlledQuadripoleProperty.isPresent()) {
            controlledQuadripole = foundControlledQuadripoleProperty.get().getValue();
        }
    }

    public AutomatonEntity convertAutomatonToEntity(MappingEntity parentMapping) {
        UUID createdId = UUID.randomUUID();
        AutomatonEntity convertedAutomaton = new AutomatonEntity();
        convertedAutomaton.setAutomatonId(createdId);
        convertedAutomaton.setFamily(this.getFamily());
        convertedAutomaton.setModel(this.getModel());
        convertedAutomaton.setSetGroup(this.getSetGroup());
        convertedAutomaton.setWatchedElement(this.getWatchedElement());
        convertedAutomaton.setMapping(parentMapping);
        ArrayList<AutomatonPropertyEntity> convertedProperties = new ArrayList<>();

        AutomatonPropertyEntity convertedProperty = new AutomatonPropertyEntity();
        convertedProperty.setAutomatonId(createdId);
        convertedProperty.setName("side");
        convertedProperty.setValue(this.getSide());
        convertedProperty.setType(PropertyType.STRING);
        convertedProperties.add(convertedProperty);

        convertedProperty = new AutomatonPropertyEntity();
        convertedProperty.setAutomatonId(createdId);
        convertedProperty.setName("controlledQuadripole");
        convertedProperty.setValue(this.getControlledQuadripole());
        convertedProperty.setType(PropertyType.STRING);
        convertedProperties.add(convertedProperty);

        convertedAutomaton.setProperties(convertedProperties);
        return convertedAutomaton;
    }
}


