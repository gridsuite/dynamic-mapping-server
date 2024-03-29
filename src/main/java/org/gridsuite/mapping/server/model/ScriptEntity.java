/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.model;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Getter
@Setter
@Table(name = "scripts")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptEntity extends AbstractManuallyAssignedIdentifierEntity<String> {
    @Id
    private String name;

    @Column(name = "parent", nullable = true)
    private String parentName;

    @Column(name = "script", nullable = false, columnDefinition = "CLOB")
    private String script;

    @Override
    public String getId() {
        return name;
    }

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "parameters_file", nullable = true, columnDefinition = "CLOB")
    private String parametersFile;

    public ScriptEntity(String name, ScriptEntity scriptToCopy) {
        this.name = name;
        parentName = scriptToCopy.getParentName();
        script = scriptToCopy.getScript();
        parametersFile = scriptToCopy.getParametersFile();
    }

}
