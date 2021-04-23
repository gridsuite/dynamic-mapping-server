package org.gridsuite.mapping.server.model;

import lombok.Builder;
import org.gridsuite.mapping.server.utils.*;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Table(name = "rule")
public class RuleEntity {

    @Id
    @GeneratedValue(strategy  =  GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EquipmentType equipmentType;

    @Column(name = "model", nullable = false)
    private String mappedModel;

    @Column(name = "composition", nullable = false)
    private String composition;

    @Column(name = "filter")
    @CollectionTable(foreignKey = @ForeignKey(name = "rule_id"))
    @ElementCollection
    private List<FilterEmbeddable> filters;


}
