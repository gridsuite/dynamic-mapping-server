package org.gridsuite.mapping.server.repository;

import org.gridsuite.mapping.server.model.ModelParameterDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelParameterDefinitionRepository extends JpaRepository<ModelParameterDefinitionEntity, String> {
}
