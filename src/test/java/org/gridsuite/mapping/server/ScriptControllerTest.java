/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import org.gridsuite.mapping.server.model.*;
import org.gridsuite.mapping.server.repository.InstanceModelRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.repository.ScriptRepository;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.gridsuite.mapping.server.utils.ParameterType;
import org.gridsuite.mapping.server.utils.ParamsType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {MappingApplication.class})
public class ScriptControllerTest {

    @Autowired
    private ScriptRepository scriptRepository;

    @Autowired
    private InstanceModelRepository instanceModelRepository;

    @Autowired
    private MockMvc mvc;

    private void cleanDB() {
        scriptRepository.deleteAll();
        instanceModelRepository.deleteAll();
        modelRepository.deleteAll();
    }

    @Autowired
    private ModelRepository modelRepository;

    private ModelParameterDefinitionEntity createDefinitionEntity(String name, ParameterType type, ParameterOrigin origin, String originName, ModelEntity model) {
        return new ModelParameterDefinitionEntity(name, model.getModelName(), type, origin, originName, model);
    }

    @Before
    public void setUp() {
        cleanDB();

        // Prepare instance models
        instanceModelRepository.save(new InstanceModelEntity("LoadLab", "LoadAlphaBeta", EquipmentType.LOAD, new ModelParamsEmbeddable("LAB", ParamsType.FIXED)));
        instanceModelRepository.save(new InstanceModelEntity("GeneratorSynchronousThreeWindingsProportionalRegulations", "GeneratorThreeWindings", EquipmentType.GENERATOR, new ModelParamsEmbeddable("GSTWPR", ParamsType.PREFIX)));
        instanceModelRepository.save(new InstanceModelEntity("GeneratorSynchronousFourWindingsProportionalRegulations", "GeneratorFourWindings", EquipmentType.GENERATOR, new ModelParamsEmbeddable("GSFWPR", ParamsType.PREFIX)));

        // prepare token model

        ModelEntity modelToSave = new ModelEntity("LoadAlphaBeta", EquipmentType.LOAD,
                null, null);
        ArrayList<ModelParameterDefinitionEntity> definitions = new ArrayList<>();
        definitions.add(createDefinitionEntity("load_alpha", ParameterType.DOUBLE, ParameterOrigin.USER, null, modelToSave));
        definitions.add(createDefinitionEntity("load_beta", ParameterType.DOUBLE, ParameterOrigin.USER, null, modelToSave));
        definitions.add(createDefinitionEntity("load_P0Pu", ParameterType.DOUBLE, ParameterOrigin.NETWORK, "p_pu", modelToSave));
        definitions.add(createDefinitionEntity("load_Q0Pu", ParameterType.DOUBLE, ParameterOrigin.NETWORK, "q_pu", modelToSave));
        definitions.add(createDefinitionEntity("load_U0Pu", ParameterType.DOUBLE, ParameterOrigin.NETWORK, "v_pu", modelToSave));
        definitions.add(createDefinitionEntity("load_UPhase0", ParameterType.DOUBLE, ParameterOrigin.NETWORK, "angle_pu", modelToSave));
        modelToSave.setParameterDefinitions(definitions);
        ArrayList<ModelParameterSetEntity> modelSets = new ArrayList<>();
        ModelParameterSetEntity setToSave = new ModelParameterSetEntity("LAB", modelToSave.getModelName(),
                null,
                new Date(),
                modelToSave);
        ArrayList<ModelParameterEntity> setParameters = new ArrayList<>();
        setParameters.add(new ModelParameterEntity("load_alpha", setToSave.getModelName(), setToSave.getName(), "1.5", setToSave));
        setParameters.add(new ModelParameterEntity("load_beta", setToSave.getModelName(), setToSave.getName(), "2.5", setToSave));
        setToSave.setParameters(setParameters);
        modelSets.add(setToSave);
        modelToSave.setSets(modelSets);
        modelRepository.save(modelToSave);
    }

    String mapping(String name, String modelName) {
        return "{\n" +
                "  \"name\": \"" + name + "\",\n" +
                "  \"rules\": [\n" +
                "    {\n" +
                "      \"composition\": \"filter1 && filter2 && filter3 && filter4\",\n" +
                "      \"equipmentType\": \"GENERATOR\",\n" +
                "      \"filters\": [\n" +
                "        {\n" +
                "          \"filterId\": \"filter1\",\n" +
                "          \"operand\": \"EQUALS\",\n" +
                "          \"property\": \"id\",\n" +
                "          \"value\": [\"test\"],\n" +
                "          \"type\": \"STRING\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"filterId\": \"filter2\",\n" +
                "          \"operand\": \"HIGHER\",\n" +
                "          \"property\": \"minP\",\n" +
                "          \"value\": [3.0],\n" +
                "          \"type\": \"NUMBER\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"filterId\": \"filter3\",\n" +
                "          \"operand\": \"IN\",\n" +
                "          \"property\": \"energySource\",\n" +
                "          \"value\": [\"HYDRO\", \"OTHERS\"],\n" +
                "          \"type\": \"STRING\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"filterId\": \"filter4\",\n" +
                "          \"operand\": \"NOT_EQUALS\",\n" +
                "          \"property\": \"voltageRegulatorOn\",\n" +
                "          \"value\": true,\n" +
                "          \"type\": \"BOOLEAN\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"mappedModel\": \"" + modelName + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"automata\": [\n" +
                "    {\n" +
                "      \"family\": \"CURRENT_LIMIT\",\n" +
                "      \"model\": \"automaton_model\",\n" +
                "      \"watchedElement\": \"element_id\",\n" +
                "      \"side\": \"Branch.Side.ONE\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"controlledParameters\": false" +
                "}";

    }

