/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;
import org.gridsuite.mapping.server.dto.automata.extensions.AutomatonSubtypesRegister;
import org.gridsuite.mapping.server.model.AutomatonEntity;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@Schema(description = "Mapping")
@AllArgsConstructor
@NoArgsConstructor
public class InputMapping implements Mapping {
    @Schema(description = "Name")
    private String name;

    @Schema(description = "Mapping rules")
    private List<Rule> rules;

    @Schema(description = "Mapping automata")
    private List<AbstractAutomaton> automata;

    @Schema(description = "Mapping should control its parameters")
    private boolean controlledParameters;

    public MappingEntity convertMappingToEntity(AutomatonSubtypesRegister automatonSubtypesRegister) {
        MappingEntity convertedMapping = new MappingEntity();
        convertedMapping.setName(name);
        convertedMapping.setControlledParameters(controlledParameters);
        convertedMapping.setRules(rules.stream().map(rule -> rule.convertRuleToEntity(convertedMapping)).collect(Collectors.toList()));
        convertedMapping.setAutomata(automata.stream().map(automaton -> {
            try {
                AutomatonEntity automatonEntity = automatonSubtypesRegister.toEntity(automaton);
                automatonEntity.setMapping(convertedMapping);
                return automatonEntity;
            } catch (Exception e) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }).collect(Collectors.toList()));
        return convertedMapping;
    }

    public InputMapping(MappingEntity mappingEntity, AutomatonSubtypesRegister automatonSubtypesRegister) {
        name = mappingEntity.getName();
        controlledParameters = mappingEntity.isControlledParameters();
        rules = mappingEntity.getRules().stream().map(Rule::new).collect(Collectors.toList());
        automata = mappingEntity.getAutomata().stream().map(automaton -> {
            try {
                return automatonSubtypesRegister.fromEntity(automaton);
            } catch (Exception e) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }).collect(Collectors.toList());
    }
}
