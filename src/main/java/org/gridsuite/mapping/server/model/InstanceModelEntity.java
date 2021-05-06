package org.gridsuite.mapping.server.model;

import lombok.*;
import org.gridsuite.mapping.server.utils.EquipmentType;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Table(name = "instance_models")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceModelEntity {

    // Could be replaced with UUID but we lose the ease of use of names
    @Id
    private String id;

    private String modelName;

    private EquipmentType equipmentType;

    @Embedded
    private ModelParamsEmbeddable params;
}
