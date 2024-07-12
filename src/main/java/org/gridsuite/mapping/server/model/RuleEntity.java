/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import jakarta.persistence.*;
import lombok.*;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.SetGroupType;

import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Entity
@Builder
@Table(name = "rules", indexes = {@Index(name = "rule_mappingName_index", columnList = "mappingName")})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RuleEntity extends AbstractManuallyAssignedIdentifierEntity<UUID> {

    public interface ProjectionFilterUuid {
        UUID getFilterUuid();
    }

    @Id
    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "type", nullable = false)
    @Enumerated
    private EquipmentType equipmentType;

    @Column(name = "model", nullable = false)
    private String mappedModel;

    @Column(name = "set_group", nullable = false)
    private String setGroup;

    @Column(name = "group_type", nullable = false)
    private SetGroupType groupType;

    // filter is an expert filter which is persisted in the filter-server
    @Column(name = "filter_uuid")
    private UUID filterUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mappingName", foreignKey = @ForeignKey(name = "mapping_rules_fk"), referencedColumnName = "name")
    private MappingEntity mapping;

    @Override
    public UUID getId() {
        return ruleId;
    }

    public RuleEntity(MappingEntity mapping, RuleEntity ruleToCopy) {
        UUID newID = UUID.randomUUID();
        this.ruleId = newID;
        this.mapping = mapping;
        this.equipmentType = ruleToCopy.getEquipmentType();
        this.mappedModel = ruleToCopy.getMappedModel();
        this.setGroup = ruleToCopy.getSetGroup();
        this.groupType = ruleToCopy.getGroupType();
        this.filterUuid = ruleToCopy.getFilterUuid();
    }
}
