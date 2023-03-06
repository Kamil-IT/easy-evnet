package com.example.easyevnet.monitor.api;

import com.example.easyevnet.monitor.api.model.ResponseList;
import com.example.easyevnet.monitor.database.OrchestraMonitorRepository;
import com.example.easyevnet.monitor.database.StageMonitorRepository;
import com.example.easyevnet.orchestra.database.OrchestraPersistence;
import com.example.easyevnet.orchestra.database.StagePersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/easyevent/monitor")
public class StageStatusApi {

    private final StageMonitorRepository stageRepository;
    private final OrchestraMonitorRepository orchestraMonitorRepository;

    @GetMapping("stage")
    public ResponseList<StagePersistence> stage() {
        return new ResponseList<>(stageRepository.findAll());
    }

    @GetMapping("orchestra")
    public ResponseList<OrchestraPersistence> orchestra() {
        return new ResponseList<>(orchestraMonitorRepository.findAll());
    }
}
