/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import lombok.*;
import org.gridsuite.mapping.server.dto.*;
import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;
import org.gridsuite.mapping.server.dto.models.ModelParameterDefinition;
import org.gridsuite.mapping.server.dto.models.ParametersSet;
import org.gridsuite.mapping.server.dto.models.ParametersSetsGroup;
import org.gridsuite.mapping.server.model.*;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.repository.ScriptRepository;
import org.gridsuite.mapping.server.service.ScriptService;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.ParameterOrigin;
import org.gridsuite.mapping.server.utils.ParameterType;
import org.gridsuite.mapping.server.utils.Templater;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
public class ScriptServiceImpl implements ScriptService {

    private final Function<RuleEntity, InstantiatedModel> funcRuleToInstantiatedModel = rule -> new InstantiatedModel(rule.getMappedModel(), rule.getSetGroup());
    private final Function<AutomatonEntity, InstantiatedModel> funcAutomatonToInstantiatedModel = automaton -> new InstantiatedModel(automaton.getModel(), automaton.getSetGroup());
    private final ModelRepository modelRepository;
    private final MappingRepository mappingRepository;
    private final ScriptRepository scriptRepository;

    public ScriptServiceImpl(
            MappingRepository mappingRepository,
            ScriptRepository scriptRepository,
            ModelRepository modelRepository
    ) {
        this.modelRepository = modelRepository;
        this.mappingRepository = mappingRepository;
        this.scriptRepository = scriptRepository;
    }

    String noModelFoundErrorMessage = "No model found with this name";

