package com.example.easyevnet.orchestra.database;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
// TODO: should be thread save
public class StatePersistenceService {

    private final StatePersistenceRepository repository;

    @Transactional
    public boolean isAbleToProcessIfYesStart(String id, String stateName) {
        Optional<StatePersistence> statePersistence = repository.findFirstByBusinessIdAndStateName(id, stateName);

        if (statePersistence.isPresent()) {
            if (StateStatus.PROCESSING.equals(statePersistence.get().getStatus()) ||
                    StateStatus.ERROR.equals(statePersistence.get().getStatus())) {
                return false;
            } else {
                statePersistence.get().setStatus(StateStatus.PROCESSING.name());
                saveState(statePersistence.get());
                return true;
            }
        } else {
            StatePersistence stateToSave = StatePersistence.builder().businessId(id).stateName(stateName).build();
            saveState(stateToSave);
            return true;
        }
    }

    public void finishProcessing(String id, String stateName) {
        Optional<StatePersistence> statePersistence = repository.findFirstByBusinessIdAndStateName(id, stateName);
        statePersistence.map(state -> {
            state.setStatus(StateStatus.DONE.name());
            return saveState(state);
        }).orElseThrow();
    }

    public void markProcessAsError(String id, String stateName, String message) {
        Optional<StatePersistence> statePersistence = repository.findFirstByBusinessIdAndStateName(id, stateName);
        statePersistence.map(state -> {
            state.setStatus(StateStatus.ERROR.name());
            state.setErrorMessage(message);
            return saveState(state);
        }).orElseThrow();
    }

    private StatePersistence saveState(StatePersistence statePersistence) {
        return repository.save(statePersistence);
    }
}
