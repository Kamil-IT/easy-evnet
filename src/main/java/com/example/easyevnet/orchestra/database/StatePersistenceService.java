package com.example.easyevnet.orchestra.database;

import jakarta.transaction.Transactional;

import java.util.Map;
import java.util.Set;

public interface StatePersistenceService {
    @Transactional
    boolean isAbleToProcessIfYesStart(String id, String stateName, String topic);
    Map<StageType, Set<String>> getProcessedStagesByType(String id, String topic);

    void finishProcessing(String id, String stateName, String topic);

    void markProcessAsError(String id, String stateName, String topic, String message);

    StagePersistence saveState(StagePersistence stagePersistence);
}
