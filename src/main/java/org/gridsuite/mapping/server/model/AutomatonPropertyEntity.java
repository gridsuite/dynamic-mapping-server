/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.utils.PropertyType;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */

@Inheritance
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "automaton_properties", indexes = {@Index(name = "property_automaton_id_index", columnList = "automaton_id")})
@IdClass(AutomatonPropertyId.class)
public class AutomatonPropertyEntity implements Serializable {

    @Id
    @Column(name = "automaton_id")
    private UUID automatonId;

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    @Column(name = "type")
    private PropertyType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "automaton_id", foreignKey = @ForeignKey(name = "automata_property_fk"))
    @MapsId("automatonId")
    private AutomatonEntity automaton;

    public AutomatonPropertyEntity(UUID automatonId, AutomatonPropertyEntity automatonPropertyEntity) {
        this.automatonId = automatonId;
        this.name = automatonPropertyEntity.getName();
        this.type = automatonPropertyEntity.getType();
        this.value = automatonPropertyEntity.getValue();

    }

}
