package org.gridsuite.mapping.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.gridsuite.mapping.server.model.MappingEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MappingRepository extends JpaRepository<MappingEntity, UUID> {

    // TODO Add User ?
    List<MappingEntity> findAll();

    Optional<MappingEntity> findByName(String name);

    @Transactional
    int deleteByName(String name);

    MappingEntity save(MappingEntity mappingEntity);
}
