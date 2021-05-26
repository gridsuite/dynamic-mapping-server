package org.gridsuite.mapping.server.service;

import org.gridsuite.mapping.server.dto.RenameObject;
import org.gridsuite.mapping.server.dto.Script;

import java.util.List;

public interface ScriptService {

    Script createFromMapping(String mappingName);

    List<Script> getAllScripts();

    Script saveScript(String scriptName, Script script);

    String deleteScript(String scriptName);

    RenameObject renameScript(String oldName, String newName);

    Script copyScript(String originalName, String copyName);
}
