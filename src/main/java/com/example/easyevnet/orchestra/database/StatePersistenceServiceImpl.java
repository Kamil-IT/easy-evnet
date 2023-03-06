package com.example.easyevnet.orchestra.database;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
// TODO: should be thread save
public class StatePersistenceServiceImpl implements StatePersistenceService {

    private final StatePersistenceRepository repository;
    private final OrchestraPersistenceRepository repositoryOr;

    @Override
    @Transactional
    public boolean isAbleToProcessIfYesStart(String id, String stateName, String topic) {
        Optional<StagePersistence> statePersistence = repository.findFirstByBusinessIdAndStageNameAndTopic(id, stateName, topic);

        if (statePersistence.isPresent()) {
            if (StageStatus.PROCESSING.equals(statePersistence.get().getStatus()) ||
                    StageStatus.ERROR.equals(statePersistence.get().getStatus())) {
                return false;
            } else {
                statePersistence.get().setStatus(StageStatus.PROCESSING.name());
                saveState(statePersistence.get());
                return true;
            }
        } else {
            StagePersistence stateToSave = StagePersistence.builder().businessId(id).stageName(stateName).build();
            saveState(stateToSave);
            return true;
        }
    }

    @Override
    public Map<StageType, Set<String>> getProcessedStagesByType(String id, String topic) {
        return repository.findAllByBusinessIdAndTopicOrderByCreated(id, topic)
                .stream().collect(Collectors.groupingBy(StagePersistence::getStageType,
                        Collectors.mapping(StagePersistence::getStageName, Collectors.toSet())));
    }

    @Override
    public void finishProcessing(String id, String stateName, String topic) {
        Optional<StagePersistence> statePersistence = repository.findFirstByBusinessIdAndStageNameAndTopic(id, stateName, topic);
        statePersistence.map(state -> {
            state.setStatus(StageStatus.DONE.name());
            return saveState(state);
        }).orElseThrow();
    }

    @Override
    public void markProcessAsError(String id, String stateName, String topic, String message) {
        Optional<StagePersistence> statePersistence = repository.findFirstByBusinessIdAndStageNameAndTopic(id, stateName, topic);
        statePersistence.map(state -> {
            state.setStatus(StageStatus.ERROR.name());
            state.setErrorMessage(message);
            return saveState(state);
        }).orElseThrow();
    }

    @Override
    public StagePersistence saveState(StagePersistence stagePersistence) {
        return repository.save(stagePersistence);
    }

    @Override
    public OrchestraPersistence saveOrchestra(OrchestraPersistence orchestraPersistence) {
        return repositoryOr.save(orchestraPersistence);
    }
}
