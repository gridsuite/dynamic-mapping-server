package org.gridsuite.mapping.server.service;

import org.gridsuite.mapping.server.dto.InputMapping;
import org.gridsuite.mapping.server.dto.RenameObject;

import java.util.List;

public interface MappingService {

    List<InputMapping> getMappingList();

    InputMapping createMapping(String mappingName, InputMapping mapping);

    int deleteMapping(String mappingName);

    RenameObject renameMapping(String oldName, String newName);

    InputMapping copyMapping(String originalName, String copyName);
}
