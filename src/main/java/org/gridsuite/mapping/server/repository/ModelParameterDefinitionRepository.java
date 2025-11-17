package org.gridsuite.mapping.server.repository;

import org.gridsuite.mapping.server.model.ModelParameterDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ModelParameterDefinitionRepository extends JpaRepository<ModelParameterDefinitionEntity, UUID> {
    List<ModelParameterDefinitionEntity> findAllByName(Collection<String> names);

    void deleteAllByName(Collection<String> names);
}
