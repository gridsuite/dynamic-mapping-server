package org.gridsuite.mapping.server;

import org.gridsuite.mapping.server.dto.InstanceModel;
import org.gridsuite.mapping.server.model.InstanceModelEntity;
import org.gridsuite.mapping.server.model.ModelParamsEmbeddable;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.ParamsType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InstanceModelTest {

    // Bare bone because not implemented yet, assumes models already in database
    @Test
    public void test() {

        String modelId = "id";
        String modelName = "modelName";
        EquipmentType equipmentType = EquipmentType.GENERATOR;
        String setName = "setName";
        ParamsType paramsType = ParamsType.FIXED;

        InstanceModelEntity modelEntity = new InstanceModelEntity(modelId, modelName, equipmentType, new ModelParamsEmbeddable(setName, paramsType));

        InstanceModel model = new InstanceModel(modelEntity);

        assertEquals(modelId, model.getId());
        assertEquals(modelName, model.getModelName());
        assertEquals(equipmentType, model.getEquipmentType());
        assertEquals(setName, model.getParams().getName());
        assertEquals(paramsType, model.getParams().getType());

    }

}
