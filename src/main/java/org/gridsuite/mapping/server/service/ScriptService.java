package org.gridsuite.mapping.server.service;

import org.gridsuite.mapping.server.dto.Script;

import java.util.List;

public interface ScriptService {

    Script createFromMapping(String mappingName);

    List<Script> getAllScripts();

    Void updateScript(String scriptName, Script script);

    Void deleteScript(String scriptName);
}
