package org.gridsuite.mapping.server.model;

import lombok.*;
import javax.persistence.*;
import java.util.List;

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
}
