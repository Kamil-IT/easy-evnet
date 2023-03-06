package com.example.easyevnet.orchestra.database;

public enum OrchestraStatus {

    PROCESSING,
    DONE,
    ERROR;

    boolean equals(String name) {
        return this.name().equals(name);
    }
}
