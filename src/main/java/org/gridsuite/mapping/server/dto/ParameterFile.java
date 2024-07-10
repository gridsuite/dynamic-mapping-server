/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Data
@Schema(description = "Parameter sets in *.par format")
@AllArgsConstructor
public class ParameterFile {

    @Schema(description = "Name of the parent mapping")
    private String mappingName;

    @Schema(description = "Parameter file content in *.par format")
    private String fileContent;

}
