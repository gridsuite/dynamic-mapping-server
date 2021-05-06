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
public class MappingEntity {
    @Id
    private String name;

    @OneToMany(targetEntity = RuleEntity.class, mappedBy = "mappingName")
    private List<RuleEntity> rules;
}
