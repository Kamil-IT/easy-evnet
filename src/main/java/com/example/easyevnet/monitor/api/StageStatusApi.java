package com.example.easyevnet.monitor.api;

import com.example.easyevnet.monitor.api.model.GetStages;
import com.example.easyevnet.monitor.database.StateMonitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/easyevent/monitor/stage")
public class StageStatusApi {

    private final StateMonitorRepository repository;

    @GetMapping
    public GetStages stage() {
        return new GetStages(repository.findAll());
    }
}
