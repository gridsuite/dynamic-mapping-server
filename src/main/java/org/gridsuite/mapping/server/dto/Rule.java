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
import lombok.Getter;
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.utils.*;
import org.gridsuite.mapping.server.dto.filters.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Getter
@AllArgsConstructor
@ApiModel("Rule")
public class Rule {
    @ApiModelProperty("Equipment type")
    private EquipmentType equipmentType;

    @ApiModelProperty("Mapped Model Instance ID")
    private String mappedModel;

    @ApiModelProperty("Composition")
    private String composition;

    @ApiModelProperty("Filters")
    private List<AbstractFilter> filters;

    public List<AbstractFilter> getFilters() {
        return filters;
    }

    public RuleEntity convertRuleToEntity(String mappingName) {
        UUID createdId = UUID.randomUUID();
        return RuleEntity.builder().composition(composition).mappedModel(mappedModel).equipmentType(equipmentType).mappingName(mappingName).ruleId(createdId).filters(filters.stream().map(filter -> filter.convertFilterToEntity(createdId)).collect(Collectors.toList())).build();
    }

    public Rule(RuleEntity ruleEntity) {
        equipmentType = ruleEntity.getEquipmentType();
        mappedModel = ruleEntity.getMappedModel();
        composition = ruleEntity.getComposition();
        filters = ruleEntity.getFilters().stream().map(filterEmbeddable -> AbstractFilter.createFilterFromEntity(filterEmbeddable)).collect(Collectors.toList());
    }

    // Needs to put the default rule last, hence going for the most specific rule to the most generic
    public static Comparator<Rule> ruleComparator = Comparator.comparing(rule -> -rule.getFilters().size());
}

