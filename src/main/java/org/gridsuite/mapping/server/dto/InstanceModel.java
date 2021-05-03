package org.gridsuite.mapping.server.dto;

import lombok.Data;
import org.gridsuite.mapping.server.model.InstanceModelEntity;
import org.gridsuite.mapping.server.utils.EquipmentType;

@Data
public class InstanceModel {
    private String id;

    private String modelName;

    private EquipmentType equipmentType;

    private ModelParams params;

    InstanceModel(InstanceModelEntity instanceModelEntity) {
        id = instanceModelEntity.getId();
        modelName = instanceModelEntity.getModelName();
        equipmentType = instanceModelEntity.getEquipmentType();
        params = new SetParams(instanceModelEntity.getParams());
    }
}
