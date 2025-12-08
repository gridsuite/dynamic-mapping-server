/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.error;

import com.powsybl.ws.commons.error.AbstractBusinessException;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * @author Thang PHAM <quyet-thang.pham at rte-france.com>
 */
@Getter
public class DynamicMappingException extends AbstractBusinessException {
    private final DynamicMappingErrorBusinessCode errorCode;

    public DynamicMappingException(DynamicMappingErrorBusinessCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    @NotNull
    @Override
    public DynamicMappingErrorBusinessCode getBusinessErrorCode() {
        return errorCode;
    }
}
