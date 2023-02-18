/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service;

import org.gridsuite.mapping.server.dto.RenameObject;
import org.gridsuite.mapping.server.dto.Script;

import java.util.List;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
public interface ScriptService {

    Script createFromMapping(String mappingName, boolean isPersitent);

    List<Script> getAllScripts();

    Script saveScript(String scriptName, Script script);

    String deleteScript(String scriptName);

    RenameObject renameScript(String oldName, String newName);

    Script copyScript(String originalName, String copyName);
}
