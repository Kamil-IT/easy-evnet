package com.example.easyevnet.monitor.api.model;

import com.example.easyevnet.orchestra.database.StagePersistence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetStages {
    List<StagePersistence> stages;
}