    String scriptOutput(String scriptName, String parentName) {
        return "{\"name\":\"" + scriptName + "\",\"parentName\":\"" + parentName + "\",\"script\":\"/**\n * Copyright (c) 2021, RTE (http://www.rte-france.com)\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n */\n\nimport com.powsybl.iidm.network.Generator\nimport com.powsybl.dynawaltz.automatons.CurrentLimitAutomaton\n\nfor (Generator equipment : network.generators) {\n          if (equipment.id.equals(\\\"test\\\") && equipment.minP > 3.000000 && [\\\"HYDRO\\\", \\\"OTHERS\\\"].contains(equipment.energySource) && equipment.voltageRegulatorOn != true) {\n                 GeneratorFourWindings {\n                     staticId equipment.id\n                     parameterSetId  \\\"GSFWPR\\\" + equipment.id\n                 }\n    }\n\n    OmegaRef {\n        generatorDynamicModelId equipment.id\n    }\n}\n\n" +
                "CurrentLimitAutomaton {\n" +
                "     staticId \\\"element_id\\\"\n" +
                "     dynamicModelId \\\"automaton_model\\\"\n" +
                "     parameterSetId \\\"automaton_model\\\"\n" +
                "     side Branch.Side.ONE\n" +
                "}\",\"current\": true, \"parametersFile\": null}";
    }

