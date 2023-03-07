package com.example.easyevnet.orchestra.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrchestraPersistenceRepository extends JpaRepository<OrchestraPersistence, UUID> {

    Optional<OrchestraPersistence> findByBusinessId(String businessId);
}
