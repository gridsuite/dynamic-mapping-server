/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

import org.gridsuite.mapping.server.utils.*;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Inheritance
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "filters", indexes = {@Index(name = "filter_rule_id_index", columnList = "rule_id")})
@IdClass(FilterId.class)
public class FilterEntity implements Serializable {
    @Id
    @Column(name = "filter_id")
    private String filterId;

    @Id
    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "property")
    private String property;

    @Column(name = "type")
    @Enumerated
    private PropertyType type;

    @Column(name = "operand")
    @Enumerated
    private Operands operand;

    // Value is the value casted to a string for persistence issue
    @Column(name = "value")
    private String value;

    @ManyToOne
    @JoinColumn(name = "rule_id", foreignKey = @ForeignKey(name = "rules_filter_fk"))
    @MapsId("ruleId")
    private RuleEntity rule;

    public FilterEntity(UUID ruleId, FilterEntity filterEntity) {
        this.filterId = filterEntity.getFilterId();
        this.ruleId = ruleId;
        this.property = filterEntity.getProperty();
        this.type = filterEntity.getType();
        this.operand = filterEntity.getOperand();
        this.value = filterEntity.getValue();

    }

}
