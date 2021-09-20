/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.mapping.server.service.implementation;

import org.gridsuite.mapping.server.dto.InstanceModel;
import org.gridsuite.mapping.server.repository.InstanceModelRepository;
import org.gridsuite.mapping.server.service.InstanceModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Scalbert <mathieu.scalbert at rte-france.com>
 */
@Service
public class InstanceModelServiceImpl implements InstanceModelService {

    private final InstanceModelRepository instanceModelRepository;

    @Autowired
    public InstanceModelServiceImpl(
            InstanceModelRepository instanceModelRepository
    ) {
        this.instanceModelRepository = instanceModelRepository;
    }

    @Override
    public List<InstanceModel> getInstances() {
        return instanceModelRepository.findAll().stream().map(InstanceModel::new).collect(Collectors.toList());
    }
}

