/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.gridsuite.mapping.server.dto.*;
import org.gridsuite.mapping.server.dto.automata.AbstractAutomaton;
import org.gridsuite.mapping.server.model.InstanceModelEntity;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.model.ScriptEntity;
import org.gridsuite.mapping.server.repository.InstanceModelRepository;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.repository.ModelRepository;
import org.gridsuite.mapping.server.repository.ScriptRepository;
import org.gridsuite.mapping.server.service.ScriptService;
import org.gridsuite.mapping.server.utils.EquipmentType;
import org.gridsuite.mapping.server.utils.Templater;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
public class ScriptServiceImpl implements ScriptService {

    private final InstanceModelRepository instanceModelRepository;
    private final ModelRepository modelRepository;
    private final MappingRepository mappingRepository;
    private final ScriptRepository scriptRepository;

    public ScriptServiceImpl(
            MappingRepository mappingRepository,
            ScriptRepository scriptRepository,
            InstanceModelRepository instanceModelRepository,
            ModelRepository modelRepository
    ) {
        this.modelRepository = modelRepository;
        this.instanceModelRepository = instanceModelRepository;
        this.mappingRepository = mappingRepository;
        this.scriptRepository = scriptRepository;
    }

    @Override
    public Script createFromMapping(String mappingName) {
        Optional<MappingEntity> foundMapping = mappingRepository.findById(mappingName);
        if (foundMapping.isPresent()) {
            SortedMapping sortedMapping = new SortedMapping(new InputMapping(foundMapping.get()));
            String createdScript = Templater.mappingToScript(sortedMapping);
            // TODO: Add Date or randomise to ensure uniqueness
            String savedScriptName = sortedMapping.getName() + "-script";
            //TODO Check current .par

            // End TODO
            ScriptEntity scriptToSave = new ScriptEntity(savedScriptName, sortedMapping.getName(), createdScript, new Date());
            scriptToSave.markNotNew();
            scriptRepository.save(scriptToSave);
            return new Script(scriptToSave);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No mapping found with this name");
        }
    }

    @Override
    public List<Script> getAllScripts() {
        return scriptRepository.findAll().stream().map(scriptEntity -> new Script(scriptEntity)).collect(Collectors.toList());
    }

    @Override
    public Script saveScript(String scriptName, Script script) {
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
                return new Script(copiedScript);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A Script with this name already exists", ex);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No script found with this name");
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
            for (EquipmentType type : sortingRules.keySet()) {
                ArrayList<Rule> typedRules = new ArrayList<>(sortingRules.get(type));
                typedRules.sort(Rule.ruleComparator);
                sortedRules.add(new SortedRules(type, typedRules));
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

        public SortedRules(EquipmentType type, ArrayList<Rule> sortedRules) {
            String equipmentClass = Templater.equipmentTypeToClass(type);
            this.equipmentClass = equipmentClass;
            this.isGenerator = equipmentClass.equals("Generator");
            this.collectionName = Templater.equipmentTypeToCollectionName(type);
            rules = sortedRules.stream().map(rule -> new FlatRule(rule)).collect(Collectors.toList());
        }
    }

    @Getter
    @AllArgsConstructor
    public class FlatRule {
        private EquipmentType equipmentType;

        private InstanceModelEntity mappedModel;

        private String composition;

        public FlatRule(Rule rule) {
            Optional<InstanceModelEntity> foundModel = instanceModelRepository.findById(rule.getMappedModel());
            if (foundModel.isPresent()) {
                equipmentType = rule.getEquipmentType();
                mappedModel = foundModel.get();
                composition = Templater.flattenFilters(rule.getComposition(), rule.getFilters());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No model found with this name");
            }

        }
    }
}
