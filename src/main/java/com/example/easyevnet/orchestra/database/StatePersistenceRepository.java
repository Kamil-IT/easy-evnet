package com.example.easyevnet.orchestra.database;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.data.repository.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StatePersistenceRepository extends Repository<StatePersistence, UUID> {

    StatePersistence save(StatePersistence entity);

    Optional<StatePersistence> findFirstByBusinessIdAndStateName(String businessId, String stateName);

    List<StatePersistence> findAllByBusinessId(String businessId);

}
