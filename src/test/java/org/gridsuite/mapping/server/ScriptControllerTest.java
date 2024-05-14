/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server;

import org.gridsuite.mapping.server.dto.models.ModelParameterDefinition;
import org.gridsuite.mapping.server.model.*;
import org.gridsuite.mapping.server.repository.ModelParameterDefinitionRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.repository.ScriptRepository;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.gridsuite.mapping.server.utils.ParameterType;
import org.gridsuite.mapping.server.utils.SetGroupType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private MockMvc mvc;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelParameterDefinitionRepository modelParameterDefinitionRepository;

    private void cleanDB() {
        scriptRepository.deleteAll();
        modelRepository.deleteAll();
        modelParameterDefinitionRepository.deleteAll();
    }

    private ModelParameterDefinitionEntity createDefinitionEntity(String name, ParameterType type) {
        return new ModelParameterDefinitionEntity(new ModelParameterDefinition(name, type, null, null, null));
    }

    @Before
    public void setUp() {
        cleanDB();

        // Prepare models
        ModelEntity loadModel = new ModelEntity("LoadAlphaBeta", EquipmentType.LOAD, false, new ArrayList<>(), null, Set.of(), Set.of(), null, null);
        ArrayList<ModelSetsGroupEntity> loadGroups = new ArrayList<>();
        ModelSetsGroupEntity loadGroup = new ModelSetsGroupEntity("LAB", loadModel.getModelName(), null, SetGroupType.FIXED, loadModel);
        ArrayList<ModelParameterSetEntity> groupSets = new ArrayList<>();
        ModelParameterSetEntity setToSave = new ModelParameterSetEntity("LAB", loadGroup.getName(), loadModel.getModelName(), loadGroup.getType(),
                null,
                new Date(),
                loadGroup);
        ArrayList<ModelParameterEntity> setParameters = new ArrayList<>();
        setParameters.add(new ModelParameterEntity("load_alpha", loadGroup.getModelName(), loadGroup.getName(), loadGroup.getType(), setToSave.getName(), "1.5", setToSave));
        setParameters.add(new ModelParameterEntity("load_beta", loadGroup.getModelName(), loadGroup.getName(), loadGroup.getType(), setToSave.getName(), "2.5", setToSave));
        setToSave.setParameters(setParameters);
        groupSets.add(setToSave);
        loadGroup.setSets(groupSets);
        loadGroups.add(loadGroup);
        loadModel.setSetsGroups(loadGroups);

        List<ModelParameterDefinitionEntity> definitions = new ArrayList<>();

        // add "USER" parameter definitions
        definitions.add(createDefinitionEntity("load_alpha", ParameterType.DOUBLE));
        definitions.add(createDefinitionEntity("load_beta", ParameterType.DOUBLE));
        loadModel.addAllParameterDefinition(definitions, ParameterOrigin.USER, null);

        definitions.clear();

        // add "NETWORK" parameter definitions
        loadModel.addParameterDefinition(createDefinitionEntity("load_P0Pu", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "p_pu");
        loadModel.addParameterDefinition(createDefinitionEntity("load_Q0Pu", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "q_pu");
        loadModel.addParameterDefinition(createDefinitionEntity("load_U0Pu", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "v_pu");
        loadModel.addParameterDefinition(createDefinitionEntity("load_UPhase0", ParameterType.DOUBLE), ParameterOrigin.NETWORK, "angle_pu");

        modelRepository.save(loadModel);

        ModelEntity generatorThreeModel = new ModelEntity("GeneratorThreeWindings", EquipmentType.GENERATOR, false, null, null, null, null, null, null);
        ArrayList<ModelSetsGroupEntity> generatorThreeGroups = new ArrayList<>();
        generatorThreeGroups.add(new ModelSetsGroupEntity("GSTWPR", generatorThreeModel.getModelName(), null, SetGroupType.PREFIX, generatorThreeModel));
        generatorThreeModel.setSetsGroups(generatorThreeGroups);
        modelRepository.save(generatorThreeModel);

        ModelEntity generatorFourModel = new ModelEntity("GeneratorFourWindings", EquipmentType.GENERATOR, false, null, null, null, null, null, null);
        ArrayList<ModelSetsGroupEntity> generatorFourGroups = new ArrayList<>();
        generatorFourGroups.add(new ModelSetsGroupEntity("GSFWPR", generatorFourModel.getModelName(), null, SetGroupType.PREFIX, generatorFourModel));
        generatorFourModel.setSetsGroups(generatorFourGroups);
        modelRepository.save(generatorFourModel);

        ModelEntity sVarModel = new ModelEntity("StaticVarCompensator", EquipmentType.STATIC_VAR_COMPENSATOR, false, null, null, null, null, null, null);
        ArrayList<ModelSetsGroupEntity> sVarModelGroups = new ArrayList<>();
        sVarModelGroups.add(new ModelSetsGroupEntity("SVarC", sVarModel.getModelName(), null, SetGroupType.PREFIX, sVarModel));
        sVarModel.setSetsGroups(sVarModelGroups);
        modelRepository.save(sVarModel);
    }

    String mapping(String name, String modelName, String groupName) {
        return """
            {
                "name":"%s",
                "rules":[
                    {
                        "composition":"filter1 && filter2 && filter3 && filter4",
                        "equipmentType":"GENERATOR",
                        "filters":[
                            {
                                "filterId":"filter1",
                                "operand":"EQUALS",
                                "property":"id",
                                "value":[
                                    "test"
                                ],
                                "type":"STRING"
                            },
                            {
                                "filterId":"filter2",
                                "operand":"HIGHER",
                                "property":"minP",
                                "value":[
                                    3.0
                                ],
                                "type":"NUMBER"
                            },
                            {
                                "filterId":"filter3",
                                "operand":"IN",
                                "property":"energySource",
                                "value":[
                                    "HYDRO",
                                    "OTHERS"
                                ],
                                "type":"STRING"
                            },
                            {
                                "filterId":"filter4",
                                "operand":"NOT_EQUALS",
                                "property":"voltageRegulatorOn",
                                "value":true,
                                "type":"BOOLEAN"
                            }
                        ],
                        "mappedModel":"%s",
                        "setGroup":"%s",
                        "groupType":"PREFIX"
                    }
                ],
                "automata":[
                    {
                        "family":"CURRENT_LIMIT",
                        "model":"OverloadManagementSystem",
                        "setGroup":"automaton_group",
                        "properties":[
                            {
                                "name":"dynamicModelId",
                                "value":"cla_automaton_name",
                                "type":"STRING"
                            },
                            {
                                "name":"iMeasurement",
                                "value":"element_id",
                                "type":"STRING"
                            },
                            {
                                "name":"iMeasurementSide",
                                "value":"TwoSides.ONE",
                                "type":"ENUM"
                            },
                            {
                                "name":"controlledBranch",
                                "value":"element_id",
                                "type":"STRING"
                            }
                        ]
                    },
                    {
                        "family":"VOLTAGE",
                        "model":"TapChangerBlockingAutomationSystem",
                        "setGroup":"automaton_group_2",
                        "properties":[
                            {
                                "name":"dynamicModelId",
                                "value":"tcb_automaton_name",
                                "type":"STRING"
                            },
                            {
                                "name":"uMeasurement",
                                "value":"bus_id_1, bus_id_2",
                                "type":"STRING"
                            },
                            {
                                "name":"transformers",
                                "value":"load_id_1, load_id_2",
                                "type":"STRING"
                            }
                        ]
                    }
                ],
                "controlledParameters":false
            }
            """
            .formatted(name, modelName, groupName);

    }

    String scriptOutput(String scriptName, String parentName) {
        return "{\"name\":\"" + scriptName + "\",\"parentName\":\"" + parentName + "\",\"script\":\"/**\n * Copyright (c) 2021, RTE (http://www.rte-france.com)\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n */\n\nimport com.powsybl.iidm.network.Generator\nimport com.powsybl.iidm.network.TwoSides\n\nfor (Generator equipment : network.generators) {\n          if (equipment.id.equals(\\\"test\\\") && equipment.minP > 3.000000 && [\\\"HYDRO\\\", \\\"OTHERS\\\"].contains(equipment.energySource) && equipment.voltageRegulatorOn != true) {\n                 GeneratorFourWindings {\n                     staticId equipment.id\n                     parameterSetId  \\\"GSFWPR\\\" + equipment.id\n                 }\n    }\n}\n\n" +
                "OverloadManagementSystem {\n" +
                "     parameterSetId \\\"automaton_group\\\"\n" +
                "     dynamicModelId \\\"cla_automaton_name\\\"\n" +
                "     iMeasurement \\\"element_id\\\"\n" +
                "     iMeasurementSide TwoSides.ONE\n" +
                "     controlledBranch \\\"element_id\\\"\n" +
                "}\n\n" +
                "TapChangerBlockingAutomationSystem {\n" +
                "     parameterSetId \\\"automaton_group_2\\\"\n" +
                "     dynamicModelId \\\"tcb_automaton_name\\\"\n" +
                "     uMeasurement \\\"bus_id_1\\\", \\\"bus_id_2\\\"\n" +
                "     transformers \\\"load_id_1\\\", \\\"load_id_2\\\"\n" +
                "}" +
                "\",\"current\": true, \"parametersFile\": null}";
    }

    @Test
    @Transactional
    public void conversionTest() throws Exception {

        String name = "test";
        String modelName = "GeneratorFourWindings";
        String groupName = "GSFWPR";

        // Put data
        mvc.perform(post("/mappings/" + name)
                        .content(mapping(name, modelName, groupName))
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
                        .content(mapping(name, "unknownModel", "unknownSet"))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // Try to convert this script
        // try to convert unknown script
        mvc.perform(get("/scripts/from/" + name)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    @Transactional
    public void conversionSVarTest() throws Exception {

        String equipmentId = "SVC2";
        String equipmentType = "STATIC_VAR_COMPENSATOR";
        String modelName = "StaticVarCompensator";
        String groupName = "SVarC";
        String groupList = "staticVarCompensators";

        // Put data
        mvc.perform(post("/mappings/" + equipmentId)
                        .content(baseMapping(equipmentType, equipmentId, modelName, groupName))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        // convert to script
        mvc.perform(get("/scripts/from/" + equipmentId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(content().json(baseScript(equipmentId, modelName, groupName, groupList), true));
    }

    private String baseMapping(String equipmentType, String name, String model, String group) {
        return """
                {
                  "name": "%s",
                  "rules": [
                    {
                      "composition": "filter1 || filter2",
                      "equipmentType": "%s",
                      "filters": [
                        {
                          "filterId": "filter1",
                          "operand": "EQUALS",
                          "property": "id",
                          "value": ["%s"],
                          "type": "STRING"
                        },
                        {
                          "filterId": "filter2",
                          "operand": "NOT_EQUALS",
                          "property": "terminal.voltageLevel.substation.country.name",
                          "value": ["FRANCE"],
                          "type": "STRING"
                        }
                      ],
                      "mappedModel": "%s",
                      "setGroup": "%s",
                      "groupType": "PREFIX"
                    }
                 ],
                 "automata": [],
                 "controlledParameters": false
                }""".formatted(name, equipmentType, name, model, group);
    }

    private String baseScript(String name, String model, String group, String modelList) {
        return """
                {
                "name": "%s-script",
                "parentName": "%s",
                "script":"/**
                 * Copyright (c) 2021, RTE (http://www.rte-france.com)
                 * This Source Code Form is subject to the terms of the Mozilla Public
                 * License, v. 2.0. If a copy of the MPL was not distributed with this
                 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
                 */

                import com.powsybl.iidm.network.%s

                for (%s equipment : network.%s) {
                          if (equipment.id.equals(\\"%s\\") || !equipment.terminal.voltageLevel.substation.country.name.equals(\\"FRANCE\\")) {
                                 StaticVarCompensator {
                                     staticId equipment.id
                                     parameterSetId  \\"%s\\" + equipment.id
                                 }
                    }
                }",
                "current": true,
                "parametersFile": null
                }
                """.formatted(name, name, model, model, modelList, name, group);
    }

    @Test
    public void testRename() throws Exception {

        String mappingName = "origin";
        String newName = "new";
        String modelName = "GeneratorFourWindings";
        String groupName = "GSFWPR";
        // Put data
        mvc.perform(post("/mappings/" + mappingName)
                        .content(mapping(mappingName, modelName, groupName))
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
                        .content(mapping(mappingName, modelName, groupName))
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
        String modelName = "GeneratorFourWindings";
        String groupName = "GSFWPR";

        // Put data
        mvc.perform(post("/mappings/" + mappingName)
                        .content(mapping(mappingName, modelName, groupName))
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
                        .content(mapping(mappingName, modelName, groupName))
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
        String simpleScript = """
            {
                "name":"%s",
                "parentName":"",
                "script":"Script",
                "current":true,
                "parametersFile":null
            }
            """
            .formatted(name);

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
        String modelName = "LoadAlphaBeta";
        String groupName = "LAB";
        String mappingToTest = """
            {
                "name":"%s",
                "rules":[
                    {
                        "composition":"true",
                        "equipmentType":"LOAD",
                        "filters":[],
                        "mappedModel":"%s",
                        "setGroup":"%s",
                        "groupType":"FIXED"
                    }
                ],
                "automata":[],
                "controlledParameters":true
            }
            """
            .formatted(name, modelName, groupName);

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

        String scriptOutput = "{\"name\":\"" + name + "-script\",\"parentName\":\"" + name + "\",\"script\":\"/**\n * Copyright (c) 2021, RTE (http://www.rte-france.com)\n * This Source Code Form is subject to the terms of the Mozilla Public\n * License, v. 2.0. If a copy of the MPL was not distributed with this\n * file, You can obtain one at http://mozilla.org/MPL/2.0/.\n */\n\nimport com.powsybl.iidm.network.Load\n\nfor (Load equipment : network.loads) {\n          if (true) {\n                 LoadAlphaBeta {\n                     staticId equipment.id\n                     parameterSetId  \\\"LAB\\\"\n                 }\n    }\n}\"," +
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
    @Transactional
    public void currentTest() throws Exception {
        String name = "test";
        String setName = "LAB";
        String modelName = "LoadAlphaBeta";
        String mappingToTest = """
            {
                "name":"%s",
                "rules":[
                    {
                        "composition":"true",
                        "equipmentType":"LOAD",
                        "filters":[],
                        "mappedModel":"%s",
                        "setGroup":"%s",
                        "groupType":"FIXED"
                    }
                ],
                "automata":[],
                "controlledParameters":true
            }
            """
            .formatted(name, modelName, setName);

        String set = """
            {
                "name":"%s",
                "modelName":"%s",
                "parameters":[
                    {
                        "name":"load_alpha",
                        "value":"1.5"
                    },
                    {
                        "name":"load_beta",
                        "value":"2.5"
                    }
                ]
            }
            """
            .formatted(setName, modelName);

        String setGroup = """
            {
                "name":"%s",
                "modelName":"%s",
                "type":"FIXED",
                "sets":[
                    %s
                ]
            }
            """
            .formatted(setName, modelName, set);
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
        mvc.perform(post("/models/" + modelName + "/parameters/sets/")
                        .content(setGroup)
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
