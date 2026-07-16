/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto.workspace;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Schema(name = "MappingWorkspaceItem", description = "Mapping workspace item")
public record MappingWorkspaceItem(
    @Schema(description = "Mapping workspace item ID")
    UUID id,

    @Schema(description = "Mapping ID")
    UUID mappingId,

    @Schema(description = "Pinned or not")
    boolean pinned
) {
}
