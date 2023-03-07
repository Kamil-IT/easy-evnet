package com.example.easyevnet.monitor.audit.database.model;


import com.example.easyevnet.orchestra.orchestra.model.StageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class StagePersistence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    private String businessId;

    private String stageName;
    private String topic;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime created;

    private String status;

    @Enumerated(EnumType.STRING)
    private StageType stageType;

    private String errorMessage;
}
