package com.example.easyevnet.orchestra.orchestra.model;

public enum OrchestraStatus {

    PROCESSING,
    DONE,
    ERROR;

    boolean equals(String name) {
        return this.name().equals(name);
    }
}
