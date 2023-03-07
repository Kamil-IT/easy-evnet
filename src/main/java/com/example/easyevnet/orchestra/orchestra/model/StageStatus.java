package com.example.easyevnet.orchestra.orchestra.model;

public enum StageStatus {

    PROCESSING,
    DONE,
    TIMEOUT,
    ERROR;

    boolean equals(String name) {
        return this.name().equals(name);
    }
}
