/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.model.RuleEntity;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.SetGroupType;

import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Rule")
public class Rule {
    @Schema(description = "Rule id")
    private UUID id;

    @Schema(description = "Equipment type")
    private EquipmentType equipmentType;

    @Schema(description = "Mapped Model Instance ID")
    private String mappedModel;

    @Schema(description = "Mapped Parameter Set Group ID")
    private String setGroup;

    @Schema(description = "Mapped Parameter Set Group Type")
    private SetGroupType groupType;

    @Schema(description = "Filter")
    private ExpertFilter filter;

    @JsonIgnore
    private UUID filterUuid;

    public RuleEntity convertRuleToEntity(MappingEntity parentMapping) {
        RuleEntity convertedRule = new RuleEntity();
        if (this.id != null) {
            convertedRule.setRuleId(this.id);
        } else {
            UUID createdId = UUID.randomUUID();
            convertedRule.setRuleId(createdId);
        }
        convertedRule.setMappedModel(mappedModel);
        convertedRule.setSetGroup(setGroup);
        convertedRule.setGroupType(groupType);
        convertedRule.setEquipmentType(equipmentType);
        convertedRule.setMapping(parentMapping);
        if (filter != null) {
            if (filter.getId() != null) {
                convertedRule.setFilterUuid(filter.getId());
            } else {
                UUID createdFilterId = UUID.randomUUID();
                this.filter.setId(createdFilterId);
                convertedRule.setFilterUuid(createdFilterId);
            }
        }
        return convertedRule;
    }

    public Rule(RuleEntity ruleEntity) {
        id = ruleEntity.getRuleId();
        equipmentType = ruleEntity.getEquipmentType();
        mappedModel = ruleEntity.getMappedModel();
        setGroup = ruleEntity.getSetGroup();
        groupType = ruleEntity.getGroupType();
        filterUuid = ruleEntity.getFilterUuid();
    }
}

