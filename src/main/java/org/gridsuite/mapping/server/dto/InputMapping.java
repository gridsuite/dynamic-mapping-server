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
import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;
import org.gridsuite.mapping.server.model.MappingEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@Schema(description = "Mapping")
@AllArgsConstructor
public class InputMapping implements Mapping {
    @Schema(description = "Name")
    private String name;

    @Schema(description = "Mapping rules")
    private List<Rule> rules;

    @Schema(description = "Mapping automata")
    private List<AbstractAutomaton> automata;

    @Schema(description = "Mapping should control its parameters")
    private boolean controlledParameters;

    public MappingEntity convertMappingToEntity() {
        MappingEntity convertedMapping = new MappingEntity();
        convertedMapping.setName(name);
        convertedMapping.setControlledParameters(controlledParameters);
        convertedMapping.setRules(rules.stream().map(rule -> rule.convertRuleToEntity(convertedMapping)).collect(Collectors.toList()));
        convertedMapping.setAutomata(automata.stream().map(automaton -> automaton.convertAutomatonToEntity(convertedMapping)).collect(Collectors.toList()));
        return convertedMapping;
    }

    public InputMapping(MappingEntity mappingEntity) {
        name = mappingEntity.getName();
        controlledParameters = mappingEntity.isControlledParameters();
        rules = mappingEntity.getRules().stream().map(Rule::new).collect(Collectors.toList());
        automata = mappingEntity.getAutomata().stream().map(AbstractAutomaton::instantiateFromEntity).collect(Collectors.toList());
    }
}
