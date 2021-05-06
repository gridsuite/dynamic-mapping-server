package org.gridsuite.mapping.server.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Table(name = "scripts")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptEntity {
    @Id
    private String name;

    @Column(name = "parent", nullable = true)
    private String parentName;

    @Column(name = "script", nullable = false)
    private String script;
}
