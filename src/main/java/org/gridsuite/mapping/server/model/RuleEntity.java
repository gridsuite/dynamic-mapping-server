package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.utils.*;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Table(name = "rule")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rule_id")
    private UUID id;

    @Column(name = "mappingName", nullable = false)
    private String mappingName;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EquipmentType equipmentType;

    @Column(name = "model", nullable = false)
    private String mappedModel;

    @Column(name = "composition", nullable = false)
    private String composition;

    @Column(name = "filters")
    @CollectionTable(name = "filters", joinColumns = @JoinColumn(name = "rule_id"))
    @ElementCollection
    private List<FilterEmbeddable> filters;
}
