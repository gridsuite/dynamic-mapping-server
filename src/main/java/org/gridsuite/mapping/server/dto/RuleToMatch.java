/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.mapping.server.utils.EquipmentType;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Data
@Schema(description = "RuleToMatch")
public class RuleToMatch {
    @Schema(description = "Rule Index")
    private int ruleIndex;

    @Schema(description = "Equipment type")
    private EquipmentType equipmentType;

    @Schema(description = "Filter")
    private ExpertFilter filter;
}

