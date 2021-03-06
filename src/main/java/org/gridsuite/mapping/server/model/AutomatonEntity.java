/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.utils.AutomatonFamily;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Entity
@Builder
@Table(name = "automata", indexes = {@Index(name = "automaton_mappingName_index", columnList = "mappingName")})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AutomatonEntity extends AbstractManuallyAssignedIdentifierEntity<UUID> {

    @Id
    @Column(name = "automaton_id")
    private UUID automatonId;

    @Column(name = "type", nullable = false)
    @Enumerated
    private AutomatonFamily family;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "set_group", nullable = false)
    private String setGroup;

    @Column(name = "watched_element", nullable = false)
    private String watchedElement;

    @OneToMany(targetEntity = AutomatonPropertyEntity.class, mappedBy = "automaton", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AutomatonPropertyEntity> properties;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mappingName", foreignKey = @ForeignKey(name = "mapping_automata_fk"), referencedColumnName = "name")
    private MappingEntity mapping;

    @Override
    public UUID getId() {
        return automatonId;
    }

    public AutomatonEntity(MappingEntity mapping, AutomatonEntity automatonToCopy) {
        UUID newID = UUID.randomUUID();
        this.automatonId = newID;
        this.mapping = mapping;
        this.family = automatonToCopy.getFamily();
        this.model = automatonToCopy.getModel();
        this.setGroup = automatonToCopy.getSetGroup();
        this.watchedElement = automatonToCopy.getWatchedElement();
        this.properties = automatonToCopy.getProperties().stream().map(automatonPropertyEntity -> new AutomatonPropertyEntity(newID, automatonPropertyEntity)).collect(Collectors.toList());

    }

    public String[] getInstantiatedModel() {
        return new String[]{model, setGroup};
    }
}
