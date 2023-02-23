package com.example.easyevnet.saga.source.stage;

import com.example.easyevnet.saga.source.orchestra.OrchestraExecutor;
import com.example.easyevnet.saga.source.stage.model.Orchestra;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StateManager {

    private static final List<CompletableFuture<Boolean>> threads = new ArrayList<>();

    public static <ID> void startOrderedWorkflow(Orchestra orchestra, ID workflowIdentifier) {
        StageExecutor<ID> stageExecutor = new StageExecutor<>(orchestra, workflowIdentifier);
        OrchestraExecutor<ID> orchestraExecutor = new OrchestraExecutor<>(stageExecutor);

        threads.add(CompletableFuture.supplyAsync(orchestraExecutor::startOrchestra));
    }
}
