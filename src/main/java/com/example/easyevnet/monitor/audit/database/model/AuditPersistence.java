package com.example.easyevnet.monitor.audit.database.model;


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
public class AuditPersistence {
//    TODO: Add audit for incoming messages
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(nullable = false)
//    @JdbcTypeCode(SqlTypes.VARCHAR)
//    private UUID id;
//
//    private String businessId;
//
//    private String orchestraName;
//
//    @CreationTimestamp
//    @Temporal(TemporalType.TIMESTAMP)
//    private LocalDateTime created;
//
//    private LocalDateTime finished;
//
//    private String status;
//
//    private String brokerUrl;
//
//    private String stagesInOrder;
//    private String stagesBraking;
//    private String sagesDefault;
}
