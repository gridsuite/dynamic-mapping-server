/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.automata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.utils.AutomatonFamily;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "family", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CurrentLimitAutomaton.class, name = "CURRENT_LIMIT"),
    @JsonSubTypes.Type(value = TapChangerBlocking.class, name = "VOLTAGE"),
})
@Data
@NoArgsConstructor
public abstract class AbstractAutomaton {
    @Schema(description = "Automaton family")
    @JsonProperty
    private AutomatonFamily family;

    @Schema(description = "Mapped Model Instance ID")
    private String model;

    @Schema(description = "Mapped Parameters Set Group ID")
    private String setGroup;

    protected AbstractAutomaton(AutomatonEntity automatonEntity) {
        this.setFamily(automatonEntity.getFamily());
        this.setModel(automatonEntity.getModel());
        this.setSetGroup(automatonEntity.getSetGroup());
    }

    public abstract List<BasicProperty> convertToBasicProperties();

    public AutomatonEntity convertAutomatonToEntity(MappingEntity parentMapping) {
        UUID createdId = UUID.randomUUID();
        AutomatonEntity convertedAutomaton = new AutomatonEntity();
        convertedAutomaton.setAutomatonId(createdId);
        convertedAutomaton.setFamily(this.getFamily());
        convertedAutomaton.setModel(this.getModel());
        convertedAutomaton.setSetGroup(this.getSetGroup());

        convertedAutomaton.setMapping(parentMapping);
        return convertedAutomaton;
    }

    public static AbstractAutomaton instantiateFromEntity(AutomatonEntity automatonEntity) {
        if (automatonEntity.getFamily() == AutomatonFamily.CURRENT_LIMIT) {
            return new CurrentLimitAutomaton(automatonEntity);
        } else if (automatonEntity.getFamily() == AutomatonFamily.VOLTAGE) {
            return new TapChangerBlocking(automatonEntity);
        } else {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }
    }
}

