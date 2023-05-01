package org.gridsuite.mapping.server.repository;

import org.gridsuite.mapping.server.model.ModelVariableDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelVariableRepository extends JpaRepository<ModelVariableDefinitionEntity, String> {
}
