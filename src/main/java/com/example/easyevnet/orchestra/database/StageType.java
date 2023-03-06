package com.example.easyevnet.orchestra.database;

public enum StageType {

    ORDERED,
    DEFAULT,
    BRAKING;

    boolean equals(String name) {
        return this.name().equals(name);
    }
}
