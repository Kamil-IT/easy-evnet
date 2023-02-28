package com.example.easyevnet.monitor.database;

import com.example.easyevnet.orchestra.database.StatePersistence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StateMonitorRepository extends JpaRepository<StatePersistence, UUID> {
}
