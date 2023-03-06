package com.example.easyevnet.orchestra.database;


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

//@Data
//@Builder
//@Entity
//@NoArgsConstructor
//@AllArgsConstructor
public class OrchestraPersistence {

//    TODO: Create persistence entity for orchestra

//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(nullable = false)
//    @JdbcTypeCode(SqlTypes.VARCHAR)
//    private UUID id;
//
//    private String businessId;
//
//    private String stateName;
//    private String topic;
//
//    @CreationTimestamp
//    @Temporal(TemporalType.TIMESTAMP)
//    private LocalDateTime created;
//
//    private String status;
//
//    @Enumerated(EnumType.STRING)
//    private StageType stageType;
//
//    private String errorMessage;
}