    @Test
    public void conversionTest() throws Exception {

        String name = "test";
        String modelName = "GeneratorSynchronousFourWindingsProportionalRegulations";

        // Put data
        mvc.perform(post("/mappings/" + name)
                        .content(mapping(name, modelName))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // convert to script
        mvc.perform(get("/scripts/from/" + name)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(scriptOutput(name + "-script", name), true));

        // try to convert unknown script
        mvc.perform(get("/scripts/from/" + "unknown")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Post a mapping without known model (OK car model not needed yet)
        mvc.perform(post("/mappings/" + name)
                        .content(mapping(name, "unknownModel"))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Try to convert this script
        // try to convert unknown script
        mvc.perform(get("/scripts/from/" + name)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    public void testRename() throws Exception {

        String mappingName = "origin";
        String newName = "new";
        String modelName = "GeneratorSynchronousFourWindingsProportionalRegulations";

        // Put data
        mvc.perform(post("/mappings/" + mappingName)
                        .content(mapping(mappingName, modelName))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // convert to script
        mvc.perform(get("/scripts/from/" + mappingName)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Rename data
        mvc.perform(post("/scripts/rename/" + mappingName + "-script/to/" + newName
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // get all data
        mvc.perform(get("/scripts/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + scriptOutput(newName, mappingName) + "]", true));

        // Add a new script
        mvc.perform(post("/mappings/" + mappingName)
                        .content(mapping(mappingName, modelName))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // convert to script
        mvc.perform(get("/scripts/from/" + mappingName)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Fail to rename to existing mapping
        mvc.perform(post("/scripts/rename/" + mappingName + "-script/to/" + newName
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict());

        // Fail to copy from missing mapping
        mvc.perform(post("/scripts/rename/NotUsed/to/AnyScript"
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    public void testCopy() throws Exception {

        String mappingName = "origin";
        String copyName = "copy";
        String modelName = "GeneratorSynchronousFourWindingsProportionalRegulations";

        // Put data
        mvc.perform(post("/mappings/" + mappingName)
                        .content(mapping(mappingName, modelName))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // convert to script
        mvc.perform(get("/scripts/from/" + mappingName)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Copy data
        mvc.perform(post("/scripts/copy/" + mappingName + "-script/to/" + copyName
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // get all s
        mvc.perform(get("/scripts/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + scriptOutput(mappingName + "-script", mappingName) + ", " + scriptOutput(copyName, mappingName) + "]", true));

        // Add a new script
        mvc.perform(post("/mappings/" + mappingName)
                        .content(mapping(mappingName, modelName))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // convert to script
        mvc.perform(get("/scripts/from/" + mappingName)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Fail to copy to existing mapping
        mvc.perform(post("/scripts/copy/" + mappingName + "-script/to/" + copyName
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict());

        // Fail to copy from missing mapping
        mvc.perform(post("/scripts/copy/NotUsed/to/AnyMapping"
                )
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    public void testSaveAndDelete() throws Exception {
        String name = "simple";
        String simpleScript = "{\"name\":\"" + name + "\",\"parentName\":\"\",\"script\":\"Script\",\"current\": true, \"parametersFile\": null}";

        // Put data
        mvc.perform(post("/scripts/" + name)
                        .content(simpleScript)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Get Data
        mvc.perform(get("/scripts/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[" + simpleScript + "]", true));

        // Delete Data
        mvc.perform(delete("/scripts/" + name))
                .andExpect(status().isOk());

        // Get Data
        mvc.perform(get("/scripts/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json("[]", true));

    }

    // Parameters tests

    @Test
    public void parTest() throws Exception {
        String name = "test";
        String mappingToTest = "{\n" +
                "  \"name\": \"" + name + "\",\n" +
                "  \"rules\": [\n" +
                "    {\n" +
                "      \"composition\": \"true\",\n" +
                "      \"equipmentType\": \"LOAD\",\n" +
                "      \"filters\": [],\n" +
                "      \"mappedModel\": \"" + "LoadLab" + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"automata\": []," +
                "  \"controlledParameters\": true" +
                "}";

        String parFile = "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\n" +
                "<parametersSet xmlns=\\\"http://www.rte-france.com/dynawo\\\">\n" +
                "    <set id=\\\"LAB\\\">\n" +
                "        <par type=\\\"DOUBLE\\\" name=\\\"load_alpha\\\" value=\\\"1.5\\\"/>\n" +
                "        <par type=\\\"DOUBLE\\\" name=\\\"load_beta\\\" value=\\\"2.5\\\"/>\n" +
                "        <reference type=\\\"DOUBLE\\\" name=\\\"load_P0Pu\\\" origData=\\\"IIDM\\\" origName=\\\"p_pu\\\"/>\n" +
                "        <reference type=\\\"DOUBLE\\\" name=\\\"load_Q0Pu\\\" origData=\\\"IIDM\\\" origName=\\\"q_pu\\\"/>\n" +
                "        <reference type=\\\"DOUBLE\\\" name=\\\"load_U0Pu\\\" origData=\\\"IIDM\\\" origName=\\\"v_pu\\\"/>\n" +
                "        <reference type=\\\"DOUBLE\\\" name=\\\"load_UPhase0\\\" origData=\\\"IIDM\\\" origName=\\\"angle_pu\\\"/>\n" +
                "    </set>\n" +
                "</parametersSet>";

        String scriptOutput = "{\"name\":\"" + name + "-script\",\"parentName\":\"" + name + "\",\"script\":\"/**\n * Copyright (c) 2021, RTE (http://www.rte-france.com)\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n */\n\nimport com.powsybl.iidm.network.Load\n\nfor (Load equipment : network.loads) {\n          if (true) {\n                 LoadAlphaBeta {\n                     staticId equipment.id\n                     parameterSetId  \\\"LAB\\\"\n                 }\n    }\n\n}\"," +
                "\"current\": true, \"parametersFile\": \"" + parFile + "\"}";

        // Put data
        mvc.perform(post("/mappings/" + name)
                        .content(mappingToTest)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // convert to script
        mvc.perform(get("/scripts/from/" + name)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(scriptOutput, true));
    }

    @Test
    public void currentTest() throws Exception {
        String name = "test";
        String mappingToTest = "{\n" +
                "  \"name\": \"" + name + "\",\n" +
                "  \"rules\": [\n" +
                "    {\n" +
                "      \"composition\": \"true\",\n" +
                "      \"equipmentType\": \"LOAD\",\n" +
                "      \"filters\": [],\n" +
                "      \"mappedModel\": \"" + "LoadLab" + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"automata\": []," +
                "  \"controlledParameters\": true" +
                "}";

        String setName = "LAB";
        String modelName = "LoadAlphaBeta";
        String set = "{\n" +
                "  \"name\": \"" + setName + "\",\n" +
                "  \"parameters\": [\n" +
                "    {\n" +
                "      \"name\": \"load_alpha\",\n" +
                "      \"value\": \"1.5\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"load_beta\",\n" +
                "      \"value\": \"2.5\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // Put data
        mvc.perform(post("/mappings/" + name)
                        .content(mappingToTest)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // convert to script
        mvc.perform(get("/scripts/from/" + name)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.current").value(true));

        // Modify Set
        mvc.perform(post("/model/" + modelName + "/parameters/sets/")
                        .content(set)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Check Current Status of the script
        mvc.perform(get("/scripts/")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$[0].current").value(false));
    }
}
