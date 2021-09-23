/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.gridsuite.mapping.server.dto.filters.AbstractFilter;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.SetGroupType;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Rule")
public class Rule {
    @Schema(description = "Equipment type")
    private EquipmentType equipmentType;

    @Schema(description = "Mapped Model Instance ID")
    private String mappedModel;

    @Schema(description = "Mapped Parameter Set Group ID")
    private String setGroup;

    @Schema(description = "Mapped Parameter Set Group Type")
    private SetGroupType groupType;

    @Schema(description = "Composition")
    private String composition;

    @Schema(description = "Filters")
    private List<AbstractFilter> filters;

    public List<AbstractFilter> getFilters() {
        return filters;
    }

    public RuleEntity convertRuleToEntity(MappingEntity parentMapping) {
        UUID createdId = UUID.randomUUID();
        RuleEntity convertedRule = new RuleEntity();
        convertedRule.setComposition(composition);
        convertedRule.setRuleId(createdId);
        convertedRule.setMappedModel(mappedModel);
        convertedRule.setSetGroup(setGroup);
        convertedRule.setGroupType(groupType);
        convertedRule.setEquipmentType(equipmentType);
        convertedRule.setMapping(parentMapping);
        convertedRule.setFilters(filters.stream().map(filter -> filter.convertFilterToEntity(convertedRule)).collect(Collectors.toList()));
        return convertedRule;
    }

    public Rule(RuleEntity ruleEntity) {
        equipmentType = ruleEntity.getEquipmentType();
        mappedModel = ruleEntity.getMappedModel();
        setGroup = ruleEntity.getSetGroup();
        groupType = ruleEntity.getGroupType();
        composition = ruleEntity.getComposition();
        filters = ruleEntity.getFilters().stream().map(filterEmbeddable -> AbstractFilter.createFilterFromEntity(filterEmbeddable)).collect(Collectors.toList());
    }

    // Needs to put the default rule last, hence going for the most specific rule to the most generic
    public static Comparator<Rule> ruleComparator = Comparator.comparing(rule -> -rule.getFilters().size());
}