    @Override
    public Script createFromMapping(String mappingName) {
        Optional<MappingEntity> foundMapping = mappingRepository.findById(mappingName);
        if (foundMapping.isPresent()) {
            SortedMapping sortedMapping = new SortedMapping(new InputMapping(foundMapping.get()));
            String createdScript = Templater.mappingToScript(sortedMapping);
            // TODO: Add Date or randomise to ensure uniqueness
            String savedScriptName = sortedMapping.getName() + "-script";
            String createdPar = null;
            if (foundMapping.get().isControlledParameters()) {
                try {
                    // build enriched parameters sets from the mapping
                    Set<InstantiatedModel> instantiatedModels = foundMapping.get().getRules().stream()
                            .map(funcRuleToInstantiatedModel::apply)
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    instantiatedModels.addAll(foundMapping.get().getAutomata().stream()
                            .map(funcAutomatonToInstantiatedModel::apply)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));

                    List<List<EnrichedParametersSet>> setsLists = instantiatedModels.stream()
                            .map(instantiatedModel -> getEnrichedSetsFromInstanceModel(instantiatedModel.getModel(), instantiatedModel.getSetGroup()))
                            .collect(Collectors.toList());
                    List<EnrichedParametersSet> sets = new ArrayList<>();
                    setsLists.forEach(sets::addAll);

                    // generate .par content
                    createdPar = Templater.setsToPar(sets);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter sets");
                }
            }
            ScriptEntity scriptToSave = new ScriptEntity(savedScriptName, sortedMapping.getName(), createdScript, new Date(), createdPar);
            scriptToSave.markNotNew();
            scriptRepository.save(scriptToSave);
            Script scriptToReturn = new Script(scriptToSave);
            scriptToReturn.setCurrent(true);
            return scriptToReturn;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No mapping found with this name");
        }
    }

    @Override
    public List<Script> getAllScripts() {
        return scriptRepository.findAll().stream().map(scriptEntity -> {
            Script scriptToReturn = new Script(scriptEntity);
            if (scriptToReturn.getParametersFile() == null) {
                scriptToReturn.setCurrent(true);
            } else {
                scriptToReturn.setCurrent(isScriptCurrent(scriptToReturn));
            }
            return scriptToReturn;
        }).collect(Collectors.toList());
    }

    @Override
    public Script saveScript(String scriptName, Script script) {
        if (script.getParametersFile() != null) {
            // TODO: If you want to update .par via the script, add parametersFile creation here (found in createFromMapping)
            script.setCurrent(isScriptCurrent(script));
        } else {
            script.setCurrent(true);
        }
        scriptRepository.save(script.convertToEntity());
        return script;
    }

    @Override
    public String deleteScript(String scriptName) {
        scriptRepository.deleteById(scriptName);
        return scriptName;
    }

    @Override
    public RenameObject renameScript(String oldName, String newName) {
        Optional<ScriptEntity> scriptToRename = scriptRepository.findById(oldName);
        if (scriptToRename.isPresent()) {
            ScriptEntity scriptToSave = new ScriptEntity(newName, scriptToRename.get());
            try {
                scriptRepository.deleteById(oldName);
                scriptRepository.save(scriptToSave);
                return new RenameObject(oldName, newName);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A Script with this name already exists", ex);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No script found with this name");
        }
    }

    @Override
    public Script copyScript(String originalName, String copyName) {
        Optional<ScriptEntity> scriptToCopy = scriptRepository.findById(originalName);
        if (scriptToCopy.isPresent()) {
            ScriptEntity copiedScript = new ScriptEntity(copyName, scriptToCopy.get());
            try {
                scriptRepository.save(copiedScript);
                Script scriptToReturn = new Script(copiedScript);
                if (scriptToReturn.getParametersFile() == null) {
                    scriptToReturn.setCurrent(true);
                } else {
                    scriptToReturn.setCurrent(isScriptCurrent(scriptToReturn));
                }
                return scriptToReturn;
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A Script with this name already exists", ex);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No script found with this name");
        }
    }

    private boolean isScriptCurrent(Script script) {
        Optional<MappingEntity> foundMapping = mappingRepository.findById(script.getParentName());
        if (foundMapping.isPresent()) {
            try {
                MappingEntity scriptMapping = foundMapping.get();
                Set<InstantiatedModel> instantiatedModels = scriptMapping.getRules().stream()
                        .map(funcRuleToInstantiatedModel::apply)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                instantiatedModels.addAll(scriptMapping.getAutomata().stream()
                        .map(funcAutomatonToInstantiatedModel::apply)
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
                List<List<ParametersSet>> setsLists = instantiatedModels.stream()
                        .map(instantiatedModel -> getSetsFromInstanceModel(instantiatedModel.getModel(), instantiatedModel.getSetGroup()))
                        .collect(Collectors.toList());
                List<ParametersSet> sets = new ArrayList<>();
                setsLists.forEach(sets::addAll);
                return sets.parallelStream().reduce(true, (acc, set) -> acc && script.getCreatedDate().compareTo(set.getLastModifiedDate()) >= 0, (a, b) -> a && b);
            } catch (Exception e) {
                // Script status cannot be checked: Missing parts
                return false;
            }
        } else {
            return false;
        }

    }

    private List<EnrichedParametersSet> getEnrichedSetsFromInstanceModel(String modelName, String groupName) {
        Optional<ModelEntity> model = modelRepository.findById(modelName);
        if (model.isPresent()) {
            ParametersSetsGroup correspondingGroup = new ParametersSetsGroup(model.get().getSetsGroups().stream().filter(group -> group.getName().equals(groupName)).findAny().orElseThrow());
            List<ParametersSet> correspondingSets = correspondingGroup.getSets();
            List<ModelParameterDefinition> correspondingDefinitions = model.get().getParameterDefinitions().stream().map(ModelParameterDefinition::new).collect(Collectors.toList());

            return correspondingSets.stream().map(set -> new EnrichedParametersSet(set, correspondingDefinitions)).collect(Collectors.toList());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, noModelFoundErrorMessage);
        }
    }

    private List<ParametersSet> getSetsFromInstanceModel(String modelName, String groupName) {
        Optional<ModelEntity> model = modelRepository.findById(modelName);
        if (model.isPresent()) {
            ParametersSetsGroup correspondingGroup = new ParametersSetsGroup(model.get().getSetsGroups().stream().filter(group -> group.getName().equals(groupName)).findAny().orElseThrow());
            return correspondingGroup.getSets();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, noModelFoundErrorMessage);
        }
    }

    @Getter
    @Setter
    public class SortedMapping implements Mapping {
        private String name;
        private ArrayList<SortedRules> sortedRules;
        private ArrayList<AbstractAutomaton> automata;

        public SortedMapping(InputMapping mapping) {
            name = mapping.getName();
            automata = (ArrayList<AbstractAutomaton>) mapping.getAutomata();
            sortedRules = new ArrayList<>();
            Map<EquipmentType, List<Rule>> sortingRules = mapping.getRules().stream().collect(groupingBy(Rule::getEquipmentType));
            for (Map.Entry<EquipmentType, List<Rule>> sortingRulesEntry : sortingRules.entrySet()) {
                ArrayList<Rule> typedRules = new ArrayList<>(sortingRulesEntry.getValue());
                typedRules.sort(Rule.RULE_COMPARATOR);
                sortedRules.add(new SortedRules(sortingRulesEntry.getKey(), typedRules));
            }
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public class SortedRules {

        private String equipmentClass;

        private boolean isGenerator;

        private String collectionName;

        private List<FlatRule> rules;

        public SortedRules(EquipmentType type, List<Rule> sortedRules) {
            String convertedClass = Templater.equipmentTypeToClass(type);
            this.equipmentClass = convertedClass;
            this.isGenerator = convertedClass.equals("Generator");
            this.collectionName = Templater.equipmentTypeToCollectionName(type);
            rules = sortedRules.stream().map(FlatRule::new).collect(Collectors.toList());
        }
    }

    @Getter
    @AllArgsConstructor
    public class FlatRule {
        private EquipmentType equipmentType;

        private ModelSetsGroupEntity mappedModel;

        private String composition;

        public FlatRule(Rule rule) {
            Optional<ModelEntity> foundModel = modelRepository.findById(rule.getMappedModel());
            if (foundModel.isPresent()) {
                equipmentType = rule.getEquipmentType();
                mappedModel = foundModel.get().getSetsGroups().stream().filter(setGroup -> setGroup.getName().equals(rule.getSetGroup()) && setGroup.getType().equals(rule.getGroupType())).findAny().orElseThrow();
                composition = Templater.flattenFilters(rule.getComposition(), rule.getFilters());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, noModelFoundErrorMessage);
            }

        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public class EnrichedParametersSet {
        private String name;
        private List<EnrichedParameter> parameters;

        public EnrichedParametersSet(ParametersSet set, List<ModelParameterDefinition> definitions) {
            name = set.getName();
            parameters = definitions.stream().map(definition ->
                    new EnrichedParameter(
                            definition.getName(),
                            definition.getOrigin() == ParameterOrigin.NETWORK ? null : set.getParameters().stream().filter(parameter -> parameter.getName().equals(definition.getName())).findAny().orElseThrow().getValue(),
                            definition.getType(),
                            definition.getOrigin(),
                            definition.getOriginName()
                    )).collect(Collectors.toList());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class EnrichedParameter {
        private String name;
        private String value;
        private ParameterType type;
        private ParameterOrigin origin;
        private String originName;


    }

    @AllArgsConstructor
    @Getter
    @EqualsAndHashCode
    public class InstantiatedModel {
        private final String model;
        private final String setGroup;
    }
}

