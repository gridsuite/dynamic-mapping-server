package org.gridsuite.mapping.server.model;

import lombok.*;
import javax.persistence.*;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import java.util.List;

@Getter
@Setter
@Table("mapping")
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
