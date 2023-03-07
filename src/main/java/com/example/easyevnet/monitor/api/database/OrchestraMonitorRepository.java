package com.example.easyevnet.monitor.api.database;

import com.example.easyevnet.monitor.audit.database.model.OrchestraPersistence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrchestraMonitorRepository extends JpaRepository<OrchestraPersistence, UUID> {
}
