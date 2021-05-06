package org.gridsuite.mapping.server.service.implementation;

import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.repository.ScriptRepository;
import org.gridsuite.mapping.server.service.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MappingServiceImpl implements MappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingServiceImpl.class);

    private final MappingRepository mappingRepository;
    private final ScriptRepository scriptRepository;

    @Autowired
    public MappingServiceImpl(
            MappingRepository mappingRepository,
            ScriptRepository scriptRepository
    ) {
        this.mappingRepository = mappingRepository;
        this.scriptRepository = scriptRepository;
    }

    @Override
    public List<InputMapping> getMappingList() {
        List<MappingEntity> mappingEntities = mappingRepository.findAll();

        return mappingEntities.stream().map(InputMapping::new).collect(Collectors.toList());
    }

    @Override
    public InputMapping createMapping(String mappingName, InputMapping mapping) {
        mappingRepository.save(mapping.convertMappingToEntity());
        return mapping;
    }

    @Override
    public Void deleteMapping(String mappingName) {
        return mappingRepository.deleteByName(mappingName);
    }

}

