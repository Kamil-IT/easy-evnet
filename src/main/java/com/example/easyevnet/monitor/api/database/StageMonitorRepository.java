package com.example.easyevnet.monitor.api.database;

import com.example.easyevnet.monitor.audit.database.model.StagePersistence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StageMonitorRepository extends JpaRepository<StagePersistence, UUID> {
}
