package org.gridsuite.mapping.server.repository;

import org.gridsuite.mapping.server.model.ScriptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScriptRepository extends JpaRepository<ScriptEntity, UUID> {

    // TODO Add User ?
    List<ScriptEntity> findAll();

    Optional<ScriptEntity> findByName(String name);

    Optional<List<ScriptEntity>> findByParent(String mappingName);

    @Transactional
    void deleteByName(String name);
}
