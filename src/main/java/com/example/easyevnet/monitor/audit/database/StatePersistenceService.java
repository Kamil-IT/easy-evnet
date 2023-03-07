package com.example.easyevnet.monitor.audit.database;

import com.example.easyevnet.monitor.audit.database.model.OrchestraPersistence;
import com.example.easyevnet.monitor.audit.database.model.StagePersistence;
import com.example.easyevnet.orchestra.orchestra.model.StageType;
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

    OrchestraPersistence saveOrchestra(OrchestraPersistence orchestraPersistence);

    OrchestraPersistence finishOrchestra(String id);
}
