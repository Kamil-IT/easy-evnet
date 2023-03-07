package com.example.easyevnet.monitor.audit.database;

import com.example.easyevnet.monitor.audit.database.model.StagePersistence;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StatePersistenceRepository extends Repository<StagePersistence, UUID> {

    StagePersistence save(StagePersistence entity);

    Optional<StagePersistence> findFirstByBusinessIdAndStageNameAndTopic(String businessId, String stateName, String topic);

    List<StagePersistence> findAllByBusinessId(String businessId);

    List<StagePersistence> findAllByBusinessIdAndTopicOrderByCreated(String businessId, String topic);

    List<StagePersistence> findAll();

}
