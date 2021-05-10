package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.utils.*;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Table(name = "rules")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RuleEntity {

    @Id
    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "mappingName", nullable = false)
    private String mappingName;

    @Column(name = "type", nullable = false)
    @Enumerated
    private EquipmentType equipmentType;

    @Column(name = "model", nullable = false)
    private String mappedModel;

    @Column(name = "composition", nullable = false)
    private String composition;

    @OneToMany(targetEntity = FilterEntity.class, mappedBy = "ruleId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FilterEntity> filters;
}
