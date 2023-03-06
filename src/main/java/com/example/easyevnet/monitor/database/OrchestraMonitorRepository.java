package com.example.easyevnet.monitor.database;

import com.example.easyevnet.orchestra.database.OrchestraPersistence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrchestraMonitorRepository extends JpaRepository<OrchestraPersistence, UUID> {
}
