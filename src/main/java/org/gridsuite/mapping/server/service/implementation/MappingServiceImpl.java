/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.RenameObject;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.service.MappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.gridsuite.mapping.server.MappingConstants.DEFAULT_MAPPING_NAME;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
public class MappingServiceImpl implements MappingService {

    private final MappingRepository mappingRepository;

    @Autowired
    public MappingServiceImpl(
            MappingRepository mappingRepository
    ) {
        this.mappingRepository = mappingRepository;
    }

    @Override
    public List<InputMapping> getMappingList() {
        List<MappingEntity> mappingEntities = mappingRepository.findAll();

        return mappingEntities.stream().map(InputMapping::new).collect(Collectors.toList());
    }

    @Override
    public InputMapping createMapping(String mappingName, InputMapping mapping) {
        MappingEntity mappingToSave = mapping.convertMappingToEntity();
        mappingToSave.markNotNew();
        mappingRepository.save(mappingToSave);
        return mapping;
    }

    @Override
    public String deleteMapping(String mappingName) {
        mappingRepository.deleteById(mappingName);
        return mappingName;
    }

    @Override
    public RenameObject renameMapping(String oldName, String newName) {
        Optional<MappingEntity> mappingToRename = mappingRepository.findById(oldName);
        if (mappingToRename.isPresent()) {
            MappingEntity mappingToSave = new MappingEntity(newName, mappingToRename.get());
            try {
                mappingRepository.deleteById(oldName);
                mappingRepository.save(mappingToSave);
                return new RenameObject(oldName, newName);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A Mapping with this name already exists", ex);
            }
        } else if (oldName.equals(DEFAULT_MAPPING_NAME)) {
            // In case of naming of new mapping, save it to db.
            try {
                mappingRepository.save(new MappingEntity(newName, new ArrayList<>()));
                return new RenameObject(DEFAULT_MAPPING_NAME, newName);

            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A Mapping with this name already exists", ex);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No mapping found with this name");
        }
    }

    @Override
    public InputMapping copyMapping(String originalName, String copyName) {
        Optional<MappingEntity> mappingToCopy = mappingRepository.findById(originalName);
        if (mappingToCopy.isPresent()) {
            MappingEntity copiedMapping = new MappingEntity(copyName, mappingToCopy.get());
            try {
                mappingRepository.save(copiedMapping);
                return new InputMapping(copiedMapping);
            } catch (DataIntegrityViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A Mapping with this name already exists", ex);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No mapping found with this name");
        }
    }

}

