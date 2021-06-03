/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;
import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Getter
@Setter
@Table(name = "mappings")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingEntity extends AbstractManuallyAssignedIdentifierEntity<String> {
    @Id
    private String name;

    @OneToMany(targetEntity = RuleEntity.class, mappedBy = "mapping", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RuleEntity> rules;

    @Override
    public String getId() {
        return name;
    }

    public MappingEntity(String name, MappingEntity mappingToCopy) {
        this.name = name;
        this.rules = mappingToCopy.getRules().stream().map(ruleEntity -> new RuleEntity(this, ruleEntity)).collect(Collectors.toList());
    }
}
