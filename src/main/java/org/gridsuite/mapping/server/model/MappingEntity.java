package org.gridsuite.mapping.server.model;

import lombok.*;
import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

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

    @OneToMany(targetEntity = RuleEntity.class, mappedBy = "mappingName", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RuleEntity> rules;

    @Override
    public String getId() {
        return name;
    }

    public MappingEntity(String name, MappingEntity mappingToCopy) {
        this.name = name;
        this.rules = mappingToCopy.getRules().stream().map(ruleEntity -> new RuleEntity(name, ruleEntity)).collect(Collectors.toList());
    }
}
