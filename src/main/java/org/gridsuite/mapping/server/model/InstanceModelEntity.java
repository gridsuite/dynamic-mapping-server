package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.springframework.data.cassandra.core.mapping.Table;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Table("instance_model")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceModelEntity {

    @Id
    // Could be replaced with UUID but we lose the ease of use of names
    private String id;

    private String modelName;

    private EquipmentType equipmentType;

    @Embedded
    private ModelParamsEmbeddable params;
}
