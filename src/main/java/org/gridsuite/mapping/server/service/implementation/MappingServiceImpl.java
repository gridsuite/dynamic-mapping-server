package org.gridsuite.mapping.server.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.model.MappingEntity;
import org.gridsuite.mapping.server.repository.MappingRepository;
import org.gridsuite.mapping.server.repository.ScriptRepository;
import org.gridsuite.mapping.server.service.MappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class MappingServiceImpl implements MappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingServiceImpl.class);

    @Autowired
    MappingServiceImpl self;
    private WebClient webClient;
    private ObjectMapper objectMapper;

    private MappingRepository mappingRepository;
    private ScriptRepository scriptRepository;

    public MappingServiceImpl(
            MappingRepository mappingRepository,
            ScriptRepository scriptRepository,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.mappingRepository = mappingRepository;
        this.scriptRepository = scriptRepository;
        this.webClient =  webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public List<InputMapping> getMappingList() {
        List<MappingEntity> mappingEntities = mappingRepository.findAll();
        //TODO Convert
        return new ArrayList<InputMapping>();
    }
}

