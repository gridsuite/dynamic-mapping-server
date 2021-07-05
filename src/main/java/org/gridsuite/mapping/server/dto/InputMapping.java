/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("Mapping")
@AllArgsConstructor
public class InputMapping implements Mapping {
    @ApiModelProperty("Name")
    private String name;

    @ApiModelProperty("Mapping rules")
    private List<Rule> rules;

    @ApiModelProperty("Mapping automata")
    private List<AbstractAutomaton> automata;

    public MappingEntity convertMappingToEntity() {
        MappingEntity convertedMapping = new MappingEntity();
        convertedMapping.setName(name);
        convertedMapping.setRules(rules.stream().map(rule -> rule.convertRuleToEntity(convertedMapping)).collect(Collectors.toList()));
        convertedMapping.setAutomata(automata.stream().map(automaton -> automaton.convertAutomatonToEntity(convertedMapping)).collect(Collectors.toList()));
        return convertedMapping;
    }

    public InputMapping(MappingEntity mappingEntity) {
        name = mappingEntity.getName();
        rules = mappingEntity.getRules().stream().map(ruleEntity -> new Rule(ruleEntity)).collect(Collectors.toList());
        automata = mappingEntity.getAutomata().stream().map(automatonEntity -> AbstractAutomaton.instantiateFromEntity(automatonEntity)).collect(Collectors.toList());
    }
}
