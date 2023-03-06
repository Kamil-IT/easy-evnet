package com.example.easyevnet.monitor.database;

import com.example.easyevnet.orchestra.database.StagePersistence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StageMonitorRepository extends JpaRepository<StagePersistence, UUID> {
}
