package com.example.easyevnet.orchestra.database;

public enum StageStatus {

    PROCESSING,
    DONE,
    TIMEOUT,
    ERROR;

    boolean equals(String name) {
        return this.name().equals(name);
    }
}
