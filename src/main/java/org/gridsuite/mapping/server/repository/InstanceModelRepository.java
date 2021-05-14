package org.gridsuite.mapping.server.repository;

import org.gridsuite.mapping.server.model.InstanceModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstanceModelRepository extends JpaRepository<InstanceModelEntity, String> {

    List<InstanceModelEntity> findAll();

    Optional<InstanceModelEntity> findById(String id);

    Optional<List<InstanceModelEntity>> findByModelName(String name);

    @Transactional
    void deleteById(String id);
}
