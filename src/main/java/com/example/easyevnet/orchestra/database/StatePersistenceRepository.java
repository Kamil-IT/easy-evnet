package com.example.easyevnet.orchestra.database;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
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
