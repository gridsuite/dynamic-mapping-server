package org.gridsuite.mapping.server.model;

import lombok.*;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Table("mapping")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptEntity {
    @Id
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private String name;

    @Column(name = "parent", nullable = true)
    private String parentName;

    @Column(name = "script", nullable = false)
    private String script;
}
