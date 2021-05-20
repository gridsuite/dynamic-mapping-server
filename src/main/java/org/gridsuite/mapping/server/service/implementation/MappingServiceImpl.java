package org.gridsuite.mapping.server.service.implementation;

import org.gridsuite.mapping.server.MappingException;
import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.RenameObject;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.service.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MappingServiceImpl implements MappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingServiceImpl.class);

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
    public int deleteMapping(String mappingName) {
        return mappingRepository.deleteByName(mappingName);
    }

    @Override
    public RenameObject renameMapping(String oldName, String newName) {
        Optional<MappingEntity> mappingToRename = mappingRepository.findByName(oldName);
        if (mappingToRename.isPresent()) {
            MappingEntity mappingToSave = new MappingEntity(newName, mappingToRename.get());
            mappingRepository.deleteByName(oldName);
            mappingRepository.save(mappingToSave);
            return new RenameObject(oldName, newName);
        } else {
            throw new MappingException(MappingException.Type.MAPPING_NOT_FOUND);
        }
    }

    @Override
    public InputMapping copyMapping(String originalName, String copyName) {
        Optional<MappingEntity> mappingToCopy = mappingRepository.findByName(originalName);
        if (mappingToCopy.isPresent()) {
            MappingEntity copiedMapping = new MappingEntity(copyName, mappingToCopy.get());
            mappingRepository.save(copiedMapping);
            return new InputMapping(copiedMapping);
        } else {
            throw new MappingException(MappingException.Type.MAPPING_NOT_FOUND);
        }
    }

}

